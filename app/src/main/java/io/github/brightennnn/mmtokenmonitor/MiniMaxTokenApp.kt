package io.github.brightennnn.mmtokenmonitor

import android.app.Application
import android.util.Log
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import io.github.brightennnn.mmtokenmonitor.widget.WidgetWorkerFactory
import javax.inject.Inject

@HiltAndroidApp
class MiniMaxTokenApp : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: WidgetWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(Log.INFO)
            .build()
}