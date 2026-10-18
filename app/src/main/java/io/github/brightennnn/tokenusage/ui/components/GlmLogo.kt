package io.github.brightennnn.tokenusage.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

/*
 * 智谱 logo —— 用户提供的原始 SVG 里有两层：
 *   1) st194：fill #2D2D2D + 白色描边的圆方法框（外周框）→ 已按要求去掉
 *   2) st23 ：fill #FFFFFF 的三个图形，拼成一个「Z」（上横 + 斜 + 下横）→ 保留
 *
 * Z 在原 viewBox（0 0 30 30）里的包围盒是 x 5.70~24.30 / y 7.10~22.91
 * —— 也就是 18.60 x 15.81，是个扁的形状。
 *
 * 但底栏里它和正方形的 MiniMax logo 并排站着，矮一截很难看。
 * 所以这里保留 Z 真实比例的同时，把 viewport 补成 24x24 的正方形，
 * 并把 Z 在里面垂直居中（上下各留 4.095dp 白边）。
 * 结果：图标盒子是正方形（底栏对齐），Z 的形状不拉伸变形。
 */
private const val GLM_Z_DISPLAY = 24f

private const val GLM_Z_WIDTH = 18.6f
private const val GLM_Z_HEIGHT = 15.81f

/** Z 距左边 / 距顶部的留白，让它在 24x24 里居中 */
private const val GLM_Z_PAD_X = (GLM_Z_DISPLAY - GLM_Z_WIDTH) / 2f          // 2.7
private const val GLM_Z_PAD_Y = (GLM_Z_DISPLAY - GLM_Z_HEIGHT) / 2f          // 4.095

/** Z 的三个组成部分（原 SVG 里 class="st23" 的三个 path/polygon），坐标已平移 + 居中 */
private val GLM_Z_PATHS = listOf(
    // 上横条：左端切斜角，右端收在上横中
    "M12.47,4.095l-1.3,1.85c-0.2,0.29-0.54,0.47-0.9,0.47h-7.1v-2.33C3.16,4.095,12.47,4.095,12.47,4.095z",
    // 中间斜杠：4 顶点，斜率与原 SVG 一致
    "M21.3,4.095 10.14,19.905 2.7,19.905 13.86,4.095z",
    // 下横条：右端切斜角，左侧收在下横中
    "M11.53,19.905l1.31-1.86c0.2-0.29,0.54-0.47,0.9-0.47h7.09v2.33H11.53z"
)

/** 智谱 logo（只保留 Z，无外框），viewport 为 24x24 正方形；颜色由 LocalContentColor 决定 */
val GlmLogo: ImageVector by lazy {
    ImageVector.Builder(
        name = "GlmLogo",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = GLM_Z_DISPLAY,
        viewportHeight = GLM_Z_DISPLAY
    ).apply {
        GLM_Z_PATHS.forEach { d ->
            addPath(
                pathData = PathParser().parsePathString(d).toNodes(),
                fill = SolidColor(Color.Black),
                fillAlpha = 1.0f
            )
        }
    }.build()
}
