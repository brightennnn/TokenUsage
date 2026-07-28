package io.github.brightennnn.mmtokenmonitor.domain.model

/**
 * Theme Settings
 */
data class ThemeSettings(
    val dynamicColor: Boolean = true,          // DynamicColor 动态颜色开关
    val darkMode: DarkMode = DarkMode.FOLLOW_SYSTEM,  // 深色模式
    val pureBlack: Boolean = false,            // PureBlack 纯黑深色模式
    val hapticFeedback: Boolean = true          // 触感反馈开关
)

/**
 * Dark Mode — 深色模式选项
 */
enum class DarkMode {
    FOLLOW_SYSTEM,  // 跟随系统
    LIGHT,          // 浅色模式
    DARK;           // 深色模式

    companion object {
        fun fromOrdinal(ordinal: Int): DarkMode =
            entries.getOrElse(ordinal) { FOLLOW_SYSTEM }
    }
}
