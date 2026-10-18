package io.github.brightennnn.tokenusage.ui.screens.settings.minimaxweb

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
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
import io.github.brightennnn.tokenusage.ui.components.HapticFeedback
import io.github.brightennnn.tokenusage.ui.theme.VerveDoDefaults

/**
 * MiniMax Token（网页登录 _token）管理页 — 单条凭证的增删改查。
 *
 * 用途严格限定：查询「用量重置额度（重置券）」。
 * 5小时/本周用量走 MiniMax API Key（sk-cp-xxx），与本凭证无关。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MiniMaxWebTokenScreen(
    state: MiniMaxWebTokenUiState,
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
                title = { Text("MiniMax Token") },
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
                        "MiniMax Token",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "网页登录凭证，仅用于查询用量重置额度",
                        style = MaterialTheme.typography.bodySmall,
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
                            Icons.Default.Key,
                            contentDescription = null,
                            tint = if (state.hasToken && !state.isExpired) MaterialTheme.colorScheme.primary
                                   else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .padding(end = VerveDoDefaults.settingsItemHorizontalPadding)
                                .height(24.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (!state.hasToken) "未配置"
                                       else if (state.isExpired) "已过期"
                                       else "已配置",
                                style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp),
                                color = if (state.hasToken && state.isExpired) MaterialTheme.colorScheme.error
                                       else MaterialTheme.colorScheme.onSurface
                            )
                            if (state.hasToken && !state.isEditing) {
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = buildString {
                                        append(state.tokenMasked)
                                        state.expiryText?.let { append("\n"); append(it) }
                                    },
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
                            label = { Text("_token") },
                            placeholder = { Text("粘贴 Cookie 或裸 JWT（eyJ...）") },
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
                        val ok = state.testResult.startsWith("测试成功")
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
                        "• 抓取方式：电脑 Chrome 登录 platform.minimaxi.com → F12 → Network → 任一 backend 请求 → Request Headers 复制 Cookie 里的 _token 值。\n" +
                        "• 也可直接粘贴整段 Cookie，会自动提取 _token。\n" +
                        "• Token 有效期较短（实测约 7 天），过期后接口返回 1004，此时回到此处更新。\n" +
                        "• 本凭证仅用于查询用量重置额度，不参与 5 小时/本周用量查询。\n" +
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
            title = { Text("删除 MiniMax Token？") },
            text = { Text("删除后将不再展示用量重置额度。此操作不可恢复。") },
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
