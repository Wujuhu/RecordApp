package com.tp.tpapp.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tp.tpapp.data.SettingsRepository
import com.tp.tpapp.ui.viewmodel.SettingsViewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = viewModel()
) {
    val themeMode by viewModel.themeMode.collectAsState()
    val fontSize by viewModel.fontSize.collectAsState()
    val maxLines by viewModel.maxLines.collectAsState()
    val showIndex by viewModel.showIndex.collectAsState()
    val recordDeleteConfirm by viewModel.recordDeleteConfirm.collectAsState()
    val passwordDeleteConfirm by viewModel.passwordDeleteConfirm.collectAsState()
    val startupTab by viewModel.startupTab.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            SettingsSectionTitle("启动默认页面")
            Column(Modifier.selectableGroup()) {
                SettingRadioRow(
                    text = "记录",
                    selected = startupTab == SettingsRepository.STARTUP_TAB_RECORD,
                    onClick = { viewModel.setStartupTab(SettingsRepository.STARTUP_TAB_RECORD) }
                )
                SettingRadioRow(
                    text = "密码",
                    selected = startupTab == SettingsRepository.STARTUP_TAB_PASSWORD,
                    onClick = { viewModel.setStartupTab(SettingsRepository.STARTUP_TAB_PASSWORD) }
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            SettingsSectionTitle("删除确认")
            SettingSwitchRow(
                title = "记录删除二次确认",
                description = "关闭后，记录删除将直接执行",
                checked = recordDeleteConfirm,
                onCheckedChange = viewModel::setRecordDeleteConfirm
            )
            SettingSwitchRow(
                title = "密码删除二次确认",
                description = "关闭后，密码相关删除将直接执行",
                checked = passwordDeleteConfirm,
                onCheckedChange = viewModel::setPasswordDeleteConfirm
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            SettingsSectionTitle("主题模式")
            val themeOptions = listOf(
                "跟随系统" to -1,
                "浅色模式" to 0,
                "深色模式" to 1
            )
            Column(Modifier.selectableGroup()) {
                themeOptions.forEach { (text, value) ->
                    SettingRadioRow(
                        text = text,
                        selected = themeMode == value,
                        onClick = { viewModel.setThemeMode(value) }
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            SettingsSectionTitle("记录文字大小")
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                val fontSizeText = when (fontSize) {
                    0 -> "小"
                    1 -> "中（默认）"
                    else -> "大"
                }
                Text("当前字号: $fontSizeText", style = MaterialTheme.typography.bodyMedium)
                Slider(
                    value = fontSize.toFloat(),
                    onValueChange = { viewModel.setFontSize(it.roundToInt()) },
                    valueRange = 0f..2f,
                    steps = 1
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            SettingsSectionTitle("记录列表最大预览行数")
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Text("当前行数: $maxLines 行", style = MaterialTheme.typography.bodyMedium)
                Slider(
                    value = maxLines.toFloat(),
                    onValueChange = { viewModel.setMaxLines(it.roundToInt()) },
                    valueRange = 1f..10f,
                    steps = 8
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            SettingsSectionTitle("记录列表显示序号")
            SettingSwitchRow(
                title = "显示序号",
                description = "在记录列表左侧显示 1、2、3",
                checked = showIndex,
                onCheckedChange = viewModel::setShowIndex
            )
        }
    }
}

@Composable
private fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        color = MaterialTheme.colorScheme.primary,
        style = MaterialTheme.typography.labelLarge,
        modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 8.dp)
    )
}

@Composable
private fun SettingRadioRow(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton
            )
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = null
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = text, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun SettingSwitchRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
