package io.github.brightennnn.mmtokenmonitor.ui.screens.settings.apikey

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.brightennnn.mmtokenmonitor.domain.model.ApiKeyProfile
import androidx.compose.ui.platform.LocalView
import io.github.brightennnn.mmtokenmonitor.ui.components.HapticFeedback
import io.github.brightennnn.mmtokenmonitor.ui.theme.VerveDoDefaults

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApiKeyManagementScreen(
    profiles: List<ApiKeyProfile>,
    activeProfileId: String?,
    onNavigateBack: () -> Unit,
    onAddKey: (name: String, key: String, groupId: String) -> Unit,
    onActivate: (id: String) -> Unit,
    onDelete: (id: String) -> Unit,
    onUpdateProfile: (ApiKeyProfile) -> Unit,
) {
    val view = LocalView.current
    var showAddDialog by remember { mutableStateOf(false) }
    var editingProfile by remember { mutableStateOf<ApiKeyProfile?>(null) }
    var profileToDelete by remember { mutableStateOf<ApiKeyProfile?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("API Key 管理") },
                navigationIcon = {
                    IconButton(onClick = {
                        HapticFeedback.perform(view)
                        onNavigateBack()
                    }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        HapticFeedback.perform(view)
                        showAddDialog = true
                    }) {
                        Icon(Icons.Default.Add, contentDescription = "添加")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (profiles.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = innerPadding.calculateTopPadding())
                    .padding(bottom = innerPadding.calculateBottomPadding()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(48.dp))
                Icon(
                    Icons.Default.Key,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    "暂无 API Key",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "点击右上角 + 添加第一个 Key",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = innerPadding.calculateTopPadding()),
                contentPadding = PaddingValues(
                    vertical = VerveDoDefaults.screenVerticalPadding,
                    horizontal = 0.dp
                )
            ) {
                items(profiles, key = { it.id }) { profile ->
                    ApiKeyListItem(
                        profile = profile,
                        isActive = profile.id == activeProfileId,
                        onEdit = { editingProfile = profile },
                        onActivate = { onActivate(profile.id) },
                        onDelete = { profileToDelete = profile }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddApiKeyDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, key, groupId ->
                onAddKey(name, key, groupId)
                showAddDialog = false
            }
        )
    }

    editingProfile?.let { profile ->
        EditApiKeyDialog(
            currentName = profile.name,
            currentKey = profile.key,
            currentGroupId = profile.groupId,
            onDismiss = { editingProfile = null },
            onConfirm = { newName, newKey, newGroupId ->
                onUpdateProfile(profile.copy(name = newName, key = newKey, groupId = newGroupId))
                editingProfile = null
            }
        )
    }

    profileToDelete?.let { profile ->
        AlertDialog(
            onDismissRequest = { profileToDelete = null },
            title = { Text("删除确认") },
            text = { Text("确定要删除「${profile.name}」吗？此操作不可恢复。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        HapticFeedback.perform(view)
                        onDelete(profile.id)
                        profileToDelete = null
                    }
                ) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    HapticFeedback.perform(view)
                    profileToDelete = null
                }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
private fun ApiKeyListItem(
    profile: ApiKeyProfile,
    isActive: Boolean,
    onEdit: () -> Unit,
    onActivate: () -> Unit,
    onDelete: () -> Unit,
) {
    val view = LocalView.current
    var showKey by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = VerveDoDefaults.screenHorizontalPadding),
        shape = MaterialTheme.shapes.large,
        color = if (isActive) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        } else {
            MaterialTheme.colorScheme.surfaceContainerHigh
        },
    ) {
        Column(
            modifier = Modifier.padding(VerveDoDefaults.settingsItemVerticalPadding)
        ) {
            // 头部：图标+名称+操作
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Key,
                    contentDescription = null,
                    tint = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .padding(end = VerveDoDefaults.settingsItemHorizontalPadding)
                        .height(24.dp)
                )
                Text(
                    text = profile.name,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.weight(1f)
                )
                if (isActive) {
                    SuggestionChip(
                        onClick = { },
                        label = { Text("使用中", style = MaterialTheme.typography.labelSmall) },
                        modifier = Modifier.height(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                } else {
                    IconButton(onClick = {
                        HapticFeedback.perform(view)
                        onActivate()
                    }) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "激活",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                IconButton(onClick = {
                    HapticFeedback.perform(view)
                    showKey = !showKey
                }) {
                    Icon(
                        imageVector = if (showKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (showKey) "隐藏" else "显示"
                    )
                }
                IconButton(onClick = {
                    HapticFeedback.perform(view)
                    onEdit()
                }) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "编辑",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = {
                    HapticFeedback.perform(view)
                    onDelete()
                }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "删除",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
            // Key 内容
            Text(
                text = if (showKey) profile.key else maskKey(profile.key),
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.padding(start = (48 + VerveDoDefaults.settingsItemHorizontalPadding.value).dp)
            )
        }
    }
    Spacer(modifier = Modifier.height(VerveDoDefaults.settingsItemPadding))
}

@Composable
private fun AddApiKeyDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, key: String, groupId: String) -> Unit,
) {
    val view = LocalView.current
    var name by remember { mutableStateOf("") }
    var key by remember { mutableStateOf("") }
    var groupId by remember { mutableStateOf("") }
    var showKey by remember { mutableStateOf(false) }
    var showAdvanced by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("添加 API Key") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("名称") },
                    placeholder = { Text("例如：我的主账号") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = key,
                    onValueChange = { key = it },
                    label = { Text("API Key") },
                    placeholder = { Text("输入你的 MiniMax API Key") },
                    singleLine = true,
                    visualTransformation = if (showKey) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        IconButton(onClick = {
                            HapticFeedback.perform(view)
                            showKey = !showKey
                        }) {
                            Icon(
                                imageVector = if (showKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (showKey) "隐藏" else "显示"
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                // 高级选项：GroupId（可选）
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            HapticFeedback.perform(view)
                            showAdvanced = !showAdvanced
                        }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (showAdvanced) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "高级选项（可选）",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                if (showAdvanced) {
                    OutlinedTextField(
                        value = groupId,
                        onValueChange = { groupId = it },
                        label = { Text("Group ID（可选）") },
                        placeholder = { Text("不填也能查") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    HapticFeedback.perform(view)
                    onConfirm(name, key, groupId)
                },
                enabled = name.isNotBlank() && key.isNotBlank()
            ) {
                Text("添加")
            }
        },
        dismissButton = {
            TextButton(onClick = {
                HapticFeedback.perform(view)
                onDismiss()
            }) {
                Text("取消")
            }
        }
    )
}

@Composable
private fun EditApiKeyDialog(
    currentName: String,
    currentKey: String,
    currentGroupId: String,
    onDismiss: () -> Unit,
    onConfirm: (name: String, key: String, groupId: String) -> Unit,
) {
    val view = LocalView.current
    var name by remember { mutableStateOf(currentName) }
    var key by remember { mutableStateOf(currentKey) }
    var groupId by remember { mutableStateOf(currentGroupId) }
    var showKey by remember { mutableStateOf(false) }
    // 已填过 GroupId 时默认展开，否则收起
    var showAdvanced by remember { mutableStateOf(currentGroupId.isNotEmpty()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("编辑 API Key") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("名称") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = key,
                    onValueChange = { key = it },
                    label = { Text("API Key") },
                    singleLine = true,
                    visualTransformation = if (showKey) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        IconButton(onClick = {
                            HapticFeedback.perform(view)
                            showKey = !showKey
                        }) {
                            Icon(
                                imageVector = if (showKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (showKey) "隐藏" else "显示"
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                // 高级选项：GroupId（可选）
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            HapticFeedback.perform(view)
                            showAdvanced = !showAdvanced
                        }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (showAdvanced) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "高级选项（可选）",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                if (showAdvanced) {
                    OutlinedTextField(
                        value = groupId,
                        onValueChange = { groupId = it },
                        label = { Text("Group ID（可选）") },
                        placeholder = { Text("不填也能查") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    HapticFeedback.perform(view)
                    onConfirm(name, key, groupId)
                },
                enabled = name.isNotBlank() && key.isNotBlank()
            ) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = {
                HapticFeedback.perform(view)
                onDismiss()
            }) {
                Text("取消")
            }
        }
    )
}

private fun maskKey(key: String): String {
    return if (key.length > 8) {
        key.take(4) + "****" + key.takeLast(4)
    } else {
        "****"
    }
}
