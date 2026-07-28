package io.github.brightennnn.mmtokenmonitor.ui.screens.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.brightennnn.mmtokenmonitor.domain.model.DarkMode
import io.github.brightennnn.mmtokenmonitor.domain.model.ThemeSettings
import io.github.brightennnn.mmtokenmonitor.ui.components.HapticFeedback
import io.github.brightennnn.mmtokenmonitor.ui.theme.VerveDoDefaults

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsAppearanceScreen(
    themeSettings: ThemeSettings,
    onDarkModeChange: (DarkMode) -> Unit,
    onDynamicColorChange: (Boolean) -> Unit,
    onPureBlackChange: (Boolean) -> Unit,
    onHapticFeedbackChange: (Boolean) -> Unit,
    onNavigateBack: () -> Unit,
) {
    val view = LocalView.current
    var showDarkModeDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("个性化设置") },
                navigationIcon = {
                    IconButton(onClick = {
                        HapticFeedback.perform(view)
                        onNavigateBack()
                    }) {
                        Icon(
                            Icons.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
                .verticalScroll(rememberScrollState())
        ) {
            // 深色模式
            VerveDoSettingsItem(
                leadingIcon = Icons.Default.Palette,
                title = "深色模式",
                description = darkModeLabel(themeSettings.darkMode),
                onClick = {
                    HapticFeedback.perform(view)
                    showDarkModeDialog = true
                }
            )

            // 动态配色
            VerveDoSettingsSwitchItem(
                leadingIcon = Icons.Default.Palette,
                title = "动态配色",
                description = "使用系统壁纸的主题颜色，仅在Android 12+上生效",
                checked = themeSettings.dynamicColor,
                onCheckedChange = {
                    HapticFeedback.perform(view)
                    onDynamicColorChange(it)
                }
            )

            // 纯黑色模式
            VerveDoSettingsSwitchItem(
                leadingIcon = Icons.Default.Palette,
                title = "纯黑色模式",
                description = "深色时使用纯黑背景（节省 OLED 电量）",
                checked = themeSettings.pureBlack,
                onCheckedChange = {
                    HapticFeedback.perform(view)
                    onPureBlackChange(it)
                }
            )

            // 触感反馈
            VerveDoSettingsSwitchItem(
                leadingIcon = Icons.Default.Palette,
                title = "触感反馈",
                description = "为某些操作提供轻微的震动反馈",
                checked = themeSettings.hapticFeedback,
                onCheckedChange = {
                    HapticFeedback.perform(view)
                    onHapticFeedbackChange(it)
                }
            )
        }
    }

    if (showDarkModeDialog) {
        AlertDialog(
            onDismissRequest = { showDarkModeDialog = false },
            title = { Text("深色模式") },
            text = {
                Column {
                    DarkMode.entries.forEach { mode ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    HapticFeedback.perform(view)
                                    onDarkModeChange(mode)
                                    showDarkModeDialog = false
                                }
                                .padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(darkModeLabel(mode))
                            RadioButton(
                                selected = themeSettings.darkMode == mode,
                                onClick = {
                                    HapticFeedback.perform(view)
                                    onDarkModeChange(mode)
                                    showDarkModeDialog = false
                                }
                            )
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VerveDoSettingsItem(
    leadingIcon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit,
) {
    val view = LocalView.current
    Surface(
        onClick = {
            HapticFeedback.perform(view)
            onClick()
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = VerveDoDefaults.screenHorizontalPadding),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = VerveDoDefaults.settingsItemMinHeight)
                .padding(
                    horizontal = VerveDoDefaults.settingsItemHorizontalPadding,
                    vertical = VerveDoDefaults.settingsItemVerticalPadding
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .padding(end = VerveDoDefaults.settingsItemHorizontalPadding)
                    .height(24.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 20.sp
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
    Spacer(modifier = Modifier.height(VerveDoDefaults.settingsItemPadding))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VerveDoSettingsSwitchItem(
    leadingIcon: ImageVector,
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    val view = LocalView.current
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = VerveDoDefaults.screenHorizontalPadding),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = VerveDoDefaults.settingsItemMinHeight)
                .padding(
                    horizontal = VerveDoDefaults.settingsItemHorizontalPadding,
                    vertical = VerveDoDefaults.settingsItemVerticalPadding
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .padding(end = VerveDoDefaults.settingsItemHorizontalPadding)
                    .height(24.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 20.sp
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Switch(
                checked = checked,
                onCheckedChange = {
                    HapticFeedback.perform(view)
                    onCheckedChange(it)
                }
            )
        }
    }
    Spacer(modifier = Modifier.height(VerveDoDefaults.settingsItemPadding))
}

private fun darkModeLabel(mode: DarkMode): String = when (mode) {
    DarkMode.FOLLOW_SYSTEM -> "跟随系统"
    DarkMode.LIGHT -> "浅色"
    DarkMode.DARK -> "深色"
}
