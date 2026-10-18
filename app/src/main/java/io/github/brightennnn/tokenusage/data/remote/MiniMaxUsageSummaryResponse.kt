package io.github.brightennnn.tokenusage.data.remote

import com.google.gson.annotations.SerializedName

/**
 * 长期统计（「全部」周期）响应。
 *
 * 真实接口：GET https://www.minimax.cn/backend/account/token_plan/usage_summary
 * Header： Cookie: _token=<JWT>（无参数）
 *
 * 实测样本（2026-10-18）：
 * {"total_days":205,"total_token_consumed":"6.17B","usage_ranking_percent":1.3651794028274218,
 *  "most_active_day":{"date":"2026-09-19","token_count":"149.95M","image_count":"0",...},
 *  "active_days":161,"current_consecutive_days":84,
 *  "daily_token_usage":[...],"date_model_usage":[...],
 *  "last_update_time":"10-18 21:00","base_resp":{"status_code":0,"status_msg":"success"}}
 *
 * 坑：
 * - `total_token_consumed` 是**格式化字符串** "6.17B"（带单位，不是数字），要展示就用原样。
 * - `usage_ranking_percent` 是 1.365…（越小越强，即"超过 98.63% 的用户"）。
 * - `most_active_day.token_count` 也是格式化字符串 "149.95M"。
 * - `daily_token_usage` 168 条纯日总量；`date_model_usage` 168 条**每天×每模型四分**。
 *   两个数组实测等长、日期一一对应，做长周期曲线用 daily，做模型分析用 date_model。
 * - `last_update_time` 是 "MM-dd HH:mm"，数据有小时级延迟。
 */
data class MiniMaxUsageSummaryResponse(
    @SerializedName("total_days")
    val totalDays: Int = 0,

    /** 格式化字符串，如 "6.17B"（含单位，不可当数字解析） */
    @SerializedName("total_token_consumed")
    val totalTokenConsumed: String? = null,

    @SerializedName("usage_ranking_percent")
    val usageRankingPercent: Double = 0.0,

    @SerializedName("most_active_day")
    val mostActiveDay: MiniMaxMostActiveDayDto? = null,

    @SerializedName("active_days")
    val activeDays: Int = 0,

    @SerializedName("current_consecutive_days")
    val currentConsecutiveDays: Int = 0,

    /**
     * ⚠️ 是一维**纯数字数组**，不是对象数组！实测 2026-10-18：
     *     `"daily_token_usage":[96474686, 39384363, 0]`
     *     顺序与 [dateModelUsage] 的日期一一对应（升序），没有日期字段。
     *     按对象解析会抛：
     *     `Expected BEGIN_OBJECT but was NUMBER at line 1 column 316 path $.daily_token_usage[0]`
     *     —— 真机首次点「全部」就是这么炸的。
     */
    @SerializedName("daily_token_usage")
    val dailyTokenUsage: List<Long> = emptyList(),

    @SerializedName("date_model_usage")
    val dateModelUsage: List<MiniMaxDateModelUsageDto> = emptyList(),

    /** "MM-dd HH:mm"，数据有小时级延迟 */
    @SerializedName("last_update_time")
    val lastUpdateTime: String? = null,

    @SerializedName("base_resp")
    val baseResp: MiniMaxBaseResp? = null
)

data class MiniMaxMostActiveDayDto(
    @SerializedName("date")
    val date: String? = null,

    /** 格式化字符串 "149.95M" */
    @SerializedName("token_count")
    val tokenCount: String? = null
)

data class MiniMaxDateModelUsageDto(
    @SerializedName("date")
    val date: String? = null,

    @SerializedName("models")
    val models: List<MiniMaxDateModelDto> = emptyList(),

    @SerializedName("total_input_token")
    val totalInputToken: Long = 0L,

    @SerializedName("total_cache_read_token")
    val totalCacheReadToken: Long = 0L,

    @SerializedName("total_cache_create_token")
    val totalCacheCreateToken: Long = 0L,

    @SerializedName("total_output_token")
    val totalOutputToken: Long = 0L,

    @SerializedName("total_token")
    val totalToken: Long = 0L,

    /** 字符串带 % 号，如 "96.70%" */
    @SerializedName("cache_hit_percent")
    val cacheHitPercent: String? = null
)

data class MiniMaxDateModelDto(
    @SerializedName("model")
    val model: String? = null,

    @SerializedName("input_token")
    val inputToken: Long = 0L,

    @SerializedName("cache_read_token")
    val cacheReadToken: Long = 0L,

    @SerializedName("cache_create_token")
    val cacheCreateToken: Long = 0L,

    @SerializedName("output_token")
    val outputToken: Long = 0L,

    @SerializedName("total_token")
    val totalToken: Long = 0L,

    /** 字符串带 % 号 */
    @SerializedName("cache_hit_percent")
    val cacheHitPercent: String? = null
)
