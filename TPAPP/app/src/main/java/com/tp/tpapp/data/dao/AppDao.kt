package com.tp.tpapp.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.tp.tpapp.data.model.AppEntity
import com.tp.tpapp.data.model.AppWithAccounts
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {

    @Query("SELECT * FROM apps WHERE isDeleted = 0 ORDER BY isPinned DESC, sortOrder ASC, appName ASC")
    fun getAllApps(): Flow<List<AppEntity>>

    @Query("SELECT * FROM apps WHERE id = :id AND isDeleted = 0")
    fun getAppById(id: Long): Flow<AppEntity?>

    @Transaction
    @Query("SELECT * FROM apps WHERE id = :appId AND isDeleted = 0")
    fun getAppWithAccounts(appId: Long): Flow<AppWithAccounts?>

    @Query(
        "SELECT * FROM apps WHERE isDeleted = 0 AND (" +
                "appName LIKE '%' || :query || '%' OR note LIKE '%' || :query || '%') " +
                "ORDER BY isPinned DESC, sortOrder ASC, appName ASC"
    )
    fun searchApps(query: String): Flow<List<AppEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApp(app: AppEntity): Long

    @Update
    suspend fun updateApp(app: AppEntity)

    @Update
    suspend fun updateApps(apps: List<AppEntity>)

    @Delete
    suspend fun deleteApp(app: AppEntity)

    @Query("SELECT * FROM apps WHERE isDeleted = 1 ORDER BY sortOrder ASC, appName ASC")
    fun getDeletedApps(): Flow<List<AppEntity>>

    @Query("SELECT * FROM apps WHERE isDeleted = 0 AND appName = :appName LIMIT 1")
    suspend fun getActiveAppByName(appName: String): AppEntity?

    @Query("UPDATE apps SET isDeleted = 1 WHERE id = :id")
    suspend fun softDelete(id: Long)

    @Query("UPDATE apps SET isDeleted = 1 WHERE isDeleted = 0")
    suspend fun softDeleteAllActive()

    @Query("UPDATE apps SET isDeleted = 0 WHERE id = :id")
    suspend fun restore(id: Long)

    @Query("DELETE FROM apps WHERE id = :id")
    suspend fun hardDelete(id: Long)

    @Query("DELETE FROM apps WHERE isDeleted = 1")
    suspend fun emptyTrash()

    @Query("DELETE FROM apps")
    suspend fun deleteAllApps()

    @Transaction
    @Query("SELECT * FROM apps WHERE isDeleted = 0 ORDER BY isPinned DESC, sortOrder ASC, appName ASC")
    fun getAllAppsWithAccounts(): Flow<List<AppWithAccounts>>

    @Query("SELECT COALESCE(MAX(sortOrder), 0) + 1 FROM apps WHERE isDeleted = 0")
    suspend fun getNextSortOrder(): Int
}
