package io.github.brightennnn.mmtokenmonitor.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
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
    lazyListState: LazyListState = rememberLazyListState()
) {
    val view = LocalView.current
    val uiState by viewModel.uiState.collectAsState()

    val themeSettings by themeRepository?.themeSettings?.collectAsState(initial = null)
        ?: remember { mutableStateOf(null) }

    val pullToRefreshState = rememberPullToRefreshState()

    CompositionLocalProvider(LocalHapticFeedbackEnabled provides (themeSettings?.hapticFeedback ?: true)) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("MiniMax Token Monitor") },
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

                val visibleQuotas = uiState.quotas.filter { quota ->
                    val remaining = quota.intervalTotal - quota.intervalUsed
                    remaining != 0
                }

                if (visibleQuotas.isNotEmpty()) {
                    LazyColumn(
                        state = lazyListState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(visibleQuotas) { quota ->
                            QuotaCard(quota = quota)
                        }
                    }
                } else if (uiState.hasKey && uiState.error == null && !uiState.isLoading) {
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
private fun QuotaCard(quota: ModelQuota) {
    val beijingZone = ZoneId.of("Asia/Shanghai")
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    val dateFormatter = DateTimeFormatter.ofPattern("MM-dd")

    val intervalStart = Instant.ofEpochMilli(quota.intervalStartMs).atZone(beijingZone)
    val intervalEnd = Instant.ofEpochMilli(quota.intervalEndMs).atZone(beijingZone)
    val weeklyStart = Instant.ofEpochMilli(quota.weeklyStartMs).atZone(beijingZone)
    val weeklyEnd = Instant.ofEpochMilli(quota.weeklyEndMs).atZone(beijingZone)

    // 判断是否为5小时模型（coding-plan 或 MiniMax-M 系列有5小时时间窗口）
    val isFiveHourModel = quota.modelName.contains("coding-plan", ignoreCase = true)
        || quota.modelName.contains("MiniMax-M", ignoreCase = true)

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
                text = displayModelName(quota.modelName),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 时间窗口1：5小时 or 一天
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val intervalLabel = if (isFiveHourModel) {
                    "5小时用量（${intervalStart.format(timeFormatter)} - ${intervalEnd.format(timeFormatter)}）"
                } else {
                    val today = java.time.LocalDate.now(beijingZone)
                    "今日用量（${today.format(DateTimeFormatter.ofPattern("MM-dd"))}）"
                }
                Text(
                    text = intervalLabel,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${quota.intervalUsed} / ${quota.intervalTotal}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (quota.intervalUsed > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val intervalFraction = if (quota.intervalTotal > 0) quota.intervalUsed.toFloat() / quota.intervalTotal.toFloat() else 0f
                LinearProgressIndicator(
                    progress = { intervalFraction },
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${(intervalFraction * 100).toInt().toString().padStart(3)}%",
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (isFiveHourModel) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(12.dp))

                // 本周用量（仅5小时模型显示）
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "本周用量（${weeklyStart.format(dateFormatter)} ~ ${weeklyEnd.format(dateFormatter)}）",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${quota.weeklyUsed} / ${quota.weeklyTotal}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (quota.weeklyUsed > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val weeklyFraction = if (quota.weeklyTotal > 0) quota.weeklyUsed.toFloat() / quota.weeklyTotal.toFloat() else 0f
                    LinearProgressIndicator(
                        progress = { weeklyFraction },
                        modifier = Modifier
                            .weight(1f)
                            .height(8.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${(weeklyFraction * 100).toInt().toString().padStart(3)}%",
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private fun displayModelName(modelName: String): String = modelName
