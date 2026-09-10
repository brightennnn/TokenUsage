package io.github.brightennnn.mmtokenmonitor.ui.screens.settings.glm

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.brightennnn.mmtokenmonitor.ui.components.HapticFeedback
import io.github.brightennnn.mmtokenmonitor.ui.theme.VerveDoDefaults

/**
 * GLM Coding Plan 凭证管理页 — 单条 JWT 的增删改查。
 * 不复用 ApiKeyManagementScreen 的多 profile 逻辑（GLM 只需要 1 条 token）。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlmCredentialScreen(
    state: GlmCredentialUiState,
    onStartEditing: () -> Unit,
    onCancelEditing: () -> Unit,
    onDraftChange: (String) -> Unit,
    onToggleShow: () -> Unit,
    onSave: () -> Unit,
    onSaveAndTest: () -> Unit,
    onDelete: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    val view = LocalView.current
    var confirmDelete by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("GLM 凭证") },
                navigationIcon = {
                    IconButton(onClick = {
                        HapticFeedback.perform(view)
                        onNavigateBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
                .padding(
                    horizontal = VerveDoDefaults.screenHorizontalPadding,
                    vertical = VerveDoDefaults.screenVerticalPadding
                ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 顶部说明卡
            Surface(
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "智谱 BigModel 平台 JWT",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "在浏览器登录 bigmodel.cn 后，从 Cookie 中复制 bigmodel_token_production 的值粘贴到此处。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.85f)
                    )
                }
            }

            // 凭证卡片
            Surface(
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(VerveDoDefaults.settingsItemVerticalPadding)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = if (state.hasToken) MaterialTheme.colorScheme.primary
                                   else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .padding(end = VerveDoDefaults.settingsItemHorizontalPadding)
                                .height(24.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (state.hasToken) "已配置" else "未配置",
                                style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (state.hasToken && !state.isEditing) {
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = state.tokenMasked,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                        if (state.hasToken && !state.isEditing) {
                            IconButton(onClick = {
                                HapticFeedback.perform(view)
                                onStartEditing()
                            }) {
                                Icon(Icons.Default.Edit, contentDescription = "编辑", tint = MaterialTheme.colorScheme.primary)
                            }
                            IconButton(onClick = {
                                HapticFeedback.perform(view)
                                confirmDelete = true
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "删除", tint = MaterialTheme.colorScheme.error)
                            }
                        } else if (!state.isEditing) {
                            FilledTonalButton(onClick = {
                                HapticFeedback.perform(view)
                                onStartEditing()
                            }) {
                                Text("添加")
                            }
                        }
                    }

                    if (state.isEditing) {
                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(
                            value = state.draftToken,
                            onValueChange = onDraftChange,
                            label = { Text("bigmodel_token_production") },
                            placeholder = { Text("粘贴 JWT（不含 Bearer 前缀）") },
                            singleLine = false,
                            minLines = 2,
                            maxLines = 4,
                            visualTransformation = if (state.showToken) VisualTransformation.None
                                                    else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            trailingIcon = {
                                IconButton(onClick = {
                                    HapticFeedback.perform(view)
                                    onToggleShow()
                                }) {
                                    Icon(
                                        if (state.showToken) Icons.Default.VisibilityOff
                                        else Icons.Default.Visibility,
                                        contentDescription = if (state.showToken) "隐藏" else "显示"
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (state.error != null) {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                state.error,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        Spacer(Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(onClick = {
                                HapticFeedback.perform(view)
                                onCancelEditing()
                            }) { Text("取消") }
                            Spacer(Modifier.width(8.dp))
                            OutlinedButton(
                                onClick = {
                                    HapticFeedback.perform(view)
                                    onSaveAndTest()
                                },
                                enabled = !state.isLoading
                            ) { Text("保存并测试") }
                            Spacer(Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    HapticFeedback.perform(view)
                                    onSave()
                                },
                                enabled = !state.isLoading
                            ) { Text("保存") }
                        }
                    }

                    // 测试结果提示
                    if (state.testResult != null) {
                        Spacer(Modifier.height(8.dp))
                        val ok = state.testResult == "测试成功"
                        Text(
                            text = state.testResult,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = if (ok) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(start = 48.dp)
                        )
                    }
                }
            }

            // 使用提示
            Surface(
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "使用提示",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "• JWT 一般有效期较长，但官方未公开具体时长；过期后接口会返回 1001，此时回到此处更新。\n" +
                        "• 仅展示 5 小时窗和周窗，工具月窗等不展示。\n" +
                        "• 凭证仅保存在本机 DataStore，不上传服务器。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f)
                    )
                }
            }
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("删除 GLM 凭证？") },
            text = { Text("删除后主页将不再展示 GLM 用量。此操作不可恢复。") },
            confirmButton = {
                TextButton(onClick = {
                    HapticFeedback.perform(view)
                    confirmDelete = false
                    onDelete()
                }) { Text("删除", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = {
                    HapticFeedback.perform(view)
                    confirmDelete = false
                }) { Text("取消") }
            }
        )
    }
}
