package io.github.brightennnn.mmtokenmonitor.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import io.github.brightennnn.mmtokenmonitor.domain.model.DarkMode
import io.github.brightennnn.mmtokenmonitor.domain.model.ThemeSettings

@Composable
fun MiniMaxTokenTheme(
    themeSettings: ThemeSettings = ThemeSettings(),
    content: @Composable () -> Unit
) {
    val context = LocalContext.current

    val isDark = when (themeSettings.darkMode) {
        DarkMode.FOLLOW_SYSTEM -> isSystemInDarkTheme()
        DarkMode.LIGHT -> false
        DarkMode.DARK -> true
    }

    val colorScheme = when {
        themeSettings.dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (isDark) {
                if (themeSettings.pureBlack) dynamicDarkPureBlackBackground(context) else dynamicDarkSurfaceTint(context)
            } else {
                dynamicLightSurfaceTint(context)
            }
        }
        isDark -> {
            if (themeSettings.pureBlack) md3eDarkPureBlack() else md3eDark()
        }
        else -> md3eLight()
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}

// ============================================================
// MD3E — 非动态模式（黑白灰基调，无主题色染色）
// surface = 背景（白/深灰/纯黑）
// surfaceContainerHigh = 卡片/框框（比背景稍亮的白/灰）
// ============================================================

// Light
private fun md3eLight() = lightColorScheme(
    primary = Color(0xFF1A1A1A),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE8E8E8),
    onPrimaryContainer = Color(0xFF1A1A1A),
    secondary = Color(0xFF4A4A4A),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF0F0F0),
    onSecondaryContainer = Color(0xFF1A1A1A),
    tertiary = Color(0xFF6A6A6A),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFF5F5F5),
    onTertiaryContainer = Color(0xFF1A1A1A),
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = Color.White,
    onBackground = Color(0xFF1A1A1A),
    surface = Color.White,
    onSurface = Color(0xFF1A1A1A),
    surfaceVariant = Color(0xFFF5F5F5),
    surfaceContainerLow = Color(0xFFF8F8F8),
    surfaceContainer = Color(0xFFF5F5F5),
    surfaceContainerHigh = Color(0xFFECECEC),
    surfaceContainerHighest = Color(0xFFE0E0E0),
    onSurfaceVariant = Color(0xFF4A4A4A),
    outline = Color(0xFF9E9E9E),
    outlineVariant = Color(0xFFE0E0E0),
    inverseSurface = Color(0xFF2D2D2D),
    inverseOnSurface = Color.White,
    inversePrimary = Color(0xFFBDBDBD),
    scrim = Color.Black,
    surfaceTint = Color.Transparent
)

// Dark
private fun md3eDark() = darkColorScheme(
    primary = Color.White,
    onPrimary = Color(0xFF1A1A1A),
    primaryContainer = Color(0xFF3D3D3D),
    onPrimaryContainer = Color.White,
    secondary = Color(0xFFE0E0E0),
    onSecondary = Color(0xFF2D2D2D),
    secondaryContainer = Color(0xFF424242),
    onSecondaryContainer = Color.White,
    tertiary = Color(0xFFBDBDBD),
    onTertiary = Color(0xFF2D2D2D),
    tertiaryContainer = Color(0xFF4A4A4A),
    onTertiaryContainer = Color.White,
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690003),
    errorContainer = Color(0xFF930006),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF1A1A1A),
    onBackground = Color.White,
    surface = Color(0xFF1A1A1A),
    onSurface = Color.White,
    surfaceVariant = Color(0xFF2D2D2D),
    surfaceContainerLow = Color(0xFF252525),
    surfaceContainer = Color(0xFF2D2D2D),
    surfaceContainerHigh = Color(0xFF3D3D3D),
    surfaceContainerHighest = Color(0xFF4D4D4D),
    onSurfaceVariant = Color(0xFFBDBDBD),
    outline = Color(0xFF9E9E9E),
    outlineVariant = Color(0xFF424242),
    inverseSurface = Color.White,
    inverseOnSurface = Color(0xFF1A1A1A),
    inversePrimary = Color(0xFF3D3D3D),
    scrim = Color.Black,
    surfaceTint = Color.Transparent
)

// Dark Pure Black
private fun md3eDarkPureBlack() = darkColorScheme(
    primary = Color.White,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF3D3D3D),
    onPrimaryContainer = Color.White,
    secondary = Color(0xFFE0E0E0),
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF2D2D2D),
    onSecondaryContainer = Color.White,
    tertiary = Color(0xFFBDBDBD),
    onTertiary = Color.Black,
    tertiaryContainer = Color(0xFF424242),
    onTertiaryContainer = Color.White,
    error = Color(0xFFFFB4AB),
    onError = Color.Black,
    errorContainer = Color(0xFF930006),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color.Black,
    onBackground = Color.White,
    surface = Color.Black,
    onSurface = Color.White,
    surfaceVariant = Color(0xFF1A1A1A),
    surfaceContainerLow = Color(0xFF0D0D0D),
    surfaceContainer = Color(0xFF1A1A1A),
    surfaceContainerHigh = Color(0xFF2D2D2D),
    surfaceContainerHighest = Color(0xFF3D3D3D),
    onSurfaceVariant = Color(0xFF9E9E9E),
    outline = Color(0xFF616161),
    outlineVariant = Color(0xFF2D2D2D),
    inverseSurface = Color.White,
    inverseOnSurface = Color.Black,
    inversePrimary = Color(0xFF3D3D3D),
    scrim = Color.Black,
    surfaceTint = Color.Transparent
)

// ============================================================
// 动态取色 — 背景 = 淡淡的主题色（surface tint），卡片 = 亮白
// ============================================================

// 动态浅色：背景 = surface（系统淡淡的浅紫/蓝），卡片 = 亮白
private fun dynamicLightSurfaceTint(context: android.content.Context): ColorScheme {
    val scheme = dynamicLightColorScheme(context)
    return scheme.copy(
        // background = surface，让窗口背景使用带 tint 的 surface 颜色
        background = scheme.surface,
        // 卡片：纯白
        surfaceContainerHigh = Color.White,
        scrim = Color.Black
    )
}

// 动态深色：背景 = surface（系统淡淡深色），卡片 = 稍亮的 surface
private fun dynamicDarkSurfaceTint(context: android.content.Context): ColorScheme {
    val scheme = dynamicDarkColorScheme(context)
    return scheme.copy(
        background = scheme.surface,
        scrim = Color.Black,
        surfaceTint = Color.Transparent
    )
}

// 动态深色 + 纯黑背景：背景/卡片/NavigationBar 全黑
private fun dynamicDarkPureBlackBackground(context: android.content.Context): ColorScheme {
    val scheme = dynamicDarkColorScheme(context)
    return scheme.copy(
        background = Color.Black,
        surface = Color.Black,
        surfaceVariant = Color(0xFF1A1A1A),
        onSurfaceVariant = Color(0xFF9E9E9E),
        outline = Color(0xFF616161),
        outlineVariant = Color(0xFF2D2D2D),
        inverseSurface = Color.White,
        inverseOnSurface = Color.Black,
        scrim = Color.Black,
        surfaceTint = Color.Transparent
    ).let { copied ->
        copied.copy(
            surfaceContainerLow = Color(0xFF0D0D0D),
            surfaceContainer = Color(0xFF1A1A1A),
            surfaceContainerHigh = Color(0xFF2D2D2D),
            surfaceContainerHighest = Color(0xFF3D3D3D)
        )
    }
}
