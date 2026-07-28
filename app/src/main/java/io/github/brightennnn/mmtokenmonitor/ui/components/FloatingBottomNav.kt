package io.github.brightennnn.mmtokenmonitor.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.LocalTextStyle

/**
 * 悬浮底栏：整体有圆角+阴影，悬浮在内容上方
 * - 整体圆角、整体阴影，悬浮感
 * - Monet 启用时背景跟随 surfaceContainerHigh（动态取色）
 * - Monet 关闭时背景为白色（亮色）/ #2C2C2C（暗色）
 * - 与界面衔接处可有渐隐或透明效果
 */
@Composable
fun FloatingBottomNav(
    selectedIndex: Int,
    items: List<BottomNavItem>,
    onItemClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    // 根据 Monet 是否启用决定背景色
    val backgroundColor = if (isMonetEnabled()) {
        MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.85f)
    } else {
        if (MaterialTheme.colorScheme.surface.luminance() > 0.5f) {
            Color.White.copy(alpha = 0.85f)
        } else {
            Color(0xFF2C2C2C).copy(alpha = 0.85f)
        }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 48.dp)
            .padding(bottom = 24.dp),
        shape = RoundedCornerShape(28.dp),
        color = backgroundColor,
        shadowElevation = 8.dp,
        tonalElevation = 6.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, item ->
                val isSelected = index == selectedIndex

                val iconColor by animateColorAsState(
                    targetValue = if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    },
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                    label = "iconColor"
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(28.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(bounded = true, radius = 28.dp, color = Color(0x40000000))
                        ) { onItemClick(index) },
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        tint = iconColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = item.label,
                        style = LocalTextStyle.current.copy(
                            fontSize = 10.sp,
                            color = iconColor
                        ),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
    }
}

/** 近似计算亮度 */
private fun Color.luminance(): Float {
    val r = red
    val g = green
    val b = blue
    return 0.299f * r + 0.587f * g + 0.114f * b
}

/** 判断 Monet 动态取色是否启用：检查 surfaceContainerHigh 是否与 surface 明显不同 */
@Composable
private fun isMonetEnabled(): Boolean {
    val surface = MaterialTheme.colorScheme.surface
    val surfaceContainerHigh = MaterialTheme.colorScheme.surfaceContainerHigh
    return kotlin.math.abs(surface.luminance() - surfaceContainerHigh.luminance()) > 0.01f
}

data class BottomNavItem(
    val label: String,
    val icon: ImageVector
)
