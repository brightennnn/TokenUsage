package io.github.brightennnn.tokenusage.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.brightennnn.tokenusage.data.remote.MiniMaxBaseResp
import io.github.brightennnn.tokenusage.data.remote.MiniMaxDateModelUsageDto
import io.github.brightennnn.tokenusage.data.remote.MiniMaxTrendModelDto
import io.github.brightennnn.tokenusage.data.remote.MiniMaxWebApi
import io.github.brightennnn.tokenusage.domain.model.MiniMaxCredit
import io.github.brightennnn.tokenusage.domain.model.MiniMaxCreditPackage
import io.github.brightennnn.tokenusage.domain.model.MiniMaxResetCard
import io.github.brightennnn.tokenusage.domain.model.ModelUsage
import io.github.brightennnn.tokenusage.domain.repository.MiniMaxWebTokenRepository
import io.github.brightennnn.tokenusage.domain.model.UsagePeriod
import io.github.brightennnn.tokenusage.domain.model.UsageStats
import io.github.brightennnn.tokenusage.domain.model.UsageSummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

/**
 * MiniMax 网页登录凭证（_token）仓库实现。
 *
 * 复用 TokenRepositoryImpl 暴露的内部 DataStore (Context.tokenDataStore)，
 * 同一个实例，避免单进程 DataStore 冲突。
 *
 * 凭证单独一个 key (minimax_web_token)，不影响 MiniMax API Key 列表与 GLM JWT。
 * 用途：重置券 + 积分余额 + 用量统计（2026-10-18 用户拍板放开限制）。
 */
