package io.github.brightennnn.mmtokenmonitor.domain.model

/**
 * App 页面字体大小配置（7个独立字号）
 */
data class AppFontSizes(
    val modelTitle: Int = 16,      // 模型名标题
    val intervalLabel: Int = 12,   // 5小时用量（09:00 - 14:00）
    val intervalNumber: Int = 14,   // 1593 / 15000
    val intervalPercent: Int = 12,  // 百分比
    val weeklyLabel: Int = 12,      // 本周用量（04-28 ~ 05-04）
    val weeklyNumber: Int = 14,    // 用量数字
    val weeklyPercent: Int = 12     // 百分比
)

/**
 * Widget 字体大小配置（9个独立字号，两个 widget 共用一套）
 */
data class WidgetFontSizes(
    val title: Int = 14,           // 标题
    val number: Int = 12,          // 用量数字
    val progressBar: Int = 10,     // 进度条字符
    val percent: Int = 10,         // 百分比
    val updateTime: Int = 8        // 更新时间
)
