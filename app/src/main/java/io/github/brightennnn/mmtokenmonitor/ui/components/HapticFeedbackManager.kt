package io.github.brightennnn.mmtokenmonitor.ui.components

import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.runtime.CompositionLocal
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.platform.LocalView

/**
 * CompositionLocal: 当前是否启用触感反馈
 */
val LocalHapticFeedbackEnabled = compositionLocalOf { true }

/**
 * 触感反馈工具类，参考 VerveDo 的 VibrationUtils 实现。
 * 在 onClick / onCheckedChange 等回调里直接调用。
 *
 * 使用方式:
 *   val view = LocalView.current
 *   IconButton(onClick = { HapticFeedback.perform(view) })
 *   Switch(..., onCheckedChange = { HapticFeedback.perform(view) })
 */
object HapticFeedback {
    /**
     * 当前是否启用（由外部通过 DataStore 等同步更新）
     */
    @Volatile
    var isEnabled: Boolean = true

    /**
     * 在视图上触发触感反馈（默认 CONTEXT_CLICK）。
     * 只有 isEnabled 为 true 时才触发。
     */
    fun perform(view: View) {
        if (!isEnabled) return
        view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
    }
}
