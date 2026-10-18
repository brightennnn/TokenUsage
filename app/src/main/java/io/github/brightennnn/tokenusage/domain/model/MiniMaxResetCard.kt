package io.github.brightennnn.tokenusage.domain.model

/**
 * MiniMax 用量重置额度（重置券）展示模型。
 *
 * 数据来源：GET https://www.minimax.cn/backend/account/reset_card/list
 * 规则：每张券有独立过期时间，过期自动失效；接口只返回未过期的可用券。
 */
data class MiniMaxResetCard(
    val cardId: String = "",
    val sourceType: String? = null,
    val expireTimeMs: Long = 0L,
    val createTimeMs: Long = 0L,
    val outBizNo: String? = null
) {
    val isExpired: Boolean
        get() = expireTimeMs in 1 until System.currentTimeMillis()
}
