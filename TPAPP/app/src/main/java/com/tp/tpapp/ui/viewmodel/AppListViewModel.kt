package com.tp.tpapp.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tp.tpapp.data.TPRepository
import com.tp.tpapp.data.model.AppEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.InputStream
import java.io.OutputStream

class AppListViewModel(application: Application) : AndroidViewModel(application) {

    enum class ImportMode {
        OVERWRITE,
        MERGE
    }

    private val repository = TPRepository.getInstance(application)

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val apps: StateFlow<List<AppEntity>> = _searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) {
                repository.getAllApps()
            } else {
                repository.searchApps(query)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _showAddDialog = MutableStateFlow(false)
    val showAddDialog: StateFlow<Boolean> = _showAddDialog.asStateFlow()

    // 新建应用后自动跳转到新增账号页
    private val _navigateToNewApp = MutableStateFlow<Long?>(null)
    val navigateToNewApp: StateFlow<Long?> = _navigateToNewApp.asStateFlow()

    // 导入/导出提示
    private val _importExportMessage = MutableStateFlow<String?>(null)
    val importExportMessage: StateFlow<String?> = _importExportMessage.asStateFlow()

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun showAddAppDialog() {
        _showAddDialog.value = true
    }

    fun hideAddAppDialog() {
        _showAddDialog.value = false
    }

    fun addApp(appName: String, note: String?) {
        viewModelScope.launch {
            val appId = repository.insertApp(
                AppEntity(appName = appName, note = note)
            )
            _showAddDialog.value = false
            _navigateToNewApp.value = appId
        }
    }

    fun clearNavigateToNewApp() {
        _navigateToNewApp.value = null
    }

    fun deleteApp(app: AppEntity) {
        viewModelScope.launch {
            repository.softDeleteApp(app.id)
        }
    }

    fun reorderApps(reorderedApps: List<AppEntity>) {
        viewModelScope.launch {
            repository.reorderApps(reorderedApps)
        }
    }

    fun pinAppToTop(app: AppEntity) {
        viewModelScope.launch {
            val currentApps = repository.getAllApps().first().toMutableList()
            val index = currentApps.indexOfFirst { it.id == app.id }
            if (index < 0) return@launch

            val targetApp = currentApps[index]
            val pinnedApp = if (targetApp.isPinned) targetApp else targetApp.copy(isPinned = true)

            currentApps.removeAt(index)
            val pinnedGroupStart = currentApps.indexOfFirst { it.isPinned }
                .takeIf { it >= 0 } ?: 0
            currentApps.add(pinnedGroupStart, pinnedApp)

            repository.reorderApps(currentApps)
        }
    }

    fun moveAppUp(app: AppEntity) {
        viewModelScope.launch {
            val currentApps = repository.getAllApps().first().toMutableList()
            val index = currentApps.indexOfFirst { it.id == app.id }
            if (index <= 0) return@launch

            val movingApp = currentApps[index]
            val previousIndex = (index - 1 downTo 0)
                .firstOrNull { currentApps[it].isPinned == movingApp.isPinned }
                ?: return@launch

            currentApps[index] = currentApps[previousIndex].also { currentApps[previousIndex] = movingApp }
            repository.reorderApps(currentApps)
        }
    }

    fun moveAppDown(app: AppEntity) {
        viewModelScope.launch {
            val currentApps = repository.getAllApps().first().toMutableList()
            val index = currentApps.indexOfFirst { it.id == app.id }
            if (index < 0) return@launch

            val movingApp = currentApps[index]
            val nextIndex = ((index + 1) until currentApps.size)
                .firstOrNull { currentApps[it].isPinned == movingApp.isPinned }
                ?: return@launch

            currentApps[index] = currentApps[nextIndex].also { currentApps[nextIndex] = movingApp }
            repository.reorderApps(currentApps)
        }
    }

    fun moveAppToTop(app: AppEntity) {
        viewModelScope.launch {
            val currentApps = repository.getAllApps().first().toMutableList()
            val index = currentApps.indexOfFirst { it.id == app.id }
            if (index < 0) return@launch

            val movingApp = currentApps[index]
            val groupStart = currentApps.indexOfFirst { it.isPinned == movingApp.isPinned }
            if (groupStart < 0 || groupStart == index) return@launch

            currentApps.removeAt(index)
            currentApps.add(groupStart, movingApp)
            repository.reorderApps(currentApps)
        }
    }

    fun moveAppToBottom(app: AppEntity) {
        viewModelScope.launch {
            val currentApps = repository.getAllApps().first().toMutableList()
            val index = currentApps.indexOfFirst { it.id == app.id }
            if (index < 0) return@launch

            val movingApp = currentApps[index]
            val groupEnd = currentApps.indexOfLast { it.isPinned == movingApp.isPinned }
            if (groupEnd < 0 || groupEnd == index) return@launch

            currentApps.removeAt(index)
            currentApps.add(groupEnd, movingApp)
            repository.reorderApps(currentApps)
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
                        "覆盖导入成功: ${result.appCount} 个应用, ${result.accountCount} 个账号, ${result.recordCount} 条记录；本地原有内容已移入回收站"
                    ImportMode.MERGE ->
                        "合并导入成功: 新增 ${result.appCount} 个应用, 新增 ${result.accountCount} 个账号, 新增 ${result.recordCount} 条记录"
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
