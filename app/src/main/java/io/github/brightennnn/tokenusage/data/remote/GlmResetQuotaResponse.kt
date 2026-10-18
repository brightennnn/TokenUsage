package io.github.brightennnn.tokenusage.data.remote

import com.google.gson.annotations.SerializedName

/**
 * GLM 用量重置额度（重置券）响应。
 *
 * 真实接口：GET https://open.bigmodel.cn/api/biz/customer-package-reset/list?targetType=PERSONAL
 * Header： Authorization: <bigmodel_token_production JWT>（不带 Bearer）
 *
 * 坑（2026-10-16 实测）：
 * - 本接口属于 /api/biz/ 族（biz 后缀非通配），**裸 HTTP 不带 Referer 会被 401**（"token expired or incorrect"）。
 *   与 /api/monitor/usage/quota/limit 不同，必须带 Referer: https://open.bigmodel.cn/。
 * - 子路径写错时返回 HTTP 200 + {"code":404,"msg":"接口不存在: /xxx"}，不报 HTTP 错误。
 * - 业务规则（官网原文）：每条次数有独立有效期，过期自动失效；重置周额度时同步重置 5h 额度，
 *   且不额外消耗 5h 次数。仅返回未使用或近 7 天已过期的次数。
 */
data class GlmResetQuotaResponse(
    @SerializedName("code")
    val code: Int = 0,
    @SerializedName("msg")
    val msg: String? = null,
    @SerializedName("data")
    val data: GlmResetQuotaData? = null,
    @SerializedName("success")
    val success: Boolean = false
)

data class GlmResetQuotaData(
    @SerializedName("customerId")
    val customerId: String? = null,
    @SerializedName("targetType")
    val targetType: String? = null,

    /** 上次使用 5 小时额度重置券的时间 */
    @SerializedName("lastFiveHourResetTime")
    val lastFiveHourResetTime: String? = null,

    /** 上次使用周额度重置券的时间 */
    @SerializedName("lastWeekResetTime")
    val lastWeekResetTime: String? = null,

    /** 可用的 5 小时额度重置券 */
    @SerializedName("fiveHourResets")
    val fiveHourResets: List<GlmResetTicketDto> = emptyList(),

    /** 可用的周额度重置券 */
    @SerializedName("weekResets")
    val weekResets: List<GlmResetTicketDto> = emptyList()
)

data class GlmResetTicketDto(
    @SerializedName("recordId")
    val recordId: Long = 0L,

    /** 发放类型：DIRECT=直发，ACTIVITY=活动获得 */
    @SerializedName("grantType")
    val grantType: String? = null,

    /** 过期时间，格式 "yyyy-MM-dd HH:mm:ss"（接口字符串，非时间戳） */
    @SerializedName("expireTime")
    val expireTime: String? = null,

    @SerializedName("available")
    val available: Boolean = true
)
