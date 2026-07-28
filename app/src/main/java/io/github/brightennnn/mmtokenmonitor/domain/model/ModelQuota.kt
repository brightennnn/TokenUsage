package io.github.brightennnn.mmtokenmonitor.domain.model

data class ModelQuota(
    val modelName: String,
    val intervalTotal: Int,
    val intervalUsed: Int,
    val weeklyTotal: Int,
    val weeklyUsed: Int,
    val intervalStartMs: Long,
    val intervalEndMs: Long,
    val weeklyStartMs: Long,
    val weeklyEndMs: Long,
    // 新接口字段 — 以 100% 为分母的剩余比例（API 直接给出）
    val intervalRemainingPercent: Int = 0,
    val weeklyRemainingPercent: Int = 0,
    val intervalStatus: Int = 0,
    val weeklyStatus: Int = 0,
    val intervalRemainsMs: Long = 0L,
    val weeklyRemainsMs: Long = 0L,
    val intervalBoostPermille: Int = 0,
    val weeklyBoostPermille: Int = 0
)
