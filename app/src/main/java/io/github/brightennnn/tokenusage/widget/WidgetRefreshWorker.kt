package io.github.brightennnn.tokenusage.widget

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import io.github.brightennnn.tokenusage.data.repository.FontSizeRepository
import io.github.brightennnn.tokenusage.domain.repository.GlmRepository
import io.github.brightennnn.tokenusage.domain.repository.TokenRepository
import kotlinx.coroutines.flow.firstOrNull

/**
 * 后台刷新 Widget — 真正调 API 拉数据 → 写 JSON → 触发 Glance 重绘。
 * 由 WidgetWorkerFactory（Hilt 注入）创建。
 */
class WidgetRefreshWorker(
    appContext: Context,
    params: WorkerParameters,
    private val tokenRepository: TokenRepository,
    private val glmRepository: GlmRepository,
    private val fontSizeRepository: FontSizeRepository,
    private val glmWidgetUpdater: GlmWidgetUpdater,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        Log.d(TAG, "doWork start")
        // 标记本次"用户点击"已生效 — 启动 30s 节流窗口（防连点）
        WidgetRefreshScheduler.markClickConsumed()
        return try {
            // 1. MiniMax 拉取
            val key = tokenRepository.activeApiKey.firstOrNull()
            val groupId = tokenRepository.activeGroupId.firstOrNull().orEmpty()
            if (!key.isNullOrBlank()) {
                val result = tokenRepository.fetchQuotas(key, groupId)
                result.onSuccess { quotas ->
                    TokenWidgetUpdater.updateWidget(applicationContext, quotas, fontSizeRepository)
                }.onFailure { e ->
                    Log.w(TAG, "MiniMax fetch failed: ${e.message}")
                }
            } else {
                Log.d(TAG, "No MiniMax key, skip")
            }

            // 2. GLM 拉取
            val glmToken = glmRepository.glmToken.firstOrNull()
            if (!glmToken.isNullOrBlank()) {
                val result = glmRepository.fetchQuotas(glmToken)
                result.onSuccess { quotas ->
                    glmWidgetUpdater.updateGlmWidget(quotas)
                }.onFailure { e ->
                    Log.w(TAG, "GLM fetch failed: ${e.message}")
                }
            } else {
                Log.d(TAG, "No GLM token, skip")
            }

            // 3. 排下一次定时刷新（30 分钟后）
            WidgetRefreshScheduler.scheduleNext(applicationContext)

            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "doWork failed", e)
            Result.retry()
        }
    }

    companion object {
        private const val TAG = "WidgetRefreshWorker"
        const val UNIQUE_NAME = "widget_refresh"
    }
}