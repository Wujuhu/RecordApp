package com.tp.tpapp.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tp.tpapp.data.TPRepository
import com.tp.tpapp.data.model.AccountEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AccountEditViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = TPRepository.getInstance(application)

    private val _username = MutableStateFlow("")
    val username: StateFlow<String> = _username.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _note = MutableStateFlow("")
    val note: StateFlow<String> = _note.asStateFlow()

    private val _tags = MutableStateFlow("")
    val tags: StateFlow<String> = _tags.asStateFlow()

    private var editingAccountId: Long? = null
    private var appId: Long = 0

    fun init(appId: Long, accountId: Long?) {
        this.appId = appId
        if (accountId != null && accountId > 0) {
            editingAccountId = accountId
            viewModelScope.launch {
                repository.getAccountById(accountId).collect { account ->
                    if (account != null) {
                        _username.value = account.username
                        _password.value = account.password
                        _note.value = account.note ?: ""
                        _tags.value = account.tags ?: ""
                    }
                }
            }
        }
    }

    fun onUsernameChange(value: String) { _username.value = value }
    fun onPasswordChange(value: String) { _password.value = value }
    fun onNoteChange(value: String) { _note.value = value }
    fun onTagsChange(value: String) { _tags.value = value }

    fun saveAccount(onComplete: () -> Unit) {
        viewModelScope.launch {
            val account = AccountEntity(
                id = editingAccountId ?: 0,
                appId = appId,
                username = _username.value,
                password = _password.value,
                note = _note.value.ifBlank { null },
                tags = _tags.value.ifBlank { null },
                updatedAt = System.currentTimeMillis()
            )
            if (editingAccountId != null) {
                repository.updateAccount(account)
            } else {
                repository.insertAccount(account)
            }
            onComplete()
        }
    }

    fun generatePassword(
        length: Int = 16,
        includeUppercase: Boolean = true,
        includeLowercase: Boolean = true,
        includeDigits: Boolean = true,
        includeSpecial: Boolean = true
    ): String {
        val chars = buildString {
            if (includeLowercase) append("abcdefghijklmnopqrstuvwxyz")
            if (includeUppercase) append("ABCDEFGHIJKLMNOPQRSTUVWXYZ")
            if (includeDigits) append("0123456789")
            if (includeSpecial) append("!@#\$%^&*()_+-=[]{}|;:,.<>?")
        }
        if (chars.isEmpty()) return ""

        val password = (1..length).map { chars.random() }.joinToString("")
        _password.value = password
        return password
    }
}
