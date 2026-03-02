package com.tp.tpapp.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import com.tp.tpapp.data.dao.AccountDao
import com.tp.tpapp.data.dao.AppDao
import com.tp.tpapp.data.dao.RecordDao
import com.tp.tpapp.data.model.AccountEntity
import com.tp.tpapp.data.model.AppEntity
import com.tp.tpapp.data.model.AppWithAccounts
import com.tp.tpapp.data.model.RecordEntity
import com.tp.tpapp.data.security.KeystoreAesGcmPasswordCipher
import com.tp.tpapp.data.security.PasswordCipher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.InputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class TPRepository(
    private val appDao: AppDao,
    private val accountDao: AccountDao,
    private val recordDao: RecordDao,
    private val passwordCipher: PasswordCipher
) {
    // ========== App 操作 ==========

    fun getAllApps(): Flow<List<AppEntity>> = appDao.getAllApps()

    fun getAppById(id: Long): Flow<AppEntity?> = appDao.getAppById(id)

    fun getAppWithAccounts(appId: Long): Flow<AppWithAccounts?> =
        appDao.getAppWithAccounts(appId).map { appWithAccounts ->
            appWithAccounts?.let { item ->
                val decryptedAccounts = item.accounts.map { decryptAccountForRead(it, migrateIfPlain = true) }
                item.copy(accounts = decryptedAccounts)
            }
        }

    fun searchApps(query: String): Flow<List<AppEntity>> = appDao.searchApps(query)

    suspend fun insertApp(app: AppEntity): Long {
        val nextOrder = appDao.getNextSortOrder()
        return appDao.insertApp(app.copy(sortOrder = nextOrder))
    }

    suspend fun updateApp(app: AppEntity) = appDao.updateApp(app)

    suspend fun deleteApp(app: AppEntity) = appDao.deleteApp(app)

    fun getDeletedApps(): Flow<List<AppEntity>> = appDao.getDeletedApps()

    suspend fun softDeleteApp(id: Long) = appDao.softDelete(id)

    suspend fun restoreApp(id: Long) = appDao.restore(id)

    suspend fun hardDeleteApp(id: Long) = appDao.hardDelete(id)

    suspend fun emptyAppTrash() = appDao.emptyTrash()

    suspend fun reorderApps(apps: List<AppEntity>) {
        val updated = apps.mapIndexed { index, app -> app.copy(sortOrder = index) }
        appDao.updateApps(updated)
    }

    // ========== Account 操作 ==========

    fun getAccountsByAppId(appId: Long): Flow<List<AccountEntity>> =
        accountDao.getAccountsByAppId(appId).map { accounts ->
            accounts.map { decryptAccountForRead(it, migrateIfPlain = true) }
        }

    fun getAccountById(id: Long): Flow<AccountEntity?> =
        accountDao.getAccountById(id).map { account ->
            account?.let { decryptAccountForRead(it, migrateIfPlain = true) }
        }

    suspend fun insertAccount(account: AccountEntity): Long {
        val nextOrder = accountDao.getNextSortOrder(account.appId)
        val accountForStorage = encryptAccountForStorage(account.copy(sortOrder = nextOrder))
        return accountDao.insertAccount(accountForStorage)
    }

    suspend fun updateAccount(account: AccountEntity) {
        accountDao.updateAccount(encryptAccountForStorage(account))
    }

    suspend fun deleteAccount(account: AccountEntity) = accountDao.deleteAccount(account)

    suspend fun reorderAccounts(accounts: List<AccountEntity>) {
        val updated = accounts.mapIndexed { index, account ->
            encryptAccountForStorage(account.copy(sortOrder = index))
        }
        accountDao.updateAccounts(updated)
    }

    // ========== Record 操作 ==========

    fun getActiveRecords(): Flow<List<RecordEntity>> = recordDao.getActiveRecords()

    fun getDeletedRecords(): Flow<List<RecordEntity>> = recordDao.getDeletedRecords()

    fun getRecordById(id: Long): Flow<RecordEntity?> = recordDao.getRecordById(id)

    suspend fun insertRecord(record: RecordEntity): Long {
        val nextOrder = recordDao.getNextSortOrder()
        return recordDao.insertRecord(record.copy(sortOrder = nextOrder))
    }

    suspend fun updateRecord(record: RecordEntity) = recordDao.updateRecord(record)

    suspend fun softDeleteRecord(id: Long) = recordDao.softDelete(id)

    suspend fun restoreRecord(id: Long) = recordDao.restore(id)

    suspend fun hardDeleteRecord(id: Long) = recordDao.hardDelete(id)

    suspend fun emptyTrash() = recordDao.emptyTrash()

    suspend fun toggleCollapse(id: Long, isCollapsed: Boolean) =
        recordDao.updateCollapsed(id, isCollapsed)

    suspend fun reorderRecords(records: List<RecordEntity>) {
        val updated = records.mapIndexed { index, record -> record.copy(sortOrder = index) }
        recordDao.updateRecords(updated)
    }

    // ========== 导出 ==========

    suspend fun exportToJson(outputStream: OutputStream) {
        val allAppsWithAccounts = appDao.getAllAppsWithAccounts().first()
        val allRecords = recordDao.getActiveRecords().first()

        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")

        val exportData = ExportData(
            version = 2,
            exportedAt = dateFormat.format(Date()),
            apps = allAppsWithAccounts.map { appWithAccounts ->
                ExportApp(
                    appName = appWithAccounts.app.appName,
                    note = appWithAccounts.app.note,
                    isPinned = appWithAccounts.app.isPinned,
                    accounts = appWithAccounts.accounts.map { account ->
                        ExportAccount(
                            username = account.username,
                            password = decryptPasswordForRead(account.password),
                            imageUri = account.imageUri,
                            note = account.note,
                            tags = account.tags?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() },
                            updatedAt = dateFormat.format(Date(account.updatedAt))
                        )
                    }
                )
            },
            records = allRecords.map { record ->
                ExportRecord(
                    title = record.title,
                    content = record.content,
                    isCollapsed = record.isCollapsed,
                    createdAt = dateFormat.format(Date(record.createdAt)),
                    updatedAt = dateFormat.format(Date(record.updatedAt))
                )
            }
        )

        val gson: Gson = GsonBuilder().setPrettyPrinting().create()
        val json = gson.toJson(exportData)
        outputStream.write(json.toByteArray(Charsets.UTF_8))
        outputStream.flush()
    }

    // ========== 导入 ==========

    suspend fun importFromJson(inputStream: InputStream, mode: ImportMode): ImportResult {
        return try {
            val json = inputStream.bufferedReader(Charsets.UTF_8).readText()
            val gson = Gson()
            val rootElement = JsonParser.parseString(json)
            if (!rootElement.isJsonObject) {
                return ImportResult.Error("导入文件格式无效")
            }
            val rootObject = rootElement.asJsonObject
            val includeRecords = rootObject.has("records")
            val exportData = gson.fromJson(rootObject, ExportData::class.java)
                ?: return ImportResult.Error("导入文件格式无效")

            val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
            dateFormat.timeZone = TimeZone.getTimeZone("UTC")

            when (mode) {
                ImportMode.OVERWRITE -> importOverwrite(exportData, dateFormat, includeRecords)
                ImportMode.MERGE -> importMerge(exportData, dateFormat, includeRecords)
            }
        } catch (e: Exception) {
            ImportResult.Error(e.message ?: "未知错误")
        }
    }

    private suspend fun importOverwrite(
        exportData: ExportData,
        dateFormat: SimpleDateFormat,
        includeRecords: Boolean
    ): ImportResult.Success {
        // 覆盖导入：密码内容始终覆盖；仅当文件包含 records 字段时覆盖记录内容
        appDao.softDeleteAllActive()
        if (includeRecords) {
            recordDao.softDeleteAllActive()
        }

        var importedAppCount = 0
        var importedAccountCount = 0
        var importedRecordCount = 0

        for (exportApp in exportData.apps.orEmpty()) {
            val appId = insertApp(
                AppEntity(
                    appName = exportApp.appName,
                    note = exportApp.note,
                    isPinned = exportApp.isPinned
                )
            )
            importedAppCount++

            for (exportAccount in exportApp.accounts.orEmpty()) {
                insertAccount(
                    AccountEntity(
                        appId = appId,
                        username = exportAccount.username,
                        password = exportAccount.password,
                        imageUri = exportAccount.imageUri,
                        note = exportAccount.note,
                        tags = exportAccount.tags?.joinToString(","),
                        updatedAt = parseTimestamp(exportAccount.updatedAt, dateFormat)
                    )
                )
                importedAccountCount++
            }
        }

        if (includeRecords) {
            for (exportRecord in exportData.records.orEmpty()) {
                val recordContent = exportRecord.content ?: continue
                if (exportRecord.title.isNullOrBlank() && recordContent.isBlank()) continue
                insertRecord(
                    RecordEntity(
                        title = exportRecord.title,
                        content = recordContent,
                        isCollapsed = exportRecord.isCollapsed,
                        createdAt = parseTimestamp(exportRecord.createdAt, dateFormat),
                        updatedAt = parseTimestamp(exportRecord.updatedAt, dateFormat)
                    )
                )
                importedRecordCount++
            }
        }

        return ImportResult.Success(importedAppCount, importedAccountCount, importedRecordCount)
    }

    private suspend fun importMerge(
        exportData: ExportData,
        dateFormat: SimpleDateFormat,
        includeRecords: Boolean
    ): ImportResult.Success {
        // 合并导入：同名应用下追加账号；没有应用则新建应用；记录在文件包含 records 字段时追加
        var createdAppCount = 0
        var addedAccountCount = 0
        var addedRecordCount = 0

        for (exportApp in exportData.apps.orEmpty()) {
            val existingApp = appDao.getActiveAppByName(exportApp.appName)
            val targetAppId = if (existingApp != null) {
                existingApp.id
            } else {
                createdAppCount++
                insertApp(
                    AppEntity(
                        appName = exportApp.appName,
                        note = exportApp.note,
                        isPinned = exportApp.isPinned
                    )
                )
            }

            for (exportAccount in exportApp.accounts.orEmpty()) {
                insertAccount(
                    AccountEntity(
                        appId = targetAppId,
                        username = exportAccount.username,
                        password = exportAccount.password,
                        imageUri = exportAccount.imageUri,
                        note = exportAccount.note,
                        tags = exportAccount.tags?.joinToString(","),
                        updatedAt = parseTimestamp(exportAccount.updatedAt, dateFormat)
                    )
                )
                addedAccountCount++
            }
        }

        if (includeRecords) {
            for (exportRecord in exportData.records.orEmpty()) {
                val recordContent = exportRecord.content ?: continue
                if (exportRecord.title.isNullOrBlank() && recordContent.isBlank()) continue
                insertRecord(
                    RecordEntity(
                        title = exportRecord.title,
                        content = recordContent,
                        isCollapsed = exportRecord.isCollapsed,
                        createdAt = parseTimestamp(exportRecord.createdAt, dateFormat),
                        updatedAt = parseTimestamp(exportRecord.updatedAt, dateFormat)
                    )
                )
                addedRecordCount++
            }
        }

        return ImportResult.Success(createdAppCount, addedAccountCount, addedRecordCount)
    }

    private fun parseTimestamp(raw: String?, dateFormat: SimpleDateFormat): Long {
        if (raw.isNullOrBlank()) return System.currentTimeMillis()
        return try {
            dateFormat.parse(raw)?.time ?: System.currentTimeMillis()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }

    private suspend fun decryptAccountForRead(
        account: AccountEntity,
        migrateIfPlain: Boolean
    ): AccountEntity {
        val password = account.password
        if (password.isEmpty()) return account

        return if (passwordCipher.isEncrypted(password)) {
            account.copy(password = decryptPasswordForRead(password))
        } else {
            if (migrateIfPlain) {
                accountDao.updateAccount(
                    account.copy(password = passwordCipher.encrypt(password))
                )
            }
            account
        }
    }

    private fun encryptAccountForStorage(account: AccountEntity): AccountEntity {
        val password = account.password
        if (password.isEmpty() || passwordCipher.isEncrypted(password)) {
            return account
        }
        return account.copy(password = passwordCipher.encrypt(password))
    }

    private fun decryptPasswordForRead(value: String): String {
        return if (!passwordCipher.isEncrypted(value)) {
            value
        } else {
            runCatching { passwordCipher.decrypt(value) }.getOrElse { value }
        }
    }

    // ========== 导入/导出数据结构 ==========

    data class ExportData(
        val version: Int,
        val exportedAt: String,
        val apps: List<ExportApp>? = emptyList(),
        val records: List<ExportRecord>? = emptyList()
    )

    data class ExportApp(
        val appName: String,
        val note: String?,
        val isPinned: Boolean = false,
        val accounts: List<ExportAccount>? = emptyList()
    )

    data class ExportAccount(
        val username: String,
        val password: String,
        val imageUri: String? = null,
        val note: String?,
        val tags: List<String>?,
        val updatedAt: String
    )

    data class ExportRecord(
        val title: String?,
        val content: String?,
        val isCollapsed: Boolean = false,
        val createdAt: String?,
        val updatedAt: String?
    )

    enum class ImportMode {
        OVERWRITE,
        MERGE
    }

    sealed class ImportResult {
        data class Success(
            val appCount: Int,
            val accountCount: Int,
            val recordCount: Int
        ) : ImportResult()
        data class Error(val message: String) : ImportResult()
    }

    companion object {
        @Volatile
        private var INSTANCE: TPRepository? = null

        fun getInstance(context: Context): TPRepository {
            return INSTANCE ?: synchronized(this) {
                val db = AppDatabase.getDatabase(context)
                val instance = TPRepository(
                    db.appDao(),
                    db.accountDao(),
                    db.recordDao(),
                    KeystoreAesGcmPasswordCipher()
                )
                INSTANCE = instance
                instance
            }
        }
    }
}
