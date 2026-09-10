package io.github.brightennnn.mmtokenmonitor.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.brightennnn.mmtokenmonitor.domain.model.GlmQuota
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * GLM 单窗口卡片。对齐 MiniMax IntervalCard/WeeklyCard 的字段和布局，
 * 用紫色左边框 + 紫系进度条与 MiniMax 卡片视觉区分。
 *
 * MiniMax 对齐点：
 * - 标题："5小时剩余量" / "本周剩余量"
 * - 副标题：窗口时间起止
 * - 进度条：剩余% (remainingPercent)，<20% 变红色
 * - 底部："剩 X天Y时"
 */
@Composable
fun GlmQuotaCard(
    quota: GlmQuota,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val beijingZone = ZoneId.of("Asia/Shanghai")
    val dateTimeFormatter = DateTimeFormatter.ofPattern("MM-dd HH:mm")

    // 5h 窗口使用 Repository 传入的实际起止时间；周窗/月窗按其重置时间展示。
    val (startMs, endMs, rangeText) = when {
        quota.isFiveHourWindow -> {
            val start = quota.windowStartMs
            val end = quota.windowEndMs
            if (start != null && end != null && end > start) {
                val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
                val startStr = Instant.ofEpochMilli(start).atZone(beijingZone).format(timeFormatter)
                val endStr = Instant.ofEpochMilli(end).atZone(beijingZone).format(timeFormatter)
                Triple(start, end, "$startStr - $endStr")
            } else {
                Triple(null, null, "时间暂不可用")
            }
        }
        quota.rawUnit == 5 -> {
            // MCP 月窗: 从下次重置时间倒推1个月（起止都是自然月）~ 下次重置时间
            val resetInstant = Instant.ofEpochMilli(quota.nextResetMs).atZone(beijingZone)
            val monthStart = resetInstant.minusMonths(1)
            Triple(
                monthStart.toInstant().toEpochMilli(),
                quota.nextResetMs,
                "${monthStart.format(dateTimeFormatter)} ~ ${resetInstant.format(dateTimeFormatter)}"
            )
        }
        else -> {
            // 周窗: 结束时间倒推7天，副标题精确到 HH:mm
            val resetInstant = Instant.ofEpochMilli(quota.nextResetMs).atZone(beijingZone)
            val weekStart = resetInstant.minusDays(7)
            Triple(
                weekStart.toInstant().toEpochMilli(),
                resetInstant.toInstant().toEpochMilli(),
                "${weekStart.format(dateTimeFormatter)} ~ ${resetInstant.format(dateTimeFormatter)}"
            )
        }
    }

    // MiniMax 对齐: 标题/副标题/进度条/底部文字
    val title = when {
        quota.isFiveHourWindow -> "5小时剩余量"
        quota.rawUnit == 5 -> "月度用量"
        else -> "本周剩余量"
    }
    val pct = quota.remainingPercent.coerceIn(0, 100)
    val barColor = if (pct < 20) MaterialTheme.colorScheme.error
                   else MaterialTheme.colorScheme.primary
    val accent = MaterialTheme.colorScheme.primary

    Card(
        modifier = modifier
            .fillMaxWidth()
            .let { if (onClick != null) it.clickable { onClick() } else it },
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // 紫色左边框 — GLM 视觉标识
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(color = accent)
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                // 标题
                Text(
                    text = title,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(2.dp))
                // 副标题: 窗口时间范围
                Text(
                    text = rangeText,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                // 进度条 + 百分比
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LinearProgressIndicator(
                        progress = { pct / 100f },
                        modifier = Modifier
                            .weight(1f)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = barColor,
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
                // 底部: 剩余时间 "剩 X天Y时"
                Text(
                    text = "剩 ${formatRemainsMs(quota.remainsMs)}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
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

/**
 * GLM Section Header
 */
@Composable
fun GlmSectionHeader(
    level: String? = null,
    hasError: Boolean = false,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(
                    color = if (hasError) MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(50)
                )
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "GLM Coding Plan",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold
        )
        if (!level.isNullOrBlank()) {
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
            ) {
                Text(
                    text = level.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "智谱 BigModel",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}

/**
 * GLM 未配置凭证引导卡
 */
@Composable
fun GlmNotConfiguredCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        border = BorderStroke(width = 2.dp, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Text(
                text = "未配置 GLM 凭证",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "点击前往设置添加智谱 BigModel JWT，即可查看 5 小时窗和周窗用量。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * GLM 错误占位卡
 */
@Composable
fun GlmErrorCard(
    message: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
        ),
        border = BorderStroke(width = 2.dp, color = MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Text(
                text = "GLM 用量获取失败",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "点击前往更新凭证 →",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}
