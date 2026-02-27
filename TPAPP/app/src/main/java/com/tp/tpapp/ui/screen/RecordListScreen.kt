package com.tp.tpapp.ui.screen

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.UnfoldLess
import androidx.compose.material.icons.filled.UnfoldMore
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tp.tpapp.data.model.RecordEntity
import com.tp.tpapp.ui.viewmodel.RecordListViewModel
import com.tp.tpapp.ui.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordListScreen(
    modifier: Modifier = Modifier,
    onEditRecord: (Long) -> Unit,
    onOpenRecycleBin: () -> Unit,
    onOpenSettings: () -> Unit,
    viewModel: RecordListViewModel = viewModel(),
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val records by viewModel.records.collectAsState()
    val navigateToNewRecord by viewModel.navigateToNewRecord.collectAsState()

    val fontSizeSetting by settingsViewModel.fontSize.collectAsState()
    val maxLines by settingsViewModel.maxLines.collectAsState()
    val showIndex by settingsViewModel.showIndex.collectAsState()
    val needDeleteConfirm by settingsViewModel.recordDeleteConfirm.collectAsState()

    val contentFontSize = when (fontSizeSetting) {
        0 -> 12.sp
        1 -> 14.sp
        else -> 16.sp
    }

    LaunchedEffect(navigateToNewRecord) {
        navigateToNewRecord?.let { recordId ->
            onEditRecord(recordId)
            viewModel.clearNavigateToNewRecord()
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("记录") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
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
                onClick = { viewModel.addRecord() },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "新建记录")
            }
        }
    ) { innerPadding ->
        if (records.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "暂无记录",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "点击右下角按钮添加第一条记录",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                itemsIndexed(
                    items = records,
                    key = { _, record -> record.id }
                ) { index, record ->
                    SwipeToDismissRecordItem(
                        record = record,
                        index = index + 1,
                        showIndex = showIndex,
                        requireDeleteConfirm = needDeleteConfirm,
                        contentFontSize = contentFontSize,
                        maxLines = maxLines,
                        onClick = { onEditRecord(record.id) },
                        onDelete = { viewModel.softDeleteRecord(record.id) },
                        onToggleCollapse = { viewModel.toggleCollapse(record.id, !record.isCollapsed) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToDismissRecordItem(
    record: RecordEntity,
    index: Int,
    showIndex: Boolean,
    requireDeleteConfirm: Boolean,
    contentFontSize: TextUnit,
    maxLines: Int,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onToggleCollapse: () -> Unit
) {
    val showDeleteConfirm = remember { mutableStateOf(false) }
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                if (requireDeleteConfirm) {
                    showDeleteConfirm.value = true
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
        RecordItemCard(
            record = record,
            index = index,
            showIndex = showIndex,
            contentFontSize = contentFontSize,
            maxLines = maxLines,
            onClick = onClick,
            onToggleCollapse = onToggleCollapse
        )
    }

    if (showDeleteConfirm.value) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm.value = false },
            title = { Text("确认删除") },
            text = { Text("确认将该记录移入回收站吗？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteConfirm.value = false
                    }
                ) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm.value = false }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
private fun RecordItemCard(
    record: RecordEntity,
    index: Int,
    showIndex: Boolean,
    contentFontSize: TextUnit,
    maxLines: Int,
    onClick: () -> Unit,
    onToggleCollapse: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            if (!record.title.isNullOrBlank()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (showIndex) {
                        Text(
                            text = "$index.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = record.title,
                        fontSize = contentFontSize,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onToggleCollapse, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = if (record.isCollapsed) Icons.Default.UnfoldMore else Icons.Default.UnfoldLess,
                            contentDescription = if (record.isCollapsed) "展开" else "折叠",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                if (record.content.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = record.content,
                        fontSize = contentFontSize,
                        maxLines = if (record.isCollapsed) maxLines else Int.MAX_VALUE,
                        overflow = if (record.isCollapsed) TextOverflow.Ellipsis else TextOverflow.Clip,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    if (showIndex) {
                        Text(
                            text = "$index.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = record.content,
                        fontSize = contentFontSize,
                        maxLines = if (record.isCollapsed) maxLines else Int.MAX_VALUE,
                        overflow = if (record.isCollapsed) TextOverflow.Ellipsis else TextOverflow.Clip,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onToggleCollapse, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = if (record.isCollapsed) Icons.Default.UnfoldMore else Icons.Default.UnfoldLess,
                            contentDescription = if (record.isCollapsed) "展开" else "折叠",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}
