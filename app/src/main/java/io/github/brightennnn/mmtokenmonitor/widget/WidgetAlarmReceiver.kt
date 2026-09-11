package io.github.brightennnn.mmtokenmonitor.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * 接收 widget 点击刷新信号和定时 alarm 信号，统一入队 WorkManager Worker。
 *
 * 注意：BroadcastReceiver.onReceive 默认 10s 超时，跑网络 IO 容易 ANR。
 * 这里用两路策略：
 *   1. 简单入队（enqueue）— 走 WorkManager 系统线程，Receiver 立即返回
 *   2. WorkManager 内部在 Worker 里执行真正的网络请求
 */
class WidgetAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "onReceive action=${intent.action}")
        // 直接入队，Worker 在 WorkManager 自管线程里跑
        WidgetRefreshScheduler.refreshNow(context.applicationContext)
    }

    companion object {
        private const val TAG = "WidgetAlarmReceiver"
    }
}