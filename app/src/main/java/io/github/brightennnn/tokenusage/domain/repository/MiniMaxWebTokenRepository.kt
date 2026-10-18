package io.github.brightennnn.tokenusage.domain.repository

import io.github.brightennnn.tokenusage.domain.model.MiniMaxCredit
import io.github.brightennnn.tokenusage.domain.model.MiniMaxResetCard
import io.github.brightennnn.tokenusage.domain.model.UsagePeriod
import io.github.brightennnn.tokenusage.domain.model.UsageStats
import io.github.brightennnn.tokenusage.domain.model.UsageSummary
import kotlinx.coroutines.flow.Flow

interface MiniMaxWebTokenRepository {

    /** 当前保存的网页 _token（Flow）。空表示未配置。 */
    val webToken: Flow<String?>

    /** 保存/替换凭证（只存一条）。 */
    suspend fun saveToken(token: String)

    /** 删除凭证。 */
    suspend fun clearToken()

    /**
     * 拉取用量重置额度（重置券）。
     * @param token _token JWT（默认读 DataStore 中保存的）
     * @return 成功时返回可用券列表
     */
    suspend fun fetchResetCards(token: String = ""): Result<List<MiniMaxResetCard>>

    /**
     * 拉取 Token Plan 积分余额 + 积分包明细（含每笔过期时间）。
     * @param token _token JWT（默认读 DataStore 中保存的）
     */
    suspend fun fetchCredit(token: String = ""): Result<MiniMaxCredit>

    /**
     * 拉取指定周期的用量统计。
     * - DAY/WEEK/MONTH → usage_overview（day 模式额外并发拉 hourly_detail 补模型拆分）
     * - ALL           → usage_summary
     * @param token _token JWT（默认读 DataStore 中保存的）
     */
    suspend fun fetchUsageStats(period: UsagePeriod, token: String = ""): Result<UsageStats>

    /**
     * 拉取长期统计（「全部」周期专属指标：累计天数/连续活跃/排名等）。
     */
    suspend fun fetchUsageSummary(token: String = ""): Result<UsageSummary>
}
