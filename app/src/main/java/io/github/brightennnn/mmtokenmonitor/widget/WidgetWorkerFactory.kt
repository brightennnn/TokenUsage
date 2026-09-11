package io.github.brightennnn.mmtokenmonitor.widget

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import io.github.brightennnn.mmtokenmonitor.data.repository.FontSizeRepository
import io.github.brightennnn.mmtokenmonitor.domain.repository.GlmRepository
import io.github.brightennnn.mmtokenmonitor.domain.repository.TokenRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 自定义 WorkerFactory — Hilt 直接持有 Repository 引用，构造 WidgetRefreshWorker 时注入。
 * 由 MiniMaxTokenApp (Configuration.Provider) 注入 WorkManager。
 */
@Singleton
class WidgetWorkerFactory @Inject constructor(
    private val tokenRepository: TokenRepository,
    private val glmRepository: GlmRepository,
    private val fontSizeRepository: FontSizeRepository,
    private val glmWidgetUpdater: GlmWidgetUpdater,
) : WorkerFactory() {

    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {
        return when (workerClassName) {
            WidgetRefreshWorker::class.java.name ->
                WidgetRefreshWorker(
                    appContext = appContext,
                    params = workerParameters,
                    tokenRepository = tokenRepository,
                    glmRepository = glmRepository,
                    fontSizeRepository = fontSizeRepository,
                    glmWidgetUpdater = glmWidgetUpdater,
                )
            else -> null
        }
    }
}