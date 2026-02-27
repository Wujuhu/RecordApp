package com.tp.tpapp.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tp.tpapp.data.TPRepository
import com.tp.tpapp.data.model.AppEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PasswordRecycleBinViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = TPRepository.getInstance(application)

    val deletedApps: StateFlow<List<AppEntity>> = repository.getDeletedApps()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun restoreApp(id: Long) {
        viewModelScope.launch {
            repository.restoreApp(id)
        }
    }

    fun hardDeleteApp(id: Long) {
        viewModelScope.launch {
            repository.hardDeleteApp(id)
        }
    }

    fun emptyTrash() {
        viewModelScope.launch {
            repository.emptyAppTrash()
        }
    }
}
