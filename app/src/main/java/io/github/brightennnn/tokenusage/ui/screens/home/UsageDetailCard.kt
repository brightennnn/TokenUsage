package io.github.brightennnn.tokenusage.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.brightennnn.tokenusage.domain.model.ModelUsage
import io.github.brightennnn.tokenusage.domain.model.UsagePeriod
import io.github.brightennnn.tokenusage.domain.model.UsageStats
import io.github.brightennnn.tokenusage.domain.model.UsageSummary

/**
 * 用量明细卡 —— 四周期分段（当日 / 近7日 / 近30日 / 全部）。
 *
 * 数据源：
 * - 当日/7日/30日 → usage_overview?period=day|7d|30d
 * - 全部          → usage_summary（date_model_usage 168 天四分）
 */
@Composable
fun UsageDetailCard(
    period: UsagePeriod,
    stats: UsageStats?,
    summary: UsageSummary?,
    isLoading: Boolean,
    error: String?,
    onSelectPeriod: (UsagePeriod) -> Unit
) {
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
                text = "用量明细",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(10.dp))

            PeriodSelector(selected = period, onSelect = onSelectPeriod)

            Spacer(modifier = Modifier.height(14.dp))

            when {
                isLoading -> {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "加载中…",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                error != null -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "用量读取失败：$error",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                stats != null && stats.hasData -> {
                    UsageStatsBody(stats = stats, summary = summary)
                }

                else -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "该周期暂无用量",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PeriodSelector(selected: UsagePeriod, onSelect: (UsagePeriod) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        UsagePeriod.entries.forEach { p ->
            val active = p == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (active) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent
                    )
                    .clickable { onSelect(p) }
                    .padding(vertical = 7.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = p.displayName,
                    fontSize = 12.sp,
                    fontWeight = if (active) FontWeight.Medium else FontWeight.Normal,
                    color = if (active) MaterialTheme.colorScheme.onSecondaryContainer
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun UsageStatsBody(stats: UsageStats, summary: UsageSummary?) {
    // 总量
    Row(verticalAlignment = Alignment.Bottom) {
        Text(
            text = formatCompactToken(stats.totalToken),
            fontSize = 28.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "token",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 4.dp)
        )
    }

    Spacer(modifier = Modifier.height(10.dp))

    // 缓存命中率 + 日均
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        MetricItem(label = "缓存命中率", value = "${formatPercent(stats.cacheHitPercent)}%")
        if (stats.averageToken > 0L) {
            MetricItem(label = "日均", value = formatCompactToken(stats.averageToken))
        }
        if (stats.isHourlyGranular) {
            MetricItem(label = "粒度", value = "小时")
        }
    }

    // 「全部」周期专属：长期统计
    if (stats.period == UsagePeriod.ALL && summary != null) {
        Spacer(modifier = Modifier.height(12.dp))
        LongTermStats(summary = summary)
    }

    // 模型分布
    if (stats.modelBreakdown.isNotEmpty()) {
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = "模型分布",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        stats.modelBreakdown.forEach { usage ->
            ModelUsageRow(usage = usage)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }

    // 四分（仅「全部」口径拿得到）
    if (stats.period == UsagePeriod.ALL &&
        (stats.inputToken > 0L || stats.outputToken > 0L || stats.cacheReadToken > 0L)
    ) {
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "构成明细",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(6.dp))
        TokenComposition(stats = stats)
    }
}

@Composable
private fun MetricItem(label: String, value: String) {
    Column {
        Text(
            text = label,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun LongTermStats(summary: UsageSummary) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        LongTermRow("累计天数", "${summary.totalDays} 天 · 活跃 ${summary.activeDays} 天")
        LongTermRow("当前连续活跃", "${summary.currentConsecutiveDays} 天")
        if (summary.totalTokenConsumed.isNotBlank()) {
            LongTermRow("累计消耗", summary.totalTokenConsumed)
        }
        if (summary.usageRankingPercent > 0.0) {
            LongTermRow(
                label = "用量排名",
                value = "超过 ${formatPercent(100.0 - summary.usageRankingPercent)}% 的用户"
            )
        }
        summary.mostActiveDayDate?.let { date ->
            val token = summary.mostActiveDayToken?.let { " · $it" }.orEmpty()
            LongTermRow("最活跃日", "$date$token")
        }
        summary.lastUpdateTime?.let { LongTermRow("数据更新", it) }
    }
}

@Composable
private fun LongTermRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun ModelUsageRow(usage: ModelUsage) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = usage.modelName.removePrefix("MiniMax-"),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "${formatCompactToken(usage.token)} · ${formatPercent(usage.percent)}%",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { (usage.percent / 100.0).toFloat().coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
        )
    }
}

@Composable
private fun TokenComposition(stats: UsageStats) {
    val rows = listOf(
        Triple("输入", stats.inputToken, MaterialTheme.colorScheme.primary),
        Triple("缓存读取", stats.cacheReadToken, MaterialTheme.colorScheme.tertiary),
        Triple("缓存写入", stats.cacheCreateToken, MaterialTheme.colorScheme.secondary),
        Triple("输出", stats.outputToken, MaterialTheme.colorScheme.error)
    ).filter { it.second > 0L }

    val total = rows.sumOf { it.second }.coerceAtLeast(1L)
    rows.forEach { (label, value, color) ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(color)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = formatCompactToken(value),
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = formatPercent(value * 100.0 / total) + "%",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/** 68,484,114 → "68.5M"；1127 → "1.1K" */
private fun formatCompactToken(v: Long): String {
    if (v >= 1_000_000_000L) return String.format("%.2fB", v / 1_000_000_000.0)
    if (v >= 1_000_000L) return String.format("%.1fM", v / 1_000_000.0)
    if (v >= 1_000L) return String.format("%.1fK", v / 1_000.0)
    return v.toString()
}

private fun formatPercent(v: Double): String = String.format("%.1f", v)