@Singleton
class MiniMaxWebTokenRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val api: MiniMaxWebApi
) : MiniMaxWebTokenRepository {

    private object PrefsKeys {
        val WEB_TOKEN = stringPreferencesKey("minimax_web_token")
    }

    override val webToken: Flow<String?> = context.tokenDataStore.data
        .map { prefs -> prefs[PrefsKeys.WEB_TOKEN]?.takeIf { it.isNotBlank() } }

    override suspend fun saveToken(token: String) {
        context.tokenDataStore.edit { prefs ->
            prefs[PrefsKeys.WEB_TOKEN] = token.trim()
        }
    }

    override suspend fun clearToken() {
        context.tokenDataStore.edit { prefs ->
            prefs.remove(PrefsKeys.WEB_TOKEN)
        }
    }

    /** 取 token：入参优先，空则读 DataStore；都没配置直接失败 */
    private suspend fun resolveToken(token: String): String =
        token.ifBlank {
            webToken.first() ?: throw IllegalStateException("MiniMax Token 未配置")
        }

    /**
     * 统一校验 BFF 返回 —— 这族接口失败也返回 HTTP 200，
     * 真错误藏在 base_resp.status_code 里（1004 = not login）。
     */
    private fun requireOk(resp: MiniMaxBaseResp?): String? {
        val code = resp?.statusCode ?: 0
        if (code == 0) return null
        val msg = resp?.statusMsg ?: "接口返回错误 code=$code"
        return if (code == 1004) "Token 无效或已过期（$msg），请重新抓取网页 Cookie" else msg
    }

    override suspend fun fetchResetCards(token: String): Result<List<MiniMaxResetCard>> {
        return try {
            val cookie = "_token=${resolveToken(token)}"
            val response = api.getResetCards(cookie = cookie)
            requireOk(response.baseResp)?.let { return Result.failure(IllegalStateException(it)) }

            Result.success(
                response.cards.map { dto ->
                    MiniMaxResetCard(
                        cardId = dto.cardId.orEmpty(),
                        sourceType = dto.sourceType,
                        expireTimeMs = dto.expireTime,
                        createTimeMs = dto.createTime,
                        outBizNo = dto.outBizNo
                    )
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun fetchCredit(token: String): Result<MiniMaxCredit> {
        return try {
            val cookie = "_token=${resolveToken(token)}"
            val response = api.getCredit(cookie = cookie)
            requireOk(response.baseResp)?.let { return Result.failure(IllegalStateException(it)) }

            Result.success(
                MiniMaxCredit(
                    totalCredits = response.totalCredits,
                    usedCredits = response.usedCredits,
                    remainingCredits = response.remainingCredits,
                    packages = response.creditPackages.map { dto ->
                        MiniMaxCreditPackage(
                            packageName = dto.packageName?.takeIf { it.isNotBlank() } ?: "Token Plan Credit",
                            totalCount = dto.totalCountValue,
                            usageCount = dto.usageCountValue,
                            remainsCount = dto.remainsCountValue,
                            status = dto.status,
                            effectiveTimeMs = dto.effectiveTime,
                            expirationTimeMs = dto.expirationTime
                        )
                    }
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun fetchUsageStats(period: UsagePeriod, token: String): Result<UsageStats> {
        return try {
            val cookie = "_token=${resolveToken(token)}"

            if (period == UsagePeriod.ALL) {
                val resp = api.getUsageSummary(cookie = cookie)
                requireOk(resp.baseResp)?.let { return Result.failure(IllegalStateException(it)) }
                // daily_token_usage 是纯数字数组，与 date_model_usage 的日期按序对应；
                // 优先用它做曲线（接口权威日总量），长度对不上才退回逐行累加。
                val dailyFromApi = resp.dailyTokenUsage
                val rows = resp.dateModelUsage
                val trendTokens = when {
                    dailyFromApi.size == rows.size -> dailyFromApi
                    rows.isEmpty() -> dailyFromApi
                    else -> rows.map { it.totalToken }
                }
                return Result.success(mapSummaryToStats(rows = rows, trendTokens = trendTokens))
            }

            val resp = api.getUsageOverview(cookie = cookie, period = period.apiValue)
            requireOk(resp.baseResp)?.let { return Result.failure(IllegalStateException(it)) }

            // trend 的 time_label 粒度随 period 变：day 是小时 "HH:mm"，其余是日期。
            val labels = resp.trend.mapNotNull { it.timeLabel }
            val tokens = resp.trend.map { it.totalToken }

            // day 模式 trend 不含 models → 额外拉小时明细补模型拆分（聚合总量与 trend 一致）。
            val models = if (period == UsagePeriod.DAY) {
                fetchHourlyModelBreakdown(cookie)
            } else {
                aggregateTrendModels(resp.trend.mapNotNull { it.models })
            }

            Result.success(
                UsageStats(
                    period = period,
                    totalToken = resp.trendTotalToken.takeIf { it > 0L } ?: resp.languageModelToken,
                    cacheHitPercent = parsePercent(resp.cacheHitPercent),
                    averageToken = resp.trendAverageToken.toLong(),
                    trendLabel = labels,
                    trendToken = tokens,
                    modelBreakdown = models,
                    isHourlyGranular = period == UsagePeriod.DAY,
                    // 四分只有「全部」口径能从 date_model_usage 拿到；7d/30d 接口不给。
                    inputToken = if (period == UsagePeriod.ALL) models.sumOf { it.inputToken } else 0L,
                    cacheReadToken = if (period == UsagePeriod.ALL) models.sumOf { it.cacheReadToken } else 0L,
                    cacheCreateToken = if (period == UsagePeriod.ALL) models.sumOf { it.cacheCreateToken } else 0L,
                    outputToken = if (period == UsagePeriod.ALL) models.sumOf { it.outputToken } else 0L
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun fetchUsageSummary(token: String): Result<UsageSummary> {
        return try {
            val cookie = "_token=${resolveToken(token)}"
            val resp = api.getUsageSummary(cookie = cookie)
            requireOk(resp.baseResp)?.let { return Result.failure(IllegalStateException(it)) }

            Result.success(
                UsageSummary(
                    totalDays = resp.totalDays,
                    activeDays = resp.activeDays,
                    currentConsecutiveDays = resp.currentConsecutiveDays,
                    totalTokenConsumed = resp.totalTokenConsumed.orEmpty(),
                    usageRankingPercent = resp.usageRankingPercent,
                    mostActiveDayDate = resp.mostActiveDay?.date,
                    mostActiveDayToken = resp.mostActiveDay?.tokenCount,
                    lastUpdateTime = resp.lastUpdateTime
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 当日模型拆分：usage_hourly_detail 按 小时×模型 给出，聚合到模型维度。
     * 失败不抛 —— overview 已经拿到总量与曲线，模型拆分只是补充。
     */
    private suspend fun fetchHourlyModelBreakdown(cookie: String): List<ModelUsage> {
        return try {
            val today = LocalDate.now(BEIJING_ZONE).toString()
            val resp = api.getHourlyDetail(cookie = cookie, startTime = today, endTime = today)
            requireOk(resp.baseResp) ?: return emptyList()

            val agg = LinkedHashMap<String, TokenAccumulator>()
            resp.entries.forEach { e ->
                val name = e.model?.takeIf { it.isNotBlank() } ?: return@forEach
                agg.getOrPut(name) { TokenAccumulator() }.add(
                    input = e.inputToken,
                    cacheRead = e.cacheReadToken,
                    cacheCreate = e.cacheCreateToken,
                    output = e.outputToken
                )
            }
            val total = agg.values.sumOf { it.total }
            agg.entries
                .map { (name, v) ->
                    ModelUsage(
                        modelName = name,
                        token = v.total,
                        percent = percentOf(v.total, total),
                        inputToken = v.input,
                        cacheReadToken = v.cacheRead,
                        cacheCreateToken = v.cacheCreate,
                        outputToken = v.output
                    )
                }
                .sortedByDescending { it.token }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /** 7d/30d：trend[].models 已按天给模型，直接跨天累加 */
    private fun aggregateTrendModels(
        perDay: List<List<MiniMaxTrendModelDto>>
    ): List<ModelUsage> {
        val agg = HashMap<String, Long>()
        perDay.forEach { day ->
            day.forEach { m ->
                val name = m.model?.takeIf { it.isNotBlank() } ?: return@forEach
                agg[name] = (agg[name] ?: 0L) + m.token
            }
        }
        val total = agg.values.sum()
        return agg.entries
            .map { (name, v) -> ModelUsage(name, v, percentOf(v, total)) }
            .sortedByDescending { it.token }
    }

    /** 「全部」周期：date_model_usage 逐日×模型累加，四分也能拿到 */
    private fun mapSummaryToStats(
        rows: List<MiniMaxDateModelUsageDto>,
        trendTokens: List<Long>
    ): UsageStats {
        val agg = HashMap<String, TokenAccumulator>()
        rows.forEach { row ->
            row.models.forEach { m ->
                val name = m.model?.takeIf { it.isNotBlank() } ?: return@forEach
                agg.getOrPut(name) { TokenAccumulator() }.add(
                    input = m.inputToken,
                    cacheRead = m.cacheReadToken,
                    cacheCreate = m.cacheCreateToken,
                    output = m.outputToken,
                    total = m.totalToken
                )
            }
        }
        val total = agg.values.sumOf { it.total }
        val models = agg.entries
            .map { (name, v) ->
                ModelUsage(
                    modelName = name,
                    token = v.total,
                    percent = percentOf(v.total, total),
                    inputToken = v.input,
                    cacheReadToken = v.cacheRead,
                    cacheCreateToken = v.cacheCreate,
                    outputToken = v.output
                )
            }
            .sortedByDescending { it.token }

        // 缓存命中率：date_model_usage 每行都带 cache_hit_percent，按 cache_read/(input+cache_read) 整体算更准。
        val input = models.sumOf { it.inputToken }
        val cacheRead = models.sumOf { it.cacheReadToken }
        val hit = if (input + cacheRead > 0L) (cacheRead * 100.0) / (input + cacheRead) else 0.0

        return UsageStats(
            period = UsagePeriod.ALL,
            totalToken = total,
            cacheHitPercent = hit,
            averageToken = if (rows.isEmpty()) 0L else total / rows.size,
            trendLabel = rows.mapNotNull { it.date },
            trendToken = trendTokens,
            modelBreakdown = models,
            isHourlyGranular = false,
            inputToken = input,
            cacheReadToken = cacheRead,
            cacheCreateToken = models.sumOf { it.cacheCreateToken },
            outputToken = models.sumOf { it.outputToken }
        )
    }

    companion object {
        private val BEIJING_ZONE: ZoneId = ZoneId.of("Asia/Shanghai")

        /** 接口返回 "96.8" / "96.70%" / "96.9" 多种形态，统一成数字 */
        private fun parsePercent(raw: String?): Double {
            if (raw.isNullOrBlank()) return 0.0
            return raw.removeSuffix("%").trim().toDoubleOrNull() ?: 0.0
        }

        private fun percentOf(part: Long, total: Long): Double {
            if (total <= 0L) return 0.0
            return (part * 100.0) / total
        }
    }
}

/**
 * 逐日/逐小时聚合 token 四分。
 *
 * 注意 total 不等于 input+cache_read+cache_create+output 的简单相加口径差异：
 * hourly_detail 没给 total 字段，需要自己按四项加总；而 usage_summary 的
 * total_token 是接口直接给的权威值。所以 [add] 的 total 参数可传 0 表示自行加总。
 */
private class TokenAccumulator {
    var input: Long = 0L
    var cacheRead: Long = 0L
    var cacheCreate: Long = 0L
    var output: Long = 0L

    /** 接口给的权威总量；为 0 时按四项加总 */
    private var explicitTotal: Long = 0L
    private var computedTotal: Long = 0L

    fun add(
        input: Long = 0L,
        cacheRead: Long = 0L,
        cacheCreate: Long = 0L,
        output: Long = 0L,
        total: Long = 0L
    ) {
        this.input += input
        this.cacheRead += cacheRead
        this.cacheCreate += cacheCreate
        this.output += output
        computedTotal += input + cacheRead + cacheCreate + output
        explicitTotal += total
    }

    val total: Long get() = if (explicitTotal > 0L) explicitTotal else computedTotal
}
