package io.github.brightennnn.mmtokenmonitor.widget

import android.content.Context
import androidx.compose.ui.graphics.Color

/**
 * Widget 颜色配置，根据 app 主题设置生成
 * 使用 VerveDo 风格：
 * - surface: 淡淡的主题色
 * - surfaceContainerHigh: 稍亮的 surface（卡片/框框用）
 * - primary: accent1（主色）
 * - secondary: accent2（副色）
 * - tertiary: accent3（第三色）
 */
data class WidgetColors(
    val primary: Color,             // accent1 — 主色（进度条、primary 文字）
    val secondary: Color,          // accent2 — 副色（次要文字）
    val tertiary: Color,            // accent3 — 第三色
    val background: Color,          // 背景（= surface）
    val surface: Color,             // surface（淡淡的主题色）
    val surfaceVariant: Color,      // surface 变体
    val surfaceContainerHigh: Color, // 稍亮的 surface（卡片用 = 亮白/亮灰）
    val onSurface: Color,           // 主文字色
    val onSurfaceVariant: Color,   // 次要文字色
    val outline: Color              // 边框色
) {
    companion object {
        fun fromContext(context: Context): WidgetColors {
            val colors = MonetColorExtractor.getMonetColors(context)
            return WidgetColors(
                primary = colors.primary,
                secondary = colors.secondary,
                tertiary = colors.tertiary,
                background = colors.background,
                surface = colors.surface,
                surfaceVariant = colors.surfaceVariant,
                surfaceContainerHigh = colors.surfaceContainerHigh,
                onSurface = colors.onSurface,
                onSurfaceVariant = colors.onSurfaceVariant,
                outline = colors.outline
            )
        }
    }
}
