package io.github.brightennnn.tokenusage.ui.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.brightennnn.tokenusage.domain.model.MiniMaxCredit
import io.github.brightennnn.tokenusage.domain.model.MiniMaxCreditPackage
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val beijingZone = ZoneId.of("Asia/Shanghai")
private val creditDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

/**
 * MiniMax 积分余额卡。
 *
 * 数据源 /backend/account/token_plan_credit（网页 _token）。
 * credit 为 null（未配 token / 拉取失败）时整体不渲染 —— 与 GLM/重置券同一策略。
 */
@Composable
fun CreditCard(
    credit: MiniMaxCredit?,
    isLoading: Boolean,
    error: String?,
    onRetry: () -> Unit
) {
    if (credit == null && !isLoading) {
        if (error != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "积分读取失败：$error",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = onRetry) { Text("重试", fontSize = 12.sp) }
                }
            }
        }
        return
    }

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
                text = "积分余额",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))

            val c = credit
            if (c == null) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            } else {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = formatTokenNumber(c.remainingCredits),
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "积分",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "共 ${formatTokenNumber(c.totalCredits)} · 已用 ${formatTokenNumber(c.usedCredits)}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { c.remainingPercent / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                )

                // 有多笔积分包时展开查看每笔明细（各自独立过期时间）
                if (c.packages.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    CreditPackageList(packages = c.sortedPackages)
                }
            }
        }
    }
}

@Composable
private fun CreditPackageList(packages: List<MiniMaxCreditPackage>) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .clickable { expanded = !expanded }
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "积分包 ${packages.size} 笔",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Default.ExpandMore,
                contentDescription = if (expanded) "收起" else "展开",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(18.dp)
                    .rotate(if (expanded) 180f else 0f)
            )
        }

        AnimatedVisibility(visible = expanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                packages.forEach { pkg ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "剩 ${formatTokenNumber(pkg.remainsCount)}",
                            fontSize = 12.sp,
                            color = when {
                                pkg.isExpiringSoon -> MaterialTheme.colorScheme.error
                                pkg.isExpired -> MaterialTheme.colorScheme.onSurfaceVariant
                                else -> MaterialTheme.colorScheme.onSurface
                            },
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "${formatTokenNumber(pkg.usageCount)}/${formatTokenNumber(pkg.totalCount)}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = expirationLabel(pkg.expirationTimeMs),
                            fontSize = 11.sp,
                            color = if (pkg.isExpiringSoon) MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

/** "2026-10-30 过期"；已过期显示「已过期」 */
private fun expirationLabel(expireMs: Long): String {
    if (expireMs <= 0L) return "有效期未知"
    val date = Instant.ofEpochMilli(expireMs).atZone(beijingZone)
    val remainDays = (expireMs - System.currentTimeMillis()) / (24 * 60 * 60 * 1000)
    return when {
        remainDays < 0 -> "已过期"
        else -> "${date.format(creditDateFormatter)} 到期"
    }
}

/** 积分用千分位，不用 B/M/K —— 2101 → "2,101" */
private fun formatTokenNumber(v: Long): String = String.format("%,d", v)
