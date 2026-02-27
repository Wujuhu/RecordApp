package com.tp.tpapp.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tp.tpapp.data.TPRepository
import com.tp.tpapp.data.model.RecordEntity
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class RecordEditViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = TPRepository.getInstance(application)

    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title.asStateFlow()

    private val _content = MutableStateFlow("")
    val content: StateFlow<String> = _content.asStateFlow()

    private var recordId: Long = 0
    private var currentRecord: RecordEntity? = null
    private var loadJob: Job? = null

    fun loadRecord(id: Long) {
        recordId = id
        loadJob?.cancel()

        if (id <= 0) {
            currentRecord = null
            _title.value = ""
            _content.value = ""
            return
        }

        loadJob = viewModelScope.launch {
            repository.getRecordById(id).collectLatest { record ->
                if (record != null) {
                    currentRecord = record
                    _title.value = record.title ?: ""
                    _content.value = record.content
                }
            }
        }
    }

    fun onTitleChange(value: String) { _title.value = value }
    fun onContentChange(value: String) { _content.value = value }

    fun saveRecord(onComplete: () -> Unit) {
        viewModelScope.launch {
            val titleBlank = _title.value.isBlank()
            val contentBlank = _content.value.isBlank()

            // 标题和内容都为空时，不保留该记录
            if (titleBlank && contentBlank) {
                if (recordId > 0) {
                    repository.hardDeleteRecord(recordId)
                }
                onComplete()
                return@launch
            }

            val now = System.currentTimeMillis()
            val titleValue = _title.value.ifBlank { null }
            val contentValue = _content.value

            if (recordId > 0) {
                val base = currentRecord ?: RecordEntity(id = recordId, content = "")
                repository.updateRecord(
                    base.copy(
                        title = titleValue,
                        content = contentValue,
                        updatedAt = now
                    )
                )
            } else {
                repository.insertRecord(
                    RecordEntity(
                        title = titleValue,
                        content = contentValue,
                        updatedAt = now
                    )
                )
            }
            onComplete()
        }
    }
}
