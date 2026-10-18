package io.github.brightennnn.tokenusage.domain.model

/**
 * MiniMax 积分余额。
 *
 * 数据源：/backend/account/token_plan_credit（_token 鉴权）。
 * 拿到的是 Token Plan 积分，与开放平台按量付费的 credit_remains 无关。
 */
data class MiniMaxCredit(
    val totalCredits: Long,
    val usedCredits: Long,
    val remainingCredits: Long,
    val packages: List<MiniMaxCreditPackage>
) {
    /** 剩余占总积分比例（0-100）；总积分为 0 时返回 0 */
    val remainingPercent: Int
        get() = if (totalCredits <= 0L) 0
        else ((remainingCredits * 100.0) / totalCredits).toInt().coerceIn(0, 100)

    /** 按过期时间升序 —— 扣减按到期日先后，最早过期的会先被消耗 */
    val sortedPackages: List<MiniMaxCreditPackage>
        get() = packages.sortedBy { it.expirationTimeMs }
}

data class MiniMaxCreditPackage(
    val packageName: String,
    val totalCount: Long,
    val usageCount: Long,
    val remainsCount: Long,
    val status: Int,
    val effectiveTimeMs: Long,
    val expirationTimeMs: Long
) {
    val isActive: Boolean get() = status == 1

    /** 距过期不足 7 天且有余额 —— UI 标红提醒 */
    val isExpiringSoon: Boolean
        get() {
            if (!isActive || remainsCount <= 0L || expirationTimeMs <= 0L) return false
            val remaining = expirationTimeMs - System.currentTimeMillis()
            return remaining in 1 until SEVEN_DAYS_MS
        }

    val isExpired: Boolean
        get() = expirationTimeMs in 1 until System.currentTimeMillis()

    companion object {
        const val SEVEN_DAYS_MS = 7L * 24 * 60 * 60 * 1000
    }
}
