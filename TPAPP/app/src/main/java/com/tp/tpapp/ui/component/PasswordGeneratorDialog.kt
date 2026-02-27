package com.tp.tpapp.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp

enum class GeneratorPreset {
    PASSWORD,
    USERNAME
}

private enum class FixedPartPosition {
    START,
    END,
    ANYWHERE
}

@Composable
fun PasswordGeneratorDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    preset: GeneratorPreset = GeneratorPreset.PASSWORD
) {
    var length by remember(preset) {
        mutableFloatStateOf(if (preset == GeneratorPreset.PASSWORD) 16f else 12f)
    }
    var includeUppercase by remember(preset) { mutableStateOf(true) }
    var includeLowercase by remember(preset) { mutableStateOf(true) }
    var includeDigits by remember(preset) { mutableStateOf(true) }
    var includeSpecial by remember(preset) {
        mutableStateOf(preset == GeneratorPreset.PASSWORD)
    }
    var fixedPart by remember { mutableStateOf("") }
    var fixedPartPosition by remember { mutableStateOf(FixedPartPosition.ANYWHERE) }
    var generatedText by remember { mutableStateOf("") }

    fun buildRandomSource(): String {
        return buildString {
            if (includeLowercase) append("abcdefghijklmnopqrstuvwxyz")
            if (includeUppercase) append("ABCDEFGHIJKLMNOPQRSTUVWXYZ")
            if (includeDigits) append("0123456789")
            if (includeSpecial) append("!@#\$%^&*()_+-=[]{}|;:,.<>?")
        }
    }

    fun generate() {
        val randomSource = buildRandomSource()
        val normalizedFixedPart = fixedPart
        val minLength = length.toInt().coerceAtLeast(1)
        val targetLength = maxOf(minLength, normalizedFixedPart.length)
        val randomLength = (targetLength - normalizedFixedPart.length).coerceAtLeast(0)

        val randomPart = if (randomLength == 0) {
            ""
        } else if (randomSource.isNotEmpty()) {
            (1..randomLength).map { randomSource.random() }.joinToString("")
        } else {
            ""
        }

        generatedText = when {
            normalizedFixedPart.isEmpty() -> randomPart
            randomLength == 0 -> normalizedFixedPart
            fixedPartPosition == FixedPartPosition.START -> normalizedFixedPart + randomPart
            fixedPartPosition == FixedPartPosition.END -> randomPart + normalizedFixedPart
            else -> {
                val insertAt = (0..randomPart.length).random()
                randomPart.substring(0, insertAt) + normalizedFixedPart + randomPart.substring(insertAt)
            }
        }
    }

    if (generatedText.isEmpty()) {
        generate()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (preset == GeneratorPreset.PASSWORD) "随机密码生成器" else "随机账号生成器")
        },
        text = {
            Column {
                Text(
                    text = generatedText,
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text("总长度: ${length.toInt()}")
                Slider(
                    value = length,
                    onValueChange = { length = it },
                    valueRange = 6f..40f,
                    steps = 33,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                SwitchOption("包含大写字母", includeUppercase) { includeUppercase = it }
                SwitchOption("包含小写字母", includeLowercase) { includeLowercase = it }
                SwitchOption("包含数字", includeDigits) { includeDigits = it }
                SwitchOption("包含特殊字符", includeSpecial) { includeSpecial = it }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = fixedPart,
                    onValueChange = { fixedPart = it },
                    label = { Text("固定字符串（可选）") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (fixedPart.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("固定字符串位置")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        PositionOption(
                            label = "首部",
                            selected = fixedPartPosition == FixedPartPosition.START,
                            onSelect = { fixedPartPosition = FixedPartPosition.START }
                        )
                        PositionOption(
                            label = "尾部",
                            selected = fixedPartPosition == FixedPartPosition.END,
                            onSelect = { fixedPartPosition = FixedPartPosition.END }
                        )
                        PositionOption(
                            label = "不限",
                            selected = fixedPartPosition == FixedPartPosition.ANYWHERE,
                            onSelect = { fixedPartPosition = FixedPartPosition.ANYWHERE }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = { generate() }) {
                    Text("重新生成")
                }
                TextButton(onClick = { onConfirm(generatedText) }) {
                    Text("使用此结果")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
private fun PositionOption(
    modifier: Modifier = Modifier,
    label: String,
    selected: Boolean,
    onSelect: () -> Unit
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        RadioButton(selected = selected, onClick = onSelect)
        Text(
            text = label,
            maxLines = 1,
            softWrap = false
        )
    }
}

@Composable
private fun SwitchOption(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
