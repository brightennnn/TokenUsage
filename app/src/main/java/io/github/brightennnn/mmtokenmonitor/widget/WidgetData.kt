package io.github.brightennnn.mmtokenmonitor.widget

data class WidgetData(
    val modelName: String = "—",
    val intervalRemainingPercent: Int = 0,
    val intervalStartMs: Long = 0L,
    val intervalEndMs: Long = 0L,
    val intervalRemainsMs: Long = 0L,
    val weeklyRemainingPercent: Int = 0,
    val weeklyStartMs: Long = 0L,
    val weeklyEndMs: Long = 0L,
    val weeklyRemainsMs: Long = 0L,
    val lastUpdate: String = "—",
    val intervalPeriod: String = "—",
    val weeklyPeriod: String = "—",
    // Widget 字体大小
    val widgetTitleSize: Int = 14,
    val widgetNumberSize: Int = 12,
    val widgetProgressBarSize: Int = 10,
    val widgetPercentSize: Int = 10,
    val widgetUpdateTimeSize: Int = 8
)
