package com.tp.tpapp.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tp.tpapp.data.TPRepository
import com.tp.tpapp.data.model.RecordEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.InputStream
import java.io.OutputStream

class RecordListViewModel(application: Application) : AndroidViewModel(application) {

    enum class ImportMode {
        OVERWRITE,
        MERGE
    }

    private val repository = TPRepository.getInstance(application)

    val records: StateFlow<List<RecordEntity>> = repository.getActiveRecords()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 新增记录后自动导航
    private val _navigateToNewRecord = MutableStateFlow<Long?>(null)
    val navigateToNewRecord: StateFlow<Long?> = _navigateToNewRecord.asStateFlow()

    private val _importExportMessage = MutableStateFlow<String?>(null)
    val importExportMessage: StateFlow<String?> = _importExportMessage.asStateFlow()

    fun addRecord() {
        // 新建时不预先插入空记录，避免返回后残留空数据
        _navigateToNewRecord.value = 0L
    }

    fun clearNavigateToNewRecord() {
        _navigateToNewRecord.value = null
    }

    fun softDeleteRecord(id: Long) {
        viewModelScope.launch {
            repository.softDeleteRecord(id)
        }
    }

    fun toggleCollapse(id: Long, isCollapsed: Boolean) {
        viewModelScope.launch {
            repository.toggleCollapse(id, isCollapsed)
        }
    }

    fun reorderRecords(reorderedRecords: List<RecordEntity>) {
        viewModelScope.launch {
            repository.reorderRecords(reorderedRecords)
        }
    }

    fun exportData(outputStream: OutputStream) {
        viewModelScope.launch {
            try {
                repository.exportToJson(outputStream)
                _importExportMessage.value = "导出成功（含记录和密码）"
            } catch (e: Exception) {
                _importExportMessage.value = "导出失败: ${e.message}"
            }
        }
    }

    fun importData(inputStream: InputStream, mode: ImportMode) {
        viewModelScope.launch {
            val repositoryMode = when (mode) {
                ImportMode.OVERWRITE -> TPRepository.ImportMode.OVERWRITE
                ImportMode.MERGE -> TPRepository.ImportMode.MERGE
            }
            val result = repository.importFromJson(inputStream, repositoryMode)
            _importExportMessage.value = when (result) {
                is TPRepository.ImportResult.Success -> when (mode) {
                    ImportMode.OVERWRITE ->
                        "覆盖导入成功: ${result.recordCount} 条记录, ${result.appCount} 个应用, ${result.accountCount} 个账号；本地原有内容已移入回收站"
                    ImportMode.MERGE ->
                        "合并导入成功: 新增 ${result.recordCount} 条记录, 新增 ${result.appCount} 个应用, 新增 ${result.accountCount} 个账号"
                }
                is TPRepository.ImportResult.Error ->
                    "导入失败: ${result.message}"
            }
        }
    }

    fun clearImportExportMessage() {
        _importExportMessage.value = null
    }
}
