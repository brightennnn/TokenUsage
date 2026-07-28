package io.github.brightennnn.mmtokenmonitor.widget

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import org.json.JSONObject
import java.io.File

/**
 * Monet 颜色提取器 — 参考 VerveDo
 *
 * 动态取色模式：用 Android 系统 Monet API (dynamicDarkColorScheme / dynamicLightColorScheme)
 * 非动态取色模式：MD3E 固定配色（黑白灰）
 *
 * VerveDo 风格配色规则：
 * - surface: 淡淡的主题色（surface 自带 tint）
 * - surfaceContainerHigh: 稍亮的 surface（卡片/框框用这个）
 * - onSurface: 主文字色
 * - onSurfaceVariant: 次要文字色
 * - outline: 边框色
 * - primary: accent1（主色）
 * - secondary: accent2（副色）
 * - tertiary: accent3（第三色）
 */
object MonetColorExtractor {

    // ============================================================
    // MD3E 固定配色（非动态模式）
    // ============================================================

    // MD3E Light
    private val MD3E_LIGHT_PRIMARY = Color(0xFF1A1A1A)
    private val MD3E_LIGHT_SECONDARY = Color(0xFF4A4A4A)
    private val MD3E_LIGHT_TERTIARY = Color(0xFF6A6A6A)
    private val MD3E_LIGHT_BACKGROUND = Color.White
    private val MD3E_LIGHT_SURFACE = Color.White
    private val MD3E_LIGHT_SURFACE_VARIANT = Color(0xFFF5F5F5)
    private val MD3E_LIGHT_SURFACE_CONTAINER_HIGH = Color(0xFFECECEC)
    private val MD3E_LIGHT_ON_SURFACE = Color(0xFF1A1A1A)
    private val MD3E_LIGHT_ON_SURFACE_VARIANT = Color(0xFF4A4A4A)
    private val MD3E_LIGHT_OUTLINE = Color(0xFF9E9E9E)

    // MD3E Dark
    private val MD3E_DARK_PRIMARY = Color.White
    private val MD3E_DARK_SECONDARY = Color(0xFFE0E0E0)
    private val MD3E_DARK_TERTIARY = Color(0xFFBDBDBD)
    private val MD3E_DARK_BACKGROUND = Color(0xFF1A1A1A)
    private val MD3E_DARK_SURFACE = Color(0xFF1A1A1A)
    private val MD3E_DARK_SURFACE_VARIANT = Color(0xFF2D2D2D)
    private val MD3E_DARK_SURFACE_CONTAINER_HIGH = Color(0xFF3D3D3D)
    private val MD3E_DARK_ON_SURFACE = Color.White
    private val MD3E_DARK_ON_SURFACE_VARIANT = Color(0xFFBDBDBD)
    private val MD3E_DARK_OUTLINE = Color(0xFF9E9E9E)

    // MD3E Pure Black Dark
    private val MD3E_PURE_BLACK_BACKGROUND = Color.Black
    private val MD3E_PURE_BLACK_SURFACE = Color.Black
    private val MD3E_PURE_BLACK_SURFACE_VARIANT = Color(0xFF1A1A1A)
    private val MD3E_PURE_BLACK_SURFACE_CONTAINER_HIGH = Color(0xFF2D2D2D)
    private val MD3E_PURE_BLACK_OUTLINE = Color(0xFF616161)

    private const val WIDGET_DATA_FILE = "widget_data.json"

    // ============================================================
    // MonetColors 数据类
    // ============================================================

    data class MonetColors(
        val primary: Color,             // accent1 — 主色
        val secondary: Color,           // accent2 — 副色
        val tertiary: Color,            // accent3 — 第三色
        val background: Color,           // 背景（= surface）
        val surface: Color,              // surface（淡淡的主题色）
        val surfaceVariant: Color,       // surface 变体
        val surfaceContainerHigh: Color, // 稍亮的 surface（卡片用 = 亮白/亮灰）
        val onSurface: Color,           // 主文字色
        val onSurfaceVariant: Color,     // 次要文字色
        val outline: Color               // 边框/分割线色
    )

    // ============================================================
    // 公开 API
    // ============================================================

    /**
     * 获取完整的 Monet 配色方案
     */
    fun getMonetColors(context: Context): MonetColors {
        val settings = readThemeSettings(context)
        val isDarkTheme = isDarkTheme(context, settings)
        val isPureBlack = settings?.pureBlack == true && isDarkTheme

        // 非动态取色模式 → MD3E 固定配色
        if (settings?.dynamicColor != true || Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            return getMd3eColors(isDarkTheme, isPureBlack)
        }

        // 动态取色模式 → Android 系统 Monet API（参考 VerveDo）
        return getDynamicColors(context, isDarkTheme, isPureBlack)
    }

    /**
     * 获取主色调（accent1）— 兼容旧 API
     */
    fun getAccentColor(context: Context): Int {
        return getMonetColors(context).primary.toArgb()
    }

    /**
     * 获取背景色 — 兼容旧 API
     */
    fun getBackgroundColor(context: Context): Int {
        return getMonetColors(context).background.toArgb()
    }

    /**
     * 获取主文字色 — 兼容旧 API
     */
    fun getTextColor(context: Context): Int {
        return getMonetColors(context).onSurface.toArgb()
    }

    /**
     * 获取次要文字色 — 兼容旧 API
     */
    fun getSecondaryTextColor(context: Context): Int {
        return getMonetColors(context).onSurfaceVariant.toArgb()
    }

