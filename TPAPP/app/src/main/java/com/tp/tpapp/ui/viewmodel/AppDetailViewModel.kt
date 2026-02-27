package com.tp.tpapp.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tp.tpapp.data.TPRepository
import com.tp.tpapp.data.model.AccountEntity
import com.tp.tpapp.data.model.AppWithAccounts
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AppDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = TPRepository.getInstance(application)

    private val _appWithAccounts = MutableStateFlow<AppWithAccounts?>(null)
    val appWithAccounts: StateFlow<AppWithAccounts?> = _appWithAccounts.asStateFlow()

    private val _showEditAppDialog = MutableStateFlow(false)
    val showEditAppDialog: StateFlow<Boolean> = _showEditAppDialog.asStateFlow()

    fun loadApp(appId: Long) {
        viewModelScope.launch {
            repository.getAppWithAccounts(appId).collect { data ->
                _appWithAccounts.value = data
            }
        }
    }

    fun showEditDialog() {
        _showEditAppDialog.value = true
    }

    fun hideEditDialog() {
        _showEditAppDialog.value = false
    }

    fun updateApp(appName: String, note: String?) {
        val currentApp = _appWithAccounts.value?.app ?: return
        viewModelScope.launch {
            repository.updateApp(
                currentApp.copy(appName = appName, note = note)
            )
            _showEditAppDialog.value = false
        }
    }

    fun deleteAccount(account: AccountEntity) {
        viewModelScope.launch {
            repository.deleteAccount(account)
        }
    }

    fun reorderAccounts(reorderedAccounts: List<AccountEntity>) {
        viewModelScope.launch {
            repository.reorderAccounts(reorderedAccounts)
        }
    }
}
