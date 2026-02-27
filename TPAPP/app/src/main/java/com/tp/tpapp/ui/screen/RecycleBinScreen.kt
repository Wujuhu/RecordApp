package com.tp.tpapp.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tp.tpapp.data.model.RecordEntity
import com.tp.tpapp.ui.viewmodel.RecycleBinViewModel
import com.tp.tpapp.ui.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecycleBinScreen(
    onNavigateBack: () -> Unit,
    viewModel: RecycleBinViewModel = viewModel(),
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val deletedRecords by viewModel.deletedRecords.collectAsState()
    val needConfirm by settingsViewModel.recordDeleteConfirm.collectAsState()
    var pendingDeleteRecordId by remember { mutableStateOf<Long?>(null) }
    var showEmptyConfirm by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("记录回收站") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    if (deletedRecords.isNotEmpty()) {
                        TextButton(
                            onClick = {
                                if (needConfirm) {
                                    showEmptyConfirm = true
                                } else {
                                    viewModel.emptyTrash()
                                }
                            }
                        ) {
                            Text("清空", color = MaterialTheme.colorScheme.error)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    titleContentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            )
        }
    ) { innerPadding ->
        if (deletedRecords.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "记录回收站为空",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(
                    items = deletedRecords,
                    key = { it.id }
                ) { record ->
                    DeletedRecordItem(
                        record = record,
                        onRestore = { viewModel.restoreRecord(record.id) },
                        onHardDelete = {
                            if (needConfirm) {
                                pendingDeleteRecordId = record.id
                            } else {
                                viewModel.hardDeleteRecord(record.id)
                            }
                        }
                    )
                }
            }
        }
    }

    if (pendingDeleteRecordId != null) {
        AlertDialog(
            onDismissRequest = { pendingDeleteRecordId = null },
            title = { Text("确认彻底删除") },
            text = { Text("彻底删除后将无法恢复，是否继续？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.hardDeleteRecord(pendingDeleteRecordId ?: return@TextButton)
                        pendingDeleteRecordId = null
                    }
                ) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteRecordId = null }) {
                    Text("取消")
                }
            }
        )
    }

    if (showEmptyConfirm) {
        AlertDialog(
            onDismissRequest = { showEmptyConfirm = false },
            title = { Text("确认清空回收站") },
            text = { Text("清空后将无法恢复，是否继续？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.emptyTrash()
                        showEmptyConfirm = false
                    }
                ) {
                    Text("清空", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEmptyConfirm = false }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
private fun DeletedRecordItem(
    record: RecordEntity,
    onRestore: () -> Unit,
    onHardDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            if (!record.title.isNullOrBlank()) {
                Text(
                    text = record.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            Text(
                text = record.content.ifBlank { "（空内容）" },
                style = MaterialTheme.typography.bodySmall,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onRestore) {
                    Icon(
                        Icons.Default.Restore,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    Text("恢复")
                }
                TextButton(onClick = onHardDelete) {
                    Icon(
                        Icons.Default.DeleteForever,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    Text("彻底删除", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
