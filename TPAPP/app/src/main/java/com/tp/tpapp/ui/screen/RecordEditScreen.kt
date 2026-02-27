package com.tp.tpapp.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tp.tpapp.ui.viewmodel.RecordEditViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordEditScreen(
    recordId: Long,
    onNavigateBack: () -> Unit,
    viewModel: RecordEditViewModel = viewModel()
) {
    val title by viewModel.title.collectAsState()
    val content by viewModel.content.collectAsState()
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(recordId) {
        viewModel.loadRecord(recordId)
    }

    // 自动弹出键盘
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("编辑记录") },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.saveRecord { onNavigateBack() }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回并保存")
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // 标题（可选）
            OutlinedTextField(
                value = title,
                onValueChange = { viewModel.onTitleChange(it) },
                label = { Text("标题（可选）") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 正文
            OutlinedTextField(
                value = content,
                onValueChange = { viewModel.onContentChange(it) },
                label = { Text("正文") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .focusRequester(focusRequester),
                maxLines = Int.MAX_VALUE
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 保存按钮
            Button(
                onClick = { viewModel.saveRecord { onNavigateBack() } },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.padding(4.dp))
                Text("保存")
            }
        }
    }
}
