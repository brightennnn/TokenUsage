package io.github.brightennnn.mmtokenmonitor.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.brightennnn.mmtokenmonitor.ui.components.HapticFeedback
import io.github.brightennnn.mmtokenmonitor.ui.theme.VerveDoDefaults

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsMainScreen(
    onNavigateToAppearance: () -> Unit,
    onNavigateToApiKey: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onNavigateToCredits: () -> Unit,
) {
    val view = LocalView.current
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置") }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding()),
            contentPadding = PaddingValues(
                bottom = innerPadding.calculateBottomPadding()
            )
        ) {
            item { Spacer(modifier = Modifier.height(VerveDoDefaults.screenVerticalPadding)) }

            item {
                VerveDoSettingsItem(
                    leadingIcon = Icons.Default.Key,
                    title = "API Key 管理",
                    description = "添加、切换、删除多个 API Key",
                    onClick = {
                        HapticFeedback.perform(view)
                        onNavigateToApiKey()
                    }
                )
            }

            item {
                VerveDoSettingsItem(
                    leadingIcon = Icons.Default.Palette,
                    title = "个性化设置",
                    description = "主题、颜色、触感反馈等",
                    onClick = {
                        HapticFeedback.perform(view)
                        onNavigateToAppearance()
                    }
                )
            }

            item {
                VerveDoSettingsItem(
                    leadingIcon = Icons.Default.Info,
                    title = "关于",
                    description = "关于本应用",
                    onClick = {
                        HapticFeedback.perform(view)
                        onNavigateToAbout()
                    }
                )
            }

            item {
                VerveDoSettingsItem(
                    leadingIcon = Icons.Default.Favorite,
                    title = "鸣谢",
                    description = "致谢相关项目和开发者",
                    onClick = {
                        HapticFeedback.perform(view)
                        onNavigateToCredits()
                    }
                )
            }

            item { Spacer(modifier = Modifier.height(VerveDoDefaults.screenVerticalPadding)) }
        }
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
