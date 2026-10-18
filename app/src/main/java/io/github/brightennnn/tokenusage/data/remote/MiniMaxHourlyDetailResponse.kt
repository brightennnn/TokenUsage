package io.github.brightennnn.tokenusage.data.remote

import com.google.gson.annotations.SerializedName

/**
 * 小时明细响应（补当日模型拆分用）。
 *
 * 真实接口：
 *   GET https://www.minimax.cn/backend/account/token_plan/usage_hourly_detail
 *       ?start_time=2026-10-18&end_time=2026-10-18
 *   Header： Cookie: _token=<JWT>
 *
 * 实测样本（2026-10-18）：
 * {"entries":[{"time_range":"2026/10/18 20:00～21:00","source":"-",
 *              "model":"MiniMax-M3.1-Flash-Preview","biz_type":"Text",
 *              "input_token":862453,"output_token":171273,
 *              "cache_read_token":26740736,"cache_create_token":0,
 *              "cache_hit_percent":"96.9"}],
 *  "base_resp":{"status_code":0,"status_msg":"success"}}
 *
 * 存在的理由：`usage_overview?period=day` 的 trend[] **不含 models 字段**，
 * 当日拿不到模型拆分。本接口按 小时×模型 给出明细，聚合后总量与 overview 的
 * trend_total_token **完全一致（差异 0，已交叉验证 2026-10-18）**。
 *
 * 坑（三个都踩过）：
 * - 参数名是 `start_time`/`end_time`，**没有 `_ms` 后缀**；传 `start_time_ms` 会报
 *   "start_time/end_time required"。
 * - 值必须是**纯日期字符串** "2006-01-02"，如 `start_time=2026-10-18`。
 *   传 Unix 秒时间戳会报：
 *   `invalid start_time: parsing time "1792252800" as "2006-01-02"`。
 * - `start_time` 与 `end_time` 相同 = 只查当天；给区间则返回区间内所有小时的明细。
 * - 逐小时聚合 total 时注意：total = input + cache_read + cache_create + output
 *   （不是只加 input）。
 */
data class MiniMaxHourlyDetailResponse(
    @SerializedName("entries")
    val entries: List<MiniMaxHourlyEntryDto> = emptyList(),

    @SerializedName("base_resp")
    val baseResp: MiniMaxBaseResp? = null
)

data class MiniMaxHourlyEntryDto(
    /** "2026/10/18 20:00～21:00" */
    @SerializedName("time_range")
    val timeRange: String? = null,

    @SerializedName("source")
    val source: String? = null,

    @SerializedName("model")
    val model: String? = null,

    /** Text / Video / Image… */
    @SerializedName("biz_type")
    val bizType: String? = null,

    @SerializedName("input_token")
    val inputToken: Long = 0L,

    @SerializedName("output_token")
    val outputToken: Long = 0L,

    @SerializedName("cache_read_token")
    val cacheReadToken: Long = 0L,

    @SerializedName("cache_create_token")
    val cacheCreateToken: Long = 0L,

    /** 字符串，纯数字无 % 号（与 overview 的 "96.70%" 带号不同，注意解析） */
    @SerializedName("cache_hit_percent")
    val cacheHitPercent: String? = null
)
