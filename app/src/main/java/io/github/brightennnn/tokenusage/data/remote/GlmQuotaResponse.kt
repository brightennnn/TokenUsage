package io.github.brightennnn.tokenusage.data.remote

import com.google.gson.annotations.SerializedName

/**
 * GLM Coding Plan 用量响应（顶层）。
 *
 * 真实接口:GET https://open.bigmodel.cn/api/monitor/usage/quota/limit
 * Header:  Authorization: <bigmodel_token_production JWT> (不带 Bearer)
 */
data class GlmQuotaResponse(
    @SerializedName("code")
    val code: Int = 0,
    @SerializedName("msg")
    val msg: String? = null,
    @SerializedName("data")
    val data: GlmQuotaData? = null,
    @SerializedName("success")
    val success: Boolean = false
)

data class GlmQuotaData(
    @SerializedName("limits")
    val limits: List<GlmLimitDto> = emptyList(),
    @SerializedName("level")
    val level: String? = null
)

data class GlmLimitDto(
    /**
     * 限额类型
     *   TOKENS_LIMIT  — token 计数型限额（5h窗 / 周窗）
     *   TIME_LIMIT    — 调用次数型限额（工具月窗，本次不展示）
     */
    @SerializedName("type")
    val type: String? = null,

    /**
     * 窗口编码（智谱内部编号，非小时/天）
     *   3 = 5小时 token 窗
     *   5 = 工具月窗
     *   6 = 周 token 窗
     */
    @SerializedName("unit")
    val unit: Int = 0,

    @SerializedName("number")
    val number: Int = 0,
    @SerializedName("usage")
    val usage: Int = 0,
    @SerializedName("currentValue")
    val currentValue: Int = 0,
    @SerializedName("remaining")
    val remaining: Int = 0,

    /** 已用百分比（0-100）。TOKENS_LIMIT 是百分比；TIME_LIMIT 是当前次数百分比。 */
    @SerializedName("percentage")
    val percentage: Int = 0,

    /** 下次重置时间（毫秒时间戳） */
    @SerializedName("nextResetTime")
    val nextResetTime: Long = 0L,

    /** 仅 TIME_LIMIT 有：按模型维度的分解 */
    @SerializedName("usageDetails")
    val usageDetails: List<GlmUsageDetailDto>? = null
)

data class GlmUsageDetailDto(
    @SerializedName("modelCode")
    val modelCode: String? = null,
    @SerializedName("usage")
    val usage: Int = 0
)
