package com.tp.tpapp.ui.screen

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.tp.tpapp.ui.component.GeneratorPreset
import com.tp.tpapp.ui.component.PasswordGeneratorDialog
import com.tp.tpapp.ui.viewmodel.AccountEditViewModel
import kotlinx.coroutines.delay

private enum class GeneratorTarget {
    USERNAME,
    PASSWORD
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountEditScreen(
    appId: Long,
    accountId: Long?,
    onNavigateBack: () -> Unit,
    viewModel: AccountEditViewModel = viewModel()
) {
    val username by viewModel.username.collectAsState()
    val password by viewModel.password.collectAsState()
    val imageUri by viewModel.imageUri.collectAsState()
    val note by viewModel.note.collectAsState()
    val tags by viewModel.tags.collectAsState()
    val context = LocalContext.current

    var showPassword by remember { mutableStateOf(false) }
    var showGenerator by remember { mutableStateOf(false) }
    var generatorTarget by remember { mutableStateOf(GeneratorTarget.PASSWORD) }
    val focusRequester = remember { FocusRequester() }
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            viewModel.onImageUriChange(it.toString())
        }
    }

    val isEditing = accountId != null

    LaunchedEffect(appId, accountId) {
        viewModel.init(appId, accountId)
        if (!isEditing) {
            delay(100)
            focusRequester.requestFocus()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "编辑账号" else "添加账号") },
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            OutlinedTextField(
                value = username,
                onValueChange = { viewModel.onUsernameChange(it) },
                label = { Text("用户名 / 账号") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                trailingIcon = {
                    IconButton(onClick = {
                        generatorTarget = GeneratorTarget.USERNAME
                        showGenerator = true
                    }) {
                        Icon(
                            imageVector = Icons.Default.Casino,
                            contentDescription = "生成随机账号"
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { viewModel.onPasswordChange(it) },
                label = { Text("密码") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (showPassword) {
                    androidx.compose.ui.text.input.VisualTransformation.None
                } else {
                    androidx.compose.ui.text.input.PasswordVisualTransformation()
                },
                trailingIcon = {
                    Row {
                        IconButton(onClick = { showPassword = !showPassword }) {
                            Icon(
                                if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (showPassword) "隐藏密码" else "显示密码"
                            )
                        }
                        IconButton(onClick = {
                            generatorTarget = GeneratorTarget.PASSWORD
                            showGenerator = true
                        }) {
                            Icon(
                                imageVector = Icons.Default.Casino,
                                contentDescription = "生成随机密码"
                            )
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "账户图片（可选）",
                style = MaterialTheme.typography.labelLarge
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (!imageUri.isNullOrBlank()) {
                AsyncImage(
                    model = imageUri,
                    contentDescription = "账户图片",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = { imagePicker.launch(arrayOf("image/*")) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (imageUri.isNullOrBlank()) "选择图片" else "更换图片")
                }
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedButton(
                    onClick = { viewModel.onImageUriChange(null) },
                    enabled = !imageUri.isNullOrBlank(),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("清除图片")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = note,
                onValueChange = { viewModel.onNoteChange(it) },
                label = { Text("备注（可选）") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = tags,
                onValueChange = { viewModel.onTagsChange(it) },
                label = { Text("标签（可选，逗号分隔）") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { viewModel.saveAccount { onNavigateBack() } },
                modifier = Modifier.fillMaxWidth(),
                enabled = username.isNotBlank() && password.isNotBlank()
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.padding(4.dp))
                Text(if (isEditing) "保存修改" else "添加账号")
            }
        }
    }

    if (showGenerator) {
        PasswordGeneratorDialog(
            onDismiss = { showGenerator = false },
            onConfirm = { generatedText ->
                if (generatorTarget == GeneratorTarget.USERNAME) {
                    viewModel.onUsernameChange(generatedText)
                } else {
                    viewModel.onPasswordChange(generatedText)
                }
                showGenerator = false
            },
            preset = if (generatorTarget == GeneratorTarget.USERNAME) {
                GeneratorPreset.USERNAME
            } else {
                GeneratorPreset.PASSWORD
            }
        )
    }
}
