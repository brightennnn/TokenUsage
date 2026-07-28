package io.github.brightennnn.mmtokenmonitor.widget

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 点击 widget 刷新信息（通过 actionSendBroadcast 触发 WidgetAlarmReceiver）
 */
object WidgetRefreshAction {

    /**
     * 刷新所有 widget（供 BroadcastReceiver 调用）
     */
    suspend fun refreshAll(context: Context) {
        withContext(Dispatchers.IO) {
            TokenWidgetUpdater.updateAllWidgets(context)
        }
        WidgetRefreshScheduler.rescheduleNext(context)
    }
}
