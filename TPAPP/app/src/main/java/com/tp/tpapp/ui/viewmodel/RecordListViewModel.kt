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

class RecordListViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = TPRepository.getInstance(application)

    val records: StateFlow<List<RecordEntity>> = repository.getActiveRecords()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 新增记录后自动导航
    private val _navigateToNewRecord = MutableStateFlow<Long?>(null)
    val navigateToNewRecord: StateFlow<Long?> = _navigateToNewRecord.asStateFlow()

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
}
