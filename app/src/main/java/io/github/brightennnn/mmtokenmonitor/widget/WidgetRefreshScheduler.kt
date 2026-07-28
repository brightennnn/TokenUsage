package io.github.brightennnn.mmtokenmonitor.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build

/**
 * 用 AlarmManager 定时刷新 widget（比 updatePeriodMillis 更可靠）
 * 支持多个 widget 共用，按引用计数管理
 */
object WidgetRefreshScheduler {

    private const val REQUEST_CODE = 1001
    // 刷新间隔：30分钟
    private const val INTERVAL_MS = 30 * 60 * 1000L

    const val ACTION_REFRESH = "io.github.brightennnn.mmtokenmonitor.ACTION_WIDGET_REFRESH"

    // 引用计数：记录当前有多少个 widget 正在使用
    private var refCount = 0

    fun schedule(context: Context) {
        refCount++
        if (refCount > 1) return // 已经有 widget 在用，不需要重复注册

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, WidgetAlarmReceiver::class.java).apply {
            action = ACTION_REFRESH
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis() + INTERVAL_MS,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis() + INTERVAL_MS,
                pendingIntent
            )
        }
    }

    fun cancel(context: Context) {
        refCount--
        if (refCount > 0) return // 还有其他 widget 在用，不取消

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, WidgetAlarmReceiver::class.java).apply {
            action = ACTION_REFRESH
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    fun rescheduleNext(context: Context) {
        if (refCount <= 0) return
        schedule(context)
    }
}
