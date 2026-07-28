package io.github.brightennnn.mmtokenmonitor.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.runBlocking

/**
 * 接收 widget 点击刷新信号（actionSendBroadcast）和定时 alarm 信号，刷新所有 widget
 */
class WidgetAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // 点击 widget 或 alarm 定时触发，都执行相同的刷新逻辑
        runBlocking {
            WidgetRefreshAction.refreshAll(context)
        }
    }
}
