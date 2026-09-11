package io.github.brightennnn.mmtokenmonitor.widget

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong

/**
 * Widget 后台刷新调度器 — 用 WorkManager 取代 AlarmManager。
 *
 * 策略要点：
 * - 用户点击按钮 → refreshNow() → REPLACE 强制新 Worker 入队（避免被 scheduleNext 占坑挡住）
 * - 30 秒节流窗口：用户连续点击只触发一次 API，防止误触 / 狂点刷流量
 * - 节流窗口起点 = Worker 实际跑起来的时间（doWork 入口），不是 enqueue 时间
 *   这样首次点击永远能立刻响应，连续点击才会被吞
 * - 定时刷新 scheduleNext() 不受节流影响（30 分钟一次，本来就不会太密）
 */
object WidgetRefreshScheduler {

    private const val TAG = "WidgetRefreshScheduler"
    private const val INTERVAL_MINUTES = 30L
    private const val INITIAL_DELAY_SECONDS = 0L

    /** 用户点击节流窗口：30 秒 */
    private const val CLICK_COOLDOWN_MS = 30_000L

    /**
     * Worker doWork 入口处刷新这个时间戳。
     * refreshNow() 在调用 enqueue 前检查当前时间 - lastClickStartMs 是否 < 30s，
     * 若是则直接 return（吞掉重复点击）。
     */
    @Volatile
    private var lastClickStartMs: Long = 0L

    /**
     * 用户触发刷新（点击 widget 按钮 / 首次安装 / etc.）
     * 用 REPLACE 策略 — 强制覆盖，旧的 cancel，新的立刻排上。
     * 30s 节流：Worker doWork 入口会刷新 lastClickStartMs，连续点击只生效一次。
     */
    fun refreshNow(context: Context) {
        // 节流判断：上一次 Worker 实际启动时间距今 < 30s → 直接吞掉
        val now = System.currentTimeMillis()
        val elapsed = now - lastClickStartMs
        if (lastClickStartMs > 0L && elapsed < CLICK_COOLDOWN_MS) {
            Log.d(TAG, "refreshNow skipped (cooldown ${CLICK_COOLDOWN_MS - elapsed}ms left)")
            return
        }

        val request = OneTimeWorkRequestBuilder<WidgetRefreshWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .setInitialDelay(INITIAL_DELAY_SECONDS, TimeUnit.SECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            WidgetRefreshWorker.UNIQUE_NAME,
            ExistingWorkPolicy.REPLACE,
            request
        )
        Log.d(TAG, "refreshNow enqueued (REPLACE)")
    }

    /**
     * Worker doWork 入口调用 — 标记本次点击"已生效"，启动 30s 节流窗口。
     */
    fun markClickConsumed() {
        lastClickStartMs = System.currentTimeMillis()
        Log.d(TAG, "markClickConsumed at $lastClickStartMs")
    }

    /**
     * Worker 完成后调用 — 排 30 分钟后的下一次定时。
     * REPLACE 策略：旧的 cancel，新 Worker 排队。
     * **不**走用户点击节流（30 分钟本身够久）。
     */
    fun scheduleNext(context: Context) {
        val request = OneTimeWorkRequestBuilder<WidgetRefreshWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .setInitialDelay(INTERVAL_MINUTES, TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            WidgetRefreshWorker.UNIQUE_NAME,
            ExistingWorkPolicy.REPLACE,
            request
        )
        Log.d(TAG, "scheduleNext enqueued (${INTERVAL_MINUTES}m)")
    }

    /** 取消所有待处理的刷新（清 DataStore / 卸载场景） */
    fun cancelAll(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WidgetRefreshWorker.UNIQUE_NAME)
        lastClickStartMs = 0L
        Log.d(TAG, "cancelAll")
    }
}