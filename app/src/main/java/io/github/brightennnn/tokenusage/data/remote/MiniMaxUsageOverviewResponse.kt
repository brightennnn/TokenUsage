package io.github.brightennnn.tokenusage.data.remote

import com.google.gson.annotations.SerializedName

/**
 * 用量概览响应（当日 / 近7日 / 近30日）。
 *
 * 真实接口：GET https://www.minimax.cn/backend/account/token_plan/usage_overview?period={day|7d|30d}
 * Header： Cookie: _token=<JWT>
 *
 * ⚠️ period 合法值只有 **day / 7d / 30d** 三个（前端 usage chunk 里写死）。
 *    传 `week` / `month` / `all` **不报错**，静默按默认（7d）返回 —— 三个周期数据看起来一样，
 *    极易误判。前端只有 3 个分段控件，没有「全部」。
 *    「全部」周期要用 usage_summary。
 *
 * 实测样本（2026-10-18）：
 * {"cache_hit_percent":"96.8","language_model_token":68484114,
 *  "trend_total_token":68484114,"trend_average_token":2853504.75,
 *  "video_count":null,"speech_count":null,"image_count":null,
 *  "trend":[{"time_label":"00:00","total_token":0},
 *           {"time_label":"11:00","total_token":6616195}],
 *  "base_resp":{"status_code":0,"status_msg":"success"}}
 *
 * 坑：
 * - `time_label` **粒度随 period 变**：day 是小时（"00:00"~"23:00"，24 点），
 *   7d/30d 是日期（"2026-10-12"，7/30 点）。
 * - `trend[].models` **只有 7d/30d 有**，day 模式该字段不存在 → 当日拿不到模型拆分，
 *   需另调 usage_hourly_detail 补（两者聚合后与本接口 total_token 差异为 0，已交叉验证）。
 * - cache_hit_percent 是字符串带 % 号（"96.8" / "96.70%"），不是数字。
 * - video/speech/image_count 用不到时为 null（别当 0 算）。
 */
data class MiniMaxUsageOverviewResponse(
    @SerializedName("cache_hit_percent")
    val cacheHitPercent: String? = null,

    @SerializedName("language_model_token")
    val languageModelToken: Long = 0L,

    @SerializedName("trend_total_token")
    val trendTotalToken: Long = 0L,

    @SerializedName("trend_average_token")
    val trendAverageToken: Double = 0.0,

    @SerializedName("video_count")
    val videoCount: Int? = null,

    @SerializedName("speech_count")
    val speechCount: Int? = null,

    @SerializedName("image_count")
    val imageCount: Int? = null,

    @SerializedName("trend")
    val trend: List<MiniMaxTrendPointDto> = emptyList(),

    @SerializedName("base_resp")
    val baseResp: MiniMaxBaseResp? = null
)

data class MiniMaxTrendPointDto(
    @SerializedName("time_label")
    val timeLabel: String? = null,

    @SerializedName("total_token")
    val totalToken: Long = 0L,

    /** 仅 7d/30d 有；day 模式不存在该字段 */
    @SerializedName("models")
    val models: List<MiniMaxTrendModelDto>? = null
)

data class MiniMaxTrendModelDto(
    @SerializedName("model")
    val model: String? = null,

    @SerializedName("token")
    val token: Long = 0L
)
