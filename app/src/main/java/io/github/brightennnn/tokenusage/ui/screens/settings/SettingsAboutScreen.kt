package io.github.brightennnn.tokenusage.ui.screens.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.brightennnn.tokenusage.R
import io.github.brightennnn.tokenusage.ui.components.HapticFeedback
import io.github.brightennnn.tokenusage.ui.theme.VerveDoDefaults

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsAboutScreen(
    onNavigateBack: () -> Unit,
) {
    val view = LocalView.current
    val context = LocalContext.current

    // 动态读取 PackageInfo 显示当前版本号 — 不要写死，跟 build.gradle.kts 的 versionName 同步
    val versionDisplay = remember(context) {
        try {
            val pi = context.packageManager.getPackageInfo(context.packageName, 0)
            val name = pi.versionName ?: "未知"
            val code = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                pi.longVersionCode
            } else {
                @Suppress("DEPRECATION") pi.versionCode.toLong()
            }
            "$name ($code)"
        } catch (e: Exception) {
            "未知"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("关于") },
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
            AboutItem(
                leadingIconContent = {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.height(24.dp)
                    )
                },
                title = "版本号",
                subtitle = versionDisplay,
                onClick = null
            )

            AboutItem(
                leadingIconContent = {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.height(24.dp)
                    )
                },
                title = "开发者",
                subtitle = "Brightennnn",
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/brightennnn"))
                    context.startActivity(intent)
                }
            )

            AboutItem(
                leadingIconContent = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_github),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.height(24.dp)
                    )
                },
                title = "在 GitHub 查看",
                subtitle = "查看源代码、提交错误报告和改进建议",
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/brightennnn/TokenUsage"))
                    context.startActivity(intent)
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AboutItem(
    leadingIconContent: @Composable () -> Unit,
    title: String,
    subtitle: String,
    onClick: (() -> Unit)?,
) {
    val view = LocalView.current
    Surface(
        onClick = onClick ?: {},
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
                .clickable(enabled = onClick != null) {
                    onClick?.let {
                        HapticFeedback.perform(view)
                        it()
                    }
                }
                .padding(
                    horizontal = VerveDoDefaults.settingsItemHorizontalPadding,
                    vertical = VerveDoDefaults.settingsItemVerticalPadding
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            leadingIconContent()
            Spacer(modifier = Modifier.width(VerveDoDefaults.settingsItemHorizontalPadding))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (onClick != null) {
                Spacer(modifier = Modifier.width(VerveDoDefaults.settingsItemHorizontalPadding))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
    Spacer(modifier = Modifier.height(VerveDoDefaults.settingsItemPadding))
}
