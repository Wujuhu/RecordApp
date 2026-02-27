package com.tp.tpapp.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tp.tpapp.data.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SettingsRepository.getInstance(application)

    val themeMode: StateFlow<Int> = repository.themeModeFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), -1)

    val fontSize: StateFlow<Int> = repository.fontSizeFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1)

    val maxLines: StateFlow<Int> = repository.maxLinesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 5)

    val showIndex: StateFlow<Boolean> = repository.showIndexFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val recordDeleteConfirm: StateFlow<Boolean> = repository.recordDeleteConfirmFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val passwordDeleteConfirm: StateFlow<Boolean> = repository.passwordDeleteConfirmFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val startupTab: StateFlow<Int> = repository.startupTabFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsRepository.STARTUP_TAB_RECORD)

    fun setThemeMode(mode: Int) {
        viewModelScope.launch {
            repository.setThemeMode(mode)
        }
    }

    fun setFontSize(size: Int) {
        viewModelScope.launch {
            repository.setFontSize(size)
        }
    }

    fun setMaxLines(lines: Int) {
        viewModelScope.launch {
            repository.setMaxLines(lines)
        }
    }

    fun setShowIndex(show: Boolean) {
        viewModelScope.launch {
            repository.setShowIndex(show)
        }
    }

    fun setRecordDeleteConfirm(enabled: Boolean) {
        viewModelScope.launch {
            repository.setRecordDeleteConfirm(enabled)
        }
    }

    fun setPasswordDeleteConfirm(enabled: Boolean) {
        viewModelScope.launch {
            repository.setPasswordDeleteConfirm(enabled)
        }
    }

    fun setStartupTab(tab: Int) {
        viewModelScope.launch {
            repository.setStartupTab(tab)
        }
    }
}
