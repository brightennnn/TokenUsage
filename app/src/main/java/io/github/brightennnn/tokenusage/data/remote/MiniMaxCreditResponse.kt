package io.github.brightennnn.tokenusage.data.remote

import com.google.gson.annotations.SerializedName

/**
 * MiniMax 积分余额响应。
 *
 * 真实接口：GET https://www.minimax.cn/backend/account/token_plan_credit
 * Header： Cookie: _token=<JWT>（网页登录态）
 *
 * 实测样本（2026-10-18）：
 * {"total_credits":4000,"used_credits":1899,"remaining_credits":2101,
 *  "api_key":"sk-cp-...",
 *  "balance_breakdown":{"total_balance":2101,"buckets":[
 *      {"source":3,"balance":2101,"expire_time_ms":1793358663250,"order_id_prefix":"GIFT"}]},
 *  "credit_packages_details":[
 *      {"combo_id":"330001","package_name":"Token Plan Credit","total_count":2000,
 *       "usage_count":17,"remains_count":1983,"status":1,
 *       "effective_time":1759190400000,"expiration_time":1793232000000,"models":[...]}],
 *  "base_resp":{"status_code":0,"status_msg":"success"}}
 *
 * 坑：
 * - ⚠️ 与 `/backend/account/usage` 完全不是一回事！后者返回的 credit_remains 是
 *   **开放平台按量付费**（media/Hailuo）的积分，Token Plan 用户恒为 0，别被它骗。
 * - `credit_packages_details` 是**多笔积分包**，每笔有独立过期时间。实测 2 笔各 2000：
 *   1983 分那笔 10-30 到期（几乎没动），118 分那笔 11-11 到期（快用完）。
 *   扣减按到期日先后，所以 10-30 那笔会先被消耗。
 * - 该端点顺带返回 `api_key`（sk-cp 格式），即网页 _token 能反查 Coding Plan Key。
 * - Referer 不是必需的（不带也能 200）。
 */
data class MiniMaxCreditResponse(
    @SerializedName("total_credits")
    val totalCredits: Long = 0L,

    @SerializedName("used_credits")
    val usedCredits: Long = 0L,

    @SerializedName("remaining_credits")
    val remainingCredits: Long = 0L,

    @SerializedName("credit_packages_details")
    val creditPackages: List<MiniMaxCreditPackageDto> = emptyList(),

    @SerializedName("balance_breakdown")
    val balanceBreakdown: MiniMaxBalanceBreakdownDto? = null,

    @SerializedName("base_resp")
    val baseResp: MiniMaxBaseResp? = null
)

data class MiniMaxCreditPackageDto(
    /** 实测是 int（330001），不是字符串 */
    @SerializedName("combo_id")
    val comboId: Int? = null,

    @SerializedName("package_name")
    val packageName: String? = null,

    /**
     * ⚠️ 实测这三个计数字段都是**字符串**，不是数字！
     *     "total_count":"2000" / "usage_count":"17" / "remains_count":"1983"
     *     Gson 能宽松把 "2000" 解析成 Long，但别依赖这个 —— 显式按 String 收再自己转。
     */
    @SerializedName("total_count")
    val totalCount: String = "0",

    @SerializedName("usage_count")
    val usageCount: String = "0",

    @SerializedName("remains_count")
    val remainsCount: String = "0",

    /** 1 = 生效中 */
    @SerializedName("status")
    val status: Int = 0,

    @SerializedName("effective_time")
    val effectiveTime: Long = 0L,

    @SerializedName("expiration_time")
    val expirationTime: Long = 0L
) {
    val totalCountValue: Long get() = totalCount.toLongOrNull() ?: 0L
    val usageCountValue: Long get() = usageCount.toLongOrNull() ?: 0L
    val remainsCountValue: Long get() = remainsCount.toLongOrNull() ?: 0L
}

data class MiniMaxBalanceBreakdownDto(
    @SerializedName("total_balance")
    val totalBalance: Long = 0L,

    @SerializedName("buckets")
    val buckets: List<MiniMaxBalanceBucketDto> = emptyList()
)

data class MiniMaxBalanceBucketDto(
    /** 3 = GIFT（赠送积分），实测积分全部来自赠送而非充值 */
    @SerializedName("source")
    val source: Int = 0,

    @SerializedName("balance")
    val balance: Long = 0L,

    @SerializedName("expire_time_ms")
    val expireTimeMs: Long = 0L,

    @SerializedName("order_id_prefix")
    val orderIdPrefix: String? = null
)