    // ============================================================
    // 私有方法
    // ============================================================

    private data class ThemeSettings(
        val dynamicColor: Boolean,
        val darkMode: Int,
        val pureBlack: Boolean
    )

    private fun readThemeSettings(context: Context): ThemeSettings? {
        val file = File(context.filesDir, WIDGET_DATA_FILE)
        if (!file.exists()) return null
        return try {
            val json = JSONObject(file.readText())
            ThemeSettings(
                dynamicColor = json.optBoolean("dynamicColor", true),
                darkMode = json.optInt("darkMode", 0),
                pureBlack = json.optBoolean("pureBlack", false)
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun isDarkTheme(context: Context, settings: ThemeSettings?): Boolean {
        return when (settings?.darkMode) {
            1 -> false
            2 -> true
            else -> {
                val flags = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
                flags == Configuration.UI_MODE_NIGHT_YES
            }
        }
    }

    /**
     * 动态取色模式 — 使用 Android 系统 Monet API
     * 参考 VerveDo: surface = 淡淡的主题色, surfaceContainerHigh = 亮色（卡片用）
     */
    private fun getDynamicColors(
        context: Context,
        isDarkTheme: Boolean,
        isPureBlack: Boolean
    ): MonetColors {
        val scheme = if (isDarkTheme) {
            androidx.compose.material3.dynamicDarkColorScheme(context)
        } else {
            androidx.compose.material3.dynamicLightColorScheme(context)
        }

        // 纯黑模式：surface/background 全黑，卡片用稍亮的黑
        // 动态模式（不纯黑）：背景 = 淡淡主题色，卡片 = 亮白（浅色模式）/稍亮（深色模式）
        val surface: Color
        val background: Color
        val surfaceContainerHigh: Color

        if (isPureBlack) {
            surface = Color.Black
            background = Color.Black
            surfaceContainerHigh = Color(0xFF2D2D2D)
        } else if (!isDarkTheme) {
            // 动态浅色：背景 = surface（淡淡主题色），卡片 = 纯白
            surface = scheme.surface
            background = scheme.surface
            surfaceContainerHigh = Color.White
        } else {
            // 动态深色：背景 = surface，卡片 = 稍亮的 surface
            surface = scheme.surface
            background = scheme.surface
            surfaceContainerHigh = scheme.surfaceContainerHigh
        }

        val surfaceVariant = if (isPureBlack) Color(0xFF1A1A1A) else scheme.surfaceVariant
        val onSurface = if (isPureBlack) Color.White else scheme.onSurface
        val onSurfaceVariant = if (isPureBlack) Color(0xFF9E9E9E) else scheme.onSurfaceVariant
        val outline = if (isPureBlack) Color(0xFF616161) else scheme.outline

        return MonetColors(
            primary = scheme.primary,
            secondary = scheme.secondary,
            tertiary = scheme.tertiary,
            background = background,
            surface = surface,
            surfaceVariant = surfaceVariant,
            surfaceContainerHigh = surfaceContainerHigh,
            onSurface = onSurface,
            onSurfaceVariant = onSurfaceVariant,
            outline = outline
        )
    }

    /**
     * 非动态取色模式 — MD3E 固定配色
     */
    private fun getMd3eColors(isDarkTheme: Boolean, isPureBlack: Boolean): MonetColors {
        return if (isPureBlack) {
            MonetColors(
                primary = MD3E_DARK_PRIMARY,
                secondary = MD3E_DARK_SECONDARY,
                tertiary = MD3E_DARK_TERTIARY,
                background = MD3E_PURE_BLACK_BACKGROUND,
                surface = MD3E_PURE_BLACK_SURFACE,
                surfaceVariant = MD3E_PURE_BLACK_SURFACE_VARIANT,
                surfaceContainerHigh = MD3E_PURE_BLACK_SURFACE_CONTAINER_HIGH,
                onSurface = MD3E_DARK_ON_SURFACE,
                onSurfaceVariant = MD3E_DARK_ON_SURFACE_VARIANT,
                outline = MD3E_PURE_BLACK_OUTLINE
            )
        } else if (isDarkTheme) {
            MonetColors(
                primary = MD3E_DARK_PRIMARY,
                secondary = MD3E_DARK_SECONDARY,
                tertiary = MD3E_DARK_TERTIARY,
                background = MD3E_DARK_BACKGROUND,
                surface = MD3E_DARK_SURFACE,
                surfaceVariant = MD3E_DARK_SURFACE_VARIANT,
                surfaceContainerHigh = MD3E_DARK_SURFACE_CONTAINER_HIGH,
                onSurface = MD3E_DARK_ON_SURFACE,
                onSurfaceVariant = MD3E_DARK_ON_SURFACE_VARIANT,
                outline = MD3E_DARK_OUTLINE
            )
        } else {
            MonetColors(
                primary = MD3E_LIGHT_PRIMARY,
                secondary = MD3E_LIGHT_SECONDARY,
                tertiary = MD3E_LIGHT_TERTIARY,
                background = MD3E_LIGHT_BACKGROUND,
                surface = MD3E_LIGHT_SURFACE,
                surfaceVariant = MD3E_LIGHT_SURFACE_VARIANT,
                surfaceContainerHigh = MD3E_LIGHT_SURFACE_CONTAINER_HIGH,
                onSurface = MD3E_LIGHT_ON_SURFACE,
                onSurfaceVariant = MD3E_LIGHT_ON_SURFACE_VARIANT,
                outline = MD3E_LIGHT_OUTLINE
            )
        }
    }
}
