package io.github.brightennnn.tokenusage.domain.model

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
    val weeklyBoostPermille: Int = 0,
    // 用量重置额度（重置券）— 仅挂「本周剩余量」卡片（前端语义 fullReset = 完整重置，用户拍板归本周）
    // null = 未配置 _token 或拉取失败 → UI 不渲染该行（与 GLM 同策略）
    val resetQuotaCount: Int? = null,
    val resetQuotaExpireList: List<Long> = emptyList(),
    val resetQuotaExpireMs: Long? = null
)
