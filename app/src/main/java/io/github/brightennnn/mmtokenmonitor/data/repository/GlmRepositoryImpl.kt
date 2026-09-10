package io.github.brightennnn.mmtokenmonitor.data.repository

import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.brightennnn.mmtokenmonitor.data.remote.GlmApi
import io.github.brightennnn.mmtokenmonitor.data.remote.GlmLimitDto
import io.github.brightennnn.mmtokenmonitor.domain.model.GlmQuota
import io.github.brightennnn.mmtokenmonitor.domain.repository.GlmRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.ZoneId
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
                    limit.toGlmQuota(response.data.level, effectiveResetMs)
                }
                // 固定顺序：5小时窗(3) → 周窗(6) → MCP月窗(5)
                .sortedWith(compareBy({ if (it.rawUnit == 5) 10 else if (it.rawUnit == 6) 5 else 0 }, { it.rawUnit }))
                .toList()

            Result.success(cards)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun GlmLimitDto.toGlmQuota(level: String?, effectiveResetMs: Long): GlmQuota {
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
            windowEndMs = windowEndMs
        )
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
