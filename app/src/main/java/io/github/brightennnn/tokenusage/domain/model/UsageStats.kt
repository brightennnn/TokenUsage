package io.github.brightennnn.tokenusage.domain.model

/**
 * 用量周期（App 支持的四个分段）。
 *
 * 对应关系：
 * - [DAY]  / [WEEK] / [MONTH] → usage_overview?period=day|7d|30d
 * - [ALL]                       → usage_summary（无 period 参数）
 *
 * ⚠️ usage_overview 的 period 合法值只有 day/7d/30d（前端写死），
 *    传 week/month/all 会静默按 7d 返回。「全部」只能走 usage_summary。
 */
enum class UsagePeriod(val apiValue: String, val displayName: String) {
    DAY("day", "当日"),
    WEEK("7d", "近7日"),
    MONTH("30d", "近30日"),
    ALL("all", "全部")
}

/**
 * 单个周期的用量统计。
 *
 * @property totalToken        区间总 token
 * @property cacheHitPercent   缓存命中率（0-100 的浮点）
 * @property averageToken      日均 token（day 模式下按 24 小时摊平）
 * @property trendLabel        曲线横坐标：day 是小时 "HH:mm"，其余是日期 "yyyy-MM-dd"
 * @property trendToken        曲线纵坐标，与 trendLabel 一一对应
 * @property modelBreakdown    模型分布，按 token 降序
 * @property isHourlyGranular  true = 当日（小时粒度）
 */
data class UsageStats(
    val period: UsagePeriod,
    val totalToken: Long,
    val cacheHitPercent: Double,
    val averageToken: Long,
    val trendLabel: List<String>,
    val trendToken: List<Long>,
    val modelBreakdown: List<ModelUsage>,
    val isHourlyGranular: Boolean,
    val inputToken: Long = 0L,
    val cacheReadToken: Long = 0L,
    val cacheCreateToken: Long = 0L,
    val outputToken: Long = 0L
) {
    val hasData: Boolean get() = totalToken > 0L || trendToken.any { it > 0L }
}

data class ModelUsage(
    val modelName: String,
    val token: Long,
    /** 该模型占当前周期总量的百分比（0-100） */
    val percent: Double,
    val inputToken: Long = 0L,
    val cacheReadToken: Long = 0L,
    val cacheCreateToken: Long = 0L,
    val outputToken: Long = 0L
)

/**
 * 「全部」周期的长期统计（来自 usage_summary）。
 *
 * 与 [UsageStats] 分开建模：usage_summary 没有 trend 曲线结构，
 * 但有 total_days / active_days / 连续活跃 / 排名等只有长期口径才有的指标。
 */
data class UsageSummary(
    val totalDays: Int,
    val activeDays: Int,
    val currentConsecutiveDays: Int,
    /** 已格式化的累计 token，如 "6.17B"（原样展示，不解析） */
    val totalTokenConsumed: String,
    /** 用量排名百分比，越小越强（1.37 = 超过 98.63% 的用户） */
    val usageRankingPercent: Double,
    val mostActiveDayDate: String?,
    val mostActiveDayToken: String?,
    val lastUpdateTime: String?
) {
    /** 累计活跃占比（active_days / total_days），0-100 */
    val activeRatePercent: Int
        get() = if (totalDays <= 0) 0
        else ((activeDays * 100.0) / totalDays).toInt().coerceIn(0, 100)
}
