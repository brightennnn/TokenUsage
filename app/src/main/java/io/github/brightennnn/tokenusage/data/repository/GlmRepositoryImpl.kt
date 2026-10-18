package io.github.brightennnn.tokenusage.data.repository

import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.brightennnn.tokenusage.data.remote.GlmApi
import io.github.brightennnn.tokenusage.data.remote.GlmLimitDto
import io.github.brightennnn.tokenusage.data.remote.GlmResetTicketDto
import io.github.brightennnn.tokenusage.domain.model.GlmQuota
import io.github.brightennnn.tokenusage.domain.repository.GlmRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton
import android.content.Context

/**
 * 复用 TokenRepositoryImpl 暴露的内部 DataStore (Context.tokenDataStore)，
 * 同一个文件、同一个实例，避免单进程 DataStore 冲突。
 *
 * GLM JWT 单独一个 key (glm_jwt_token)，不影响 MiniMax API Key 列表。
 */
@Singleton
class GlmRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val api: GlmApi
) : GlmRepository {

    private object PrefsKeys {
        val GLM_TOKEN = stringPreferencesKey("glm_jwt_token")
    }

    override val glmToken: Flow<String?> = context.tokenDataStore.data
        .map { prefs -> prefs[PrefsKeys.GLM_TOKEN]?.takeIf { it.isNotBlank() } }

    override suspend fun saveToken(token: String) {
        context.tokenDataStore.edit { prefs ->
            prefs[PrefsKeys.GLM_TOKEN] = token.trim()
        }
    }

    override suspend fun clearToken() {
        context.tokenDataStore.edit { prefs ->
            prefs.remove(PrefsKeys.GLM_TOKEN)
        }
    }

    override suspend fun fetchQuotas(token: String): Result<List<GlmQuota>> {
        return try {
            val effectiveToken = token.ifBlank {
                glmToken.first() ?: return Result.failure(IllegalStateException("GLM JWT 未配置"))
            }
            val response = api.getQuotaLimit(authorization = effectiveToken)

            if (response.code != 200 || response.data == null) {
                return Result.failure(IllegalStateException(
                    response.msg ?: "GLM 接口返回错误 code=${response.code}"
                ))
            }

            // 包含所有三个窗口：
            // - unit=3: 5小时窗 (TOKENS_LIMIT)
            // - unit=5: MCP 月窗 (TIME_LIMIT) — 计数
            // - unit=6: 周窗 (TOKENS_LIMIT)
            // unit=3 在部分响应中不返回 nextResetTime。网页端使用其他有效窗口的
            // 重置时刻作为账户重置锚点；这里沿用该锚点推导当前 5 小时窗口。
            val inferredFiveHourResetMs = inferFiveHourResetMs(
                response.data.limits.mapNotNull { it.nextResetTime.takeIf { reset -> reset > 0L } }
            )
            // 用量重置额度（重置券）：单独接口，失败不拖垮配额主流程。
            // /api/biz/* 族需要 Referer（GlmApi.getResetQuota 默认值已带）。
            val resetTickets = runCatching { api.getResetQuota(authorization = effectiveToken) }
                .getOrNull()
                ?.takeIf { it.code == 200 }
                ?.data
            val fiveHourTickets = resetTickets?.fiveHourResets.orEmpty()
            val weekTickets = resetTickets?.weekResets.orEmpty()
            val cards = response.data.limits
                .asSequence()
                .filter { limit ->
                    (limit.type == "TOKENS_LIMIT" && (limit.unit == 3 || limit.unit == 6)) ||
                    (limit.type == "TIME_LIMIT" && limit.unit == 5)
                }
                .map { limit ->
                    val effectiveResetMs = if (limit.unit == 3 && limit.nextResetTime <= 0L) {
                        inferredFiveHourResetMs
                    } else limit.nextResetTime
                    val tickets = when (limit.unit) {
                        3 -> fiveHourTickets
                        6 -> weekTickets
                        else -> emptyList()
                    }
                    limit.toGlmQuota(response.data.level, effectiveResetMs, tickets)
                }
                // 固定顺序：5小时窗(3) → 周窗(6) → MCP月窗(5)
                .sortedWith(compareBy({ if (it.rawUnit == 5) 10 else if (it.rawUnit == 6) 5 else 0 }, { it.rawUnit }))
                .toList()

            Result.success(cards)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun GlmLimitDto.toGlmQuota(
        level: String?,
        effectiveResetMs: Long,
        resetTickets: List<GlmResetTicketDto> = emptyList()
    ): GlmQuota {
        val displayName = when (unit) {
            3 -> "5小时剩余"
            5 -> "月度用量"
            6 -> "本周剩余"
            else -> "Token 限额"
        }
        val usedPct = percentage.coerceIn(0, 100)
        val remainingPct = 100 - usedPct
        val remainsMs = (effectiveResetMs - System.currentTimeMillis()).coerceAtLeast(0)
        val windowStartMs = if (unit == 3 && effectiveResetMs > 0L) {
            effectiveResetMs - FIVE_HOUR_MS
        } else null
        val windowEndMs = if (unit == 3 && effectiveResetMs > 0L) effectiveResetMs else null
        // 重置券：仅 5 小时窗(3) / 周窗(6) 有；接口失败或无券 → null（UI 隐藏该行）
        val ticketExpireList = if (unit == 3 || unit == 6) {
            resetTickets.mapNotNull { it.expireMsOrNull() }
        } else emptyList()
        val ticketInfo: Pair<Int, Long?>? = if (unit == 3 || unit == 6) {
            if (resetTickets.isEmpty()) null
            else resetTickets.size to ticketExpireList.minOrNull()
        } else null
        return GlmQuota(
            displayName = displayName,
            usedPercent = usedPct,
            remainingPercent = remainingPct,
            nextResetMs = effectiveResetMs,
            remainsMs = remainsMs,
            level = level,
            rawType = type.orEmpty(),
            rawUnit = unit,
            currentUsage = (currentValue ?: 0).toLong(),
            totalUsage = (usage ?: 0).toLong(),
            windowStartMs = windowStartMs,
            windowEndMs = windowEndMs,
            resetQuotaCount = ticketInfo?.first,
            resetQuotaExpireList = ticketExpireList,
            resetQuotaExpireMs = ticketInfo?.second
        )
    }

    /** 重置券过期时间 "yyyy-MM-dd HH:mm:ss" → 毫秒时间戳（北京时间）。解析失败返回 null。 */
    private fun GlmResetTicketDto.expireMsOrNull(): Long? {
        val raw = expireTime?.trim().orEmpty()
        if (raw.isEmpty()) return null
        return runCatching {
            LocalDateTime.parse(raw, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                .atZone(ZoneId.of("Asia/Shanghai"))
                .toInstant()
                .toEpochMilli()
        }.getOrNull()
    }

    /** 从其他窗口的有效 nextResetTime 提取 5 小时周期锚点，找到当前之后的下一次重置。 */
    private fun inferFiveHourResetMs(resetTimes: List<Long>): Long {
        if (resetTimes.isEmpty()) return 0L
        val zone = ZoneId.of("Asia/Shanghai")
        val now = Instant.now().atZone(zone)
        val anchorTime = Instant.ofEpochMilli(resetTimes.first()).atZone(zone).toLocalTime()
        var candidate = now.toLocalDate().atTime(anchorTime).atZone(zone)
        while (candidate.toInstant().toEpochMilli() <= System.currentTimeMillis()) {
            candidate = candidate.plusHours(5)
        }
        return candidate.toInstant().toEpochMilli()
    }

    private companion object {
        const val FIVE_HOUR_MS = 5L * 60L * 60L * 1000L
    }
}
