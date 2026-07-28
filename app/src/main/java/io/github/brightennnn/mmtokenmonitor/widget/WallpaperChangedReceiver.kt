package io.github.brightennnn.mmtokenmonitor.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.runBlocking

/**
 * 监听壁纸变化广播，刷新所有 widget 配色
 */
class WallpaperChangedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_WALLPAPER_CHANGED) {
            // 刷新 Glance widgets
            runBlocking {
                GlanceIntervalWidget().updateAll(context)
                GlanceWeeklyWidget().updateAll(context)
            }
        }
    }
}
