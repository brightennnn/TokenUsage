package io.github.brightennnn.mmtokenmonitor.widget

import android.content.Context

/**
 * 点击 widget 刷新信息入口（actionSendBroadcast 触发 WidgetAlarmReceiver 后，
 * Receiver 调这里统一入队 Worker）。
 */
object WidgetRefreshAction {

    suspend fun refreshAll(context: Context) {
        WidgetRefreshScheduler.refreshNow(context)
    }
}