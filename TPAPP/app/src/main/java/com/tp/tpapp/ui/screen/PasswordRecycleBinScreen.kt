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
import com.tp.tpapp.data.model.AppEntity
import com.tp.tpapp.ui.viewmodel.PasswordRecycleBinViewModel
import com.tp.tpapp.ui.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordRecycleBinScreen(
    onNavigateBack: () -> Unit,
    viewModel: PasswordRecycleBinViewModel = viewModel(),
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val deletedApps by viewModel.deletedApps.collectAsState()
    val needConfirm by settingsViewModel.passwordDeleteConfirm.collectAsState()

    var pendingDeleteAppId by remember { mutableStateOf<Long?>(null) }
    var showEmptyConfirm by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("密码回收站") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    if (deletedApps.isNotEmpty()) {
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
        if (deletedApps.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "密码回收站为空",
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
                    items = deletedApps,
                    key = { it.id }
                ) { app ->
                    DeletedAppItem(
                        app = app,
                        onRestore = { viewModel.restoreApp(app.id) },
                        onHardDelete = {
                            if (needConfirm) {
                                pendingDeleteAppId = app.id
                            } else {
                                viewModel.hardDeleteApp(app.id)
                            }
                        }
                    )
                }
            }
        }
    }

    if (pendingDeleteAppId != null) {
        AlertDialog(
            onDismissRequest = { pendingDeleteAppId = null },
            title = { Text("确认彻底删除") },
            text = { Text("彻底删除后将无法恢复，是否继续？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.hardDeleteApp(pendingDeleteAppId ?: return@TextButton)
                        pendingDeleteAppId = null
                    }
                ) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteAppId = null }) {
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
private fun DeletedAppItem(
    app: AppEntity,
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = app.appName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                if (app.isPinned) {
                    Text(
                        text = "置顶",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            if (!app.note.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = app.note,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

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
