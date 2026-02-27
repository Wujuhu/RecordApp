package com.tp.tpapp.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tp.tpapp.data.TPRepository
import com.tp.tpapp.data.model.RecordEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RecycleBinViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = TPRepository.getInstance(application)

    val deletedRecords: StateFlow<List<RecordEntity>> = repository.getDeletedRecords()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun restoreRecord(id: Long) {
        viewModelScope.launch {
            repository.restoreRecord(id)
        }
    }

    fun hardDeleteRecord(id: Long) {
        viewModelScope.launch {
            repository.hardDeleteRecord(id)
        }
    }

    fun emptyTrash() {
        viewModelScope.launch {
            repository.emptyTrash()
        }
    }
}
