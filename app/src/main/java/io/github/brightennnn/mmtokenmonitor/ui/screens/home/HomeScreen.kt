package io.github.brightennnn.mmtokenmonitor.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import io.github.brightennnn.mmtokenmonitor.data.repository.ThemeRepository
import io.github.brightennnn.mmtokenmonitor.domain.model.ModelQuota
import io.github.brightennnn.mmtokenmonitor.ui.components.GlmErrorCard
import io.github.brightennnn.mmtokenmonitor.ui.components.GlmNotConfiguredCard
import io.github.brightennnn.mmtokenmonitor.ui.components.GlmQuotaCard
import io.github.brightennnn.mmtokenmonitor.ui.components.GlmSectionHeader
import io.github.brightennnn.mmtokenmonitor.ui.components.MinimaxSectionHeader
import io.github.brightennnn.mmtokenmonitor.ui.components.HapticFeedback
import io.github.brightennnn.mmtokenmonitor.ui.components.LocalHapticFeedbackEnabled
import dagger.hilt.android.EntryPointAccessors
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    themeRepository: ThemeRepository? = null,
    lazyListState: LazyListState = rememberLazyListState(),
    onNavigateToGlmCredential: () -> Unit = {},
) {
    val view = LocalView.current
    val uiState by viewModel.uiState.collectAsState()

    val themeSettings by themeRepository?.themeSettings?.collectAsState(initial = null)
        ?: remember { mutableStateOf(null) }

    val pullToRefreshState = rememberPullToRefreshState()

    // GLM 凭证页跳转触发
    LaunchedEffect(uiState.glmNavigateToCredential) {
        if (uiState.glmNavigateToCredential) {
            viewModel.consumeGlmCredentialNavigation()
            onNavigateToGlmCredential()
        }
    }

    CompositionLocalProvider(LocalHapticFeedbackEnabled provides (themeSettings?.hapticFeedback ?: true)) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Plan Usage") },
                    actions = {
                        if (uiState.hasKey) {
                            IconButton(
                                onClick = {
                                    HapticFeedback.perform(view)
                                    viewModel.refresh()
                                }
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = "刷新")
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        ) { innerPadding ->
            PullToRefreshBox(
                isRefreshing = uiState.isLoading,
                onRefresh = { viewModel.refresh() },
                state = pullToRefreshState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = innerPadding.calculateTopPadding())
            ) {
                if (uiState.error != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = uiState.error!!,
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }

                if (!uiState.hasKey) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "请先设置 API Key",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "点击右上角设置按钮添加",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                            )
                        }
                    }
                }

                val generalQuota = uiState.quotas.firstOrNull { it.modelName.equals("general", ignoreCase = true) }
                val hasMinimaxCard = generalQuota != null

                if (hasMinimaxCard || uiState.hasGlmKey || uiState.glmError != null) {
                    LazyColumn(
                        state = lazyListState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // --- MiniMax 卡片 ---
                        if (generalQuota != null) {
                            item { MinimaxSectionHeader() }
                            item { IntervalCard(quota = generalQuota) }
                            item { WeeklyCard(quota = generalQuota) }
                        }

                        // --- GLM Section ---
                        item { GlmSectionHeader(level = uiState.glmLevel, hasError = uiState.glmError != null) }

                        when {
                            !uiState.hasGlmKey -> {
                                item {
                                    GlmNotConfiguredCard(
                                        onClick = { viewModel.requestGlmCredentialNavigation() }
                                    )
                                }
                            }
                            uiState.glmError != null && uiState.glmQuotas.isEmpty() -> {
                                item {
                                    GlmErrorCard(
                                        message = uiState.glmError ?: "未知错误",
                                        onClick = { viewModel.requestGlmCredentialNavigation() }
                                    )
                                }
                            }
                            else -> {
                                // 5小时窗、周窗（过滤掉 MCP 月窗 rawUnit=5）
                                uiState.glmQuotas.filter { it.rawUnit != 5 }.forEach { quota ->
                                    item(key = "glm-${quota.rawUnit}") {
                                        GlmQuotaCard(quota = quota)
                                    }
                                }
                                if (uiState.isGlmLoading) {
                                    item {
                                        Box(
                                            modifier = Modifier.fillMaxWidth().padding(8.dp),
                                            contentAlignment = Alignment.Center
                                        ) { CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp) }
                                    }
                                }
                            }
                        }
                    }
                } else if (uiState.hasKey && uiState.error == null && !uiState.isLoading && !uiState.isGlmLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "暂无用量数据",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun IntervalCard(quota: ModelQuota) {
    val beijingZone = ZoneId.of("Asia/Shanghai")
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    val start = Instant.ofEpochMilli(quota.intervalStartMs).atZone(beijingZone)
    val end = Instant.ofEpochMilli(quota.intervalEndMs).atZone(beijingZone)
    val range = "${start.format(timeFormatter)} - ${end.format(timeFormatter)}"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Text(
                text = "5小时剩余量",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = range,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            val pct = quota.intervalRemainingPercent.coerceIn(0, 100)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LinearProgressIndicator(
                    progress = { pct / 100f },
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp),
                    color = if (pct < 20) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "$pct%",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "剩 ${formatRemainsMs(quota.intervalRemainsMs)}",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun WeeklyCard(quota: ModelQuota) {
    val beijingZone = ZoneId.of("Asia/Shanghai")
    val dateFormatter = DateTimeFormatter.ofPattern("MM-dd")
    val start = Instant.ofEpochMilli(quota.weeklyStartMs).atZone(beijingZone)
    val end = Instant.ofEpochMilli(quota.weeklyEndMs).atZone(beijingZone)
    val range = "${start.format(dateFormatter)} ~ ${end.format(dateFormatter)}"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Text(
                text = "本周剩余量",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = range,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val pct = quota.weeklyRemainingPercent.coerceIn(0, 100)
                LinearProgressIndicator(
                    progress = { pct / 100f },
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp),
                    color = if (pct < 20) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "$pct%",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "剩 ${formatRemainsMs(quota.weeklyRemainsMs)}",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

/** 把毫秒转成 "X天 Y小时" / "Y小时 Z分" / "Z分" */
private fun formatRemainsMs(ms: Long): String {
    if (ms <= 0) return "—"
    val totalMin = ms / 1000 / 60
    val days = totalMin / (60 * 24)
    val hours = (totalMin % (60 * 24)) / 60
    val mins = totalMin % 60
    return when {
        days > 0 -> "${days}天${hours}时"
        hours > 0 -> "${hours}时${mins}分"
        else -> "${mins}分"
    }
}
