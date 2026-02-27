package com.tp.tpapp.ui.screen

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.material3.RadioButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tp.tpapp.data.model.AppEntity
import com.tp.tpapp.ui.viewmodel.AppListViewModel
import com.tp.tpapp.ui.viewmodel.SettingsViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppListScreen(
    modifier: Modifier = Modifier,
    onAppClick: (Long) -> Unit,
    onNavigateToAccountEdit: (Long) -> Unit,
    onOpenRecycleBin: () -> Unit,
    onOpenSettings: () -> Unit,
    viewModel: AppListViewModel = viewModel(),
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val apps by viewModel.apps.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val showAddDialog by viewModel.showAddDialog.collectAsState()
    val importExportMessage by viewModel.importExportMessage.collectAsState()
    val navigateToNewApp by viewModel.navigateToNewApp.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val needDeleteConfirm by settingsViewModel.passwordDeleteConfirm.collectAsState()
    var showImportModeDialog by remember { mutableStateOf(false) }
    var selectedImportMode by remember { mutableStateOf(AppListViewModel.ImportMode.OVERWRITE) }

    LaunchedEffect(navigateToNewApp) {
        navigateToNewApp?.let { appId ->
            onNavigateToAccountEdit(appId)
            viewModel.clearNavigateToNewApp()
        }
    }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let {
            context.contentResolver.openOutputStream(it)?.let { stream ->
                viewModel.exportData(stream)
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            context.contentResolver.openInputStream(it)?.let { stream ->
                viewModel.importData(stream, selectedImportMode)
            }
        }
    }

    LaunchedEffect(importExportMessage) {
        importExportMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearImportExportMessage()
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("密码") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    IconButton(onClick = { exportLauncher.launch("tp_passwords.json") }) {
                        Icon(Icons.Default.FileUpload, contentDescription = "导出")
                    }
                    IconButton(onClick = { showImportModeDialog = true }) {
                        Icon(Icons.Default.FileDownload, contentDescription = "导入")
                    }
                    IconButton(onClick = onOpenRecycleBin) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = "回收站")
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "设置")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.showAddAppDialog() },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "添加应用")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChange(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("搜索应用、账号、备注...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true
            )

            if (apps.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (searchQuery.isBlank()) "暂无应用" else "未找到匹配的应用",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (searchQuery.isBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "点击右下角按钮添加第一个应用",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(
                        items = apps,
                        key = { it.id }
                    ) { app ->
                        SwipeToDismissAppItem(
                            app = app,
                            requireDeleteConfirm = needDeleteConfirm,
                            onClick = { onAppClick(app.id) },
                            onDelete = { viewModel.deleteApp(app) },
                            onPinToTop = { viewModel.pinAppToTop(app) },
                            onMoveUp = { viewModel.moveAppUp(app) },
                            onMoveDown = { viewModel.moveAppDown(app) },
                            onMoveToTop = { viewModel.moveAppToTop(app) },
                            onMoveToBottom = { viewModel.moveAppToBottom(app) }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddAppDialog(
            onDismiss = { viewModel.hideAddAppDialog() },
            onConfirm = { name, note -> viewModel.addApp(name, note) }
        )
    }

    if (showImportModeDialog) {
        AlertDialog(
            onDismissRequest = { showImportModeDialog = false },
            title = { Text("选择导入模式") },
            text = {
                Column {
                    ImportModeItem(
                        title = "覆盖",
                        description = "只保留导入文件内容，本地内容移入回收站",
                        selected = selectedImportMode == AppListViewModel.ImportMode.OVERWRITE,
                        onClick = { selectedImportMode = AppListViewModel.ImportMode.OVERWRITE }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ImportModeItem(
                        title = "合并",
                        description = "同名应用下追加账号；没有应用则新建",
                        selected = selectedImportMode == AppListViewModel.ImportMode.MERGE,
                        onClick = { selectedImportMode = AppListViewModel.ImportMode.MERGE }
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showImportModeDialog = false
                        importLauncher.launch(arrayOf("application/json"))
                    }
                ) {
                    Text("选择文件")
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportModeDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
private fun ImportModeItem(
    title: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton
            )
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        RadioButton(
            selected = selected,
            onClick = null
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToDismissAppItem(
    app: AppEntity,
    requireDeleteConfirm: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onPinToTop: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onMoveToTop: () -> Unit,
    onMoveToBottom: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                if (requireDeleteConfirm) {
                    showDeleteConfirm = true
                    false
                } else {
                    onDelete()
                    true
                }
            } else {
                false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val color by animateColorAsState(
                targetValue = if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart) {
                    MaterialTheme.colorScheme.errorContainer
                } else {
                    MaterialTheme.colorScheme.surface
                },
                label = "swipe_bg"
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "删除",
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        },
        enableDismissFromStartToEnd = false
    ) {
        AppItemCard(
            app = app,
            onClick = onClick,
            onPinToTop = onPinToTop,
            onMoveUp = onMoveUp,
            onMoveDown = onMoveDown,
            onMoveToTop = onMoveToTop,
            onMoveToBottom = onMoveToBottom
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("确认删除") },
            text = { Text("确认将该应用移入回收站吗？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteConfirm = false
                    }
                ) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("取消")
                }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AppItemCard(
    app: AppEntity,
    onClick: () -> Unit,
    onPinToTop: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onMoveToTop: () -> Unit,
    onMoveToBottom: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .combinedClickable(
                onClick = { onClick() },
                onLongClick = { showMenu = true }
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = app.appName.first().uppercase(),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = app.appName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (!app.note.isNullOrBlank()) {
                        Text(
                            text = app.note,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                if (app.isPinned) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "置顶",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text("上移") },
                    onClick = {
                        showMenu = false
                        onMoveUp()
                    }
                )
                DropdownMenuItem(
                    text = { Text("下移") },
                    onClick = {
                        showMenu = false
                        onMoveDown()
                    }
                )
                DropdownMenuItem(
                    text = { Text("上移至顶部") },
                    onClick = {
                        showMenu = false
                        onMoveToTop()
                    }
                )
                DropdownMenuItem(
                    text = { Text("下移至底部") },
                    onClick = {
                        showMenu = false
                        onMoveToBottom()
                    }
                )
                DropdownMenuItem(
                    text = { Text("置顶") },
                    onClick = {
                        showMenu = false
                        onPinToTop()
                    }
                )
            }
        }
    }
}

@Composable
private fun AddAppDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String?) -> Unit
) {
    var appName by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        delay(100)
        focusRequester.requestFocus()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("添加应用") },
        text = {
            Column {
                OutlinedTextField(
                    value = appName,
                    onValueChange = { appName = it },
                    label = { Text("应用名称") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("备注（可选）") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(appName, note.ifBlank { null }) },
                enabled = appName.isNotBlank()
            ) {
                Text("添加")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}
