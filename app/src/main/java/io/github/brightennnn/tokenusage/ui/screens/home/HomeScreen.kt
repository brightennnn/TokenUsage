package io.github.brightennnn.tokenusage.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import io.github.brightennnn.tokenusage.data.repository.ThemeRepository
import io.github.brightennnn.tokenusage.domain.model.ModelQuota
import io.github.brightennnn.tokenusage.ui.components.MinimaxSectionHeader
import io.github.brightennnn.tokenusage.ui.components.HapticFeedback
import io.github.brightennnn.tokenusage.ui.components.LocalHapticFeedbackEnabled
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
    insightViewModel: MiniMaxInsightViewModel = hiltViewModel(),
) {
    val view = LocalView.current
    val uiState by viewModel.uiState.collectAsState()
    val insightState by insightViewModel.uiState.collectAsState()

    // 首次进入且已配 _token 时预加载积分 + 默认周期用量
    LaunchedEffect(Unit) { insightViewModel.ensureLoaded() }

    val themeSettings by themeRepository?.themeSettings?.collectAsState(initial = null)
        ?: remember { mutableStateOf(null) }

    val pullToRefreshState = rememberPullToRefreshState()

    CompositionLocalProvider(LocalHapticFeedbackEnabled provides (themeSettings?.hapticFeedback ?: true)) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("MiniMax") },
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
                onRefresh = {
                    viewModel.refresh()
                    // 下拉刷新同时带上 _token 侧的积分与用量
                    if (insightState.hasWebToken) {
                        insightViewModel.fetchCredit()
                        insightViewModel.refreshStats()
                    }
                },
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

                // MiniMax 独立页 — 5小时/本周走 API Key；积分+用量明细走网页 _token。
                // 两套凭证独立：没配 _token 就只显示前两张卡，不报错。
                val hasInsight = insightState.hasWebToken
                if (generalQuota != null || hasInsight) {
                    LazyColumn(
                        state = lazyListState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item { MinimaxSectionHeader() }
                        if (generalQuota != null) {
                            item { IntervalCard(quota = generalQuota) }
                            item { WeeklyCard(quota = generalQuota) }
                        }
                        // 积分余额（_token）
                        if (hasInsight) {
                            item {
                                CreditCard(
                                    credit = insightState.credit,
                                    isLoading = insightState.isCreditLoading,
                                    error = insightState.creditError,
                                    onRetry = insightViewModel::fetchCredit
                                )
                            }
                            // 用量明细（_token，四周期分段）
                            item {
                                UsageDetailCard(
                                    period = insightState.period,
                                    stats = insightState.stats,
                                    summary = insightState.summary,
                                    isLoading = insightState.isStatsLoading,
                                    error = insightState.statsError,
                                    onSelectPeriod = insightViewModel::selectPeriod
                                )
                            }
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
            // 标题行：左标题 + 右「可重置N次」（点击列过期时间）
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "本周剩余量",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                val resetCount = quota.resetQuotaCount
                val resetExpire = quota.resetQuotaExpireMs
                val resetExpireList = quota.resetQuotaExpireList
                if (resetCount != null && resetCount > 0) {
                    val expireRows = remember(resetExpireList, resetExpire) {
                        if (resetExpireList.isNotEmpty()) {
                            resetExpireList.map {
                                Instant.ofEpochMilli(it).atZone(beijingZone).format(expireDateFormatter)
                            }
                        } else if (resetExpire != null) {
                            listOf(
                                Instant.ofEpochMilli(resetExpire).atZone(beijingZone).format(expireDateFormatter)
                            )
                        } else emptyList()
                    }
                    // 默认只显示「可重置N次」；点击弹出逐张过期时间，点别处自动消失
                    var menuOpen by remember { mutableStateOf(false) }
                    Box {
                        Text(
                            text = "可重置${resetCount}次",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { menuOpen = true }
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                        DropdownMenu(
                            expanded = menuOpen,
                            onDismissRequest = { menuOpen = false },
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.background(
                                MaterialTheme.colorScheme.surfaceContainerHigh,
                                RoundedCornerShape(16.dp)
                            )
                        ) {
                            if (expireRows.isEmpty()) {
                                Text(
                                    text = "有效期未知",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                                )
                            } else {
                                Text(
                                    text = "过期时间",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                                )
                                expireRows.forEachIndexed { index, row ->
                                    Text(
                                        text = "${superscriptMark(index + 1)} $row",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(
                                            start = 22.dp, end = 16.dp, top = 3.dp, bottom = 3.dp
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
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

/**
 * 上标序号：1 → ¹，2 → ²，3 → ³ … 超出 9 回退成普通数字。
 * 与 GlmQuotaCard.superscript 同款实现（那边是 private，这里独立一份避免跨文件耦合）。
 */
private fun superscriptMark(n: Int): String {
    val marks = listOf("⁰", "¹", "²", "³", "⁴", "⁵", "⁶", "⁷", "⁸", "⁹")
    return if (n in 1..9) marks[n] else n.toString()
}

/** 重置券过期时间展示格式（北京时间） */
private val expireDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MM-dd HH:mm")

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
