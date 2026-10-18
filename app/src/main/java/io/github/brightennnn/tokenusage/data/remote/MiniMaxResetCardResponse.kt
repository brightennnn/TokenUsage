package io.github.brightennnn.tokenusage.data.remote

import com.google.gson.annotations.SerializedName

/**
 * MiniMax 用量重置额度（重置券 / Reset Card）响应。
 *
 * 真实接口：GET https://www.minimax.cn/backend/account/reset_card/list?page=1&size=20
 * Header： Cookie: _token=<JWT>（网页登录态）
 *
 * 实测样本（2026-10-17）：
 * {"cards":[{"card_id":"144100","source_type":"mcode_signin","expire_time":1793030400000,
 *            "create_time":1791810281566,"out_biz_no":"2109620150125748753:1"}],
 *  "total":7,"base_resp":{"status_code":0,"status_msg":"success"}}
 *
 * 坑：
 * - 默认 size=5 会截断（total 与 cards.length 不一致），调用方传大一点的 size。
 * - 未登录/过期返回 HTTP 200 + status_code=1004 "not login" —— 判定成功看 status_code，不看 HTTP。
 */
data class MiniMaxResetCardResponse(
    @SerializedName("cards")
    val cards: List<MiniMaxResetCardDto> = emptyList(),

    /** 总张数（可能大于 cards.length —— 分页 size 限制） */
    @SerializedName("total")
    val total: Int = 0,

    @SerializedName("base_resp")
    val baseResp: MiniMaxBaseResp? = null
)

data class MiniMaxResetCardDto(
    @SerializedName("card_id")
    val cardId: String? = null,

    /** 发放来源，实测值 mcode_signin（MiniMax Code 签到） */
    @SerializedName("source_type")
    val sourceType: String? = null,

    /** 过期时间（毫秒时间戳） */
    @SerializedName("expire_time")
    val expireTime: Long = 0L,

    /** 发放时间（毫秒时间戳） */
    @SerializedName("create_time")
    val createTime: Long = 0L,

    /** 使用券时 POST /use 要带的业务单号 */
    @SerializedName("out_biz_no")
    val outBizNo: String? = null
)

data class MiniMaxBaseResp(
    @SerializedName("status_code")
    val statusCode: Int = 0,

    @SerializedName("status_msg")
    val statusMsg: String? = null
)
