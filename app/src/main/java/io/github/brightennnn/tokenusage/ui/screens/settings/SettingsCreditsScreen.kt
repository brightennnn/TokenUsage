package io.github.brightennnn.tokenusage.ui.screens.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import io.github.brightennnn.tokenusage.R
import io.github.brightennnn.tokenusage.ui.components.HapticFeedback
import io.github.brightennnn.tokenusage.ui.theme.VerveDoDefaults

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsCreditsScreen(
    onNavigateBack: () -> Unit,
) {
    val view = LocalView.current
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("鸣谢") },
                navigationIcon = {
                    IconButton(onClick = {
                        HapticFeedback.perform(view)
                        onNavigateBack()
                    }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
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
            CreditsItem(
                leadingIconContent = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_vervedo),
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier.height(32.dp)
                    )
                },
                title = "VerveDo",
                subtitle = "遵循 Material 3 Expressive 的待办应用",
                url = "https://github.com/Super12138/VerveDo",
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Super12138/VerveDo"))
                    context.startActivity(intent)
                }
            )

            CreditsItem(
                leadingIconContent = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_hermes),
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier.height(32.dp)
                    )
                },
                title = "Hermes",
                subtitle = "An agent that grows with you",
                url = "https://github.com/nousresearch/hermes-agent",
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/nousresearch/hermes-agent"))
                    context.startActivity(intent)
                }
            )

            CreditsItem(
                leadingIconContent = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_minimax),
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier.height(32.dp)
                    )
                },
                title = "MiniMax",
                subtitle = "全栈模型，覆盖文本、语音、视频、音乐",
                url = "https://www.minimaxi.com",
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.minimaxi.com"))
                    context.startActivity(intent)
                }
            )
        }
    }
}

@Composable
private fun CreditsItem(
    leadingIconContent: @Composable () -> Unit,
    title: String,
    subtitle: String,
    url: String,
    onClick: () -> Unit,
) {
    val view = LocalView.current
    Surface(
        onClick = onClick,
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
                .clickable {
                    HapticFeedback.perform(view)
                    onClick()
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
        }
    }
    Spacer(modifier = Modifier.height(VerveDoDefaults.settingsItemPadding))
}
