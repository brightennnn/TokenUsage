package io.github.brightennnn.tokenusage.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.brightennnn.tokenusage.ui.components.GlmLogo
import io.github.brightennnn.tokenusage.ui.components.HapticFeedback
import io.github.brightennnn.tokenusage.ui.components.MiniMaxLogo
import io.github.brightennnn.tokenusage.ui.theme.VerveDoDefaults

/**
 * 从 MiniMax 网页 Token（JWT）里解析 exp → "MM-dd HH:mm" 文案。
 * 解不出来返回 null（设置项描述留空）。
 *
 * 纯手写 URL-safe Base64 解码 + 正则取 exp，避免额外依赖；
 * 失败一律兜 null，绝不让设置主页因凭证格式异常而崩。
 */
internal fun jwtExpiryText(jwt: String): String? {
    val parts = jwt.trim().split('.')
    if (parts.size < 2) return null
    val payload = runCatching {
        String(java.util.Base64.getUrlDecoder().decode(parts[1]), Charsets.UTF_8)
    }.getOrNull() ?: return null
    val exp = Regex("\"exp\"\\s*:\\s*(\\d+)")
        .find(payload)?.groupValues?.get(1)?.toLongOrNull() ?: return null
    val cal = java.util.Calendar.getInstance()
    cal.timeInMillis = exp * 1000L
    return "%02d-%02d %02d:%02d".format(
        cal.get(java.util.Calendar.MONTH) + 1,
        cal.get(java.util.Calendar.DAY_OF_MONTH),
        cal.get(java.util.Calendar.HOUR_OF_DAY),
        cal.get(java.util.Calendar.MINUTE)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsMainScreen(
    onNavigateToAppearance: () -> Unit,
    onNavigateToApiKey: () -> Unit,
    onNavigateToGlmCredential: () -> Unit,
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
                    leadingIcon = MiniMaxLogo,
                    title = "MiniMax账号管理",
                    description = "API KEY及Cookies配置",
                    onClick = {
                        HapticFeedback.perform(view)
                        onNavigateToApiKey()
                    }
                )
            }

            item {
                VerveDoSettingsItem(
                    leadingIcon = GlmLogo,
                    title = "智谱账号管理",
                    description = "Cookies配置",
                    onClick = {
                        HapticFeedback.perform(view)
                        onNavigateToGlmCredential()
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
    description: String? = null,
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
            // 图标统一 24x24 —— 两个 logo 的 viewport 都是正方形，锁死宽高不会变形
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .padding(end = VerveDoDefaults.settingsItemHorizontalPadding)
                    .size(24.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 20.sp
                    )
                )
                if (!description.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
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
