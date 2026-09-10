package io.github.brightennnn.mmtokenmonitor.widget

/**
 * GLM Widget 显示数据（存储在 glm_widget_data.json）
 */
data class GlmWidgetData(
    val intervalRemainingPercent: Int = 0,   // 5h 窗剩余%
    val intervalPeriod: String = "—",        // "HH:mm-HH:mm"
    val intervalRemainsMs: Long = 0L,
    val weeklyRemainingPercent: Int = 0,     // 周窗剩余%
    val weeklyPeriod: String = "—",          // "MM-dd HH:mm ~ MM-dd HH:mm"
    val weeklyRemainsMs: Long = 0L,
    val lastUpdate: String = "—"
)
