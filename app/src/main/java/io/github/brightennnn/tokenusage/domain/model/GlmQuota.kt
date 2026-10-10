package io.github.brightennnn.tokenusage.domain.model

/**
 * GLM Coding Plan 单个窗口的展示数据。
 *
 * 字段与 MiniMax IntervalCard/WeeklyCard 口径对齐：
 * - remainingPercent = 100 - usedPercent（剩余%，进度条用这个）
 * - remainsMs = 下次重置的倒计时毫秒数（用于"剩 X天Y时"展示）
 *
 * @param displayName   中文显示名（"5小时窗" / "周窗" / "MCP月窗"）
 * @param usedPercent   已用百分比（0-100）
 * @param remainingPercent 剩余百分比（100 - usedPercent），进度条用这个
 * @param nextResetMs   下次重置时间戳（毫秒）
 * @param remainsMs     剩余时间（毫秒，nextResetMs - now）
 * @param level         用户等级（来自 API data.level）
 * @param rawType       原始 type 字段（TOKENS_LIMIT / TIME_LIMIT）
 * @param rawUnit       原始 unit 字段（3=5小时窗，6=周窗，5=MCP月窗）
 * @param currentUsage  原始当前用量（token数 或 分钟数，取决于 isTimeLimit）
 * @param totalUsage    原始总量（token数 或 分钟数，取决于 isTimeLimit）
 */
data class GlmQuota(
    val displayName: String,
    val usedPercent: Int,
    val remainingPercent: Int,
    val nextResetMs: Long,
    val remainsMs: Long,
    val level: String?,
    val rawType: String,
    val rawUnit: Int,
    val currentUsage: Long = 0L,
    val totalUsage: Long = 0L,
    /** 5 小时窗口的实际起点；其他窗口为空。 */
    val windowStartMs: Long? = null,
    /** 5 小时窗口的实际终点；其他窗口为空。 */
    val windowEndMs: Long? = null
) {
    /** 单位标识：3=5小时窗 */
    val isFiveHourWindow: Boolean get() = rawUnit == 3
    /** 是否为时间维度配额（TIME_LIMIT，如 MCP 月度） */
    val isTimeLimit: Boolean get() = rawType == "TIME_LIMIT"
}
