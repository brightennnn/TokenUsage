package io.github.brightennnn.tokenusage.widget

import android.content.Context
import android.util.Log
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.updateAll
import io.github.brightennnn.tokenusage.data.repository.FontSizeRepository
import io.github.brightennnn.tokenusage.data.repository.ThemeRepository
import io.github.brightennnn.tokenusage.data.repository.ThemeRepositoryEntryPoint
import io.github.brightennnn.tokenusage.domain.model.ModelQuota
import dagger.hilt.EntryPoints
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File

object TokenWidgetUpdater {

    private const val WIDGET_DATA_FILE = "widget_data.json"

    /**
     * 完整更新 Widget（拉取新数据 + 写入字号）
     */
    suspend fun updateWidget(context: Context, quotas: List<ModelQuota>, fontSizeRepository: FontSizeRepository) {
        android.util.Log.d("TokenWidget", "updateWidget called with ${quotas.size} quotas")
        // 优先使用 general 模型（Token Plan 主模型）
        val generalModel = quotas.filter {
            !it.modelName.contains("coding-plan", ignoreCase = true)
                && it.modelName == "general"
        }.firstOrNull {
            it.intervalRemainingPercent > 0 || it.weeklyRemainingPercent > 0
        } ?: quotas.filter {
            !it.modelName.contains("coding-plan", ignoreCase = true)
                && it.modelName == "general"
        }.firstOrNull()
        if (generalModel == null) {
            android.util.Log.d("TokenWidget", "No general model found")
            return
        }
        android.util.Log.d("TokenWidget", "Using model: ${generalModel.modelName}, interval: ${generalModel.intervalRemainingPercent}%, weekly: ${generalModel.weeklyRemainingPercent}%")

        // 读取 Widget 字号
        val widgetFontSizes = fontSizeRepository.widgetFontSizes.first()

        // 读取主题设置
        val themeSettings = try {
            val entryPoint = EntryPoints.get(context, ThemeRepositoryEntryPoint::class.java)
            entryPoint.themeRepository().themeSettings.first()
        } catch (e: Exception) {
            null
        }

        val beijingZone = java.time.ZoneId.of("Asia/Shanghai")
        val timeFormatter = java.time.format.DateTimeFormatter.ofPattern("HH:mm")
        val dateFormatter = java.time.format.DateTimeFormatter.ofPattern("MM-dd")
        val nowStr = java.time.Instant.now().atZone(beijingZone).format(timeFormatter)
        val intervalPeriodStr = if (generalModel.intervalStartMs > 0 && generalModel.intervalEndMs > 0) {
            val start = java.time.Instant.ofEpochMilli(generalModel.intervalStartMs).atZone(beijingZone).format(timeFormatter)
            val end = java.time.Instant.ofEpochMilli(generalModel.intervalEndMs).atZone(beijingZone).format(timeFormatter)
            "$start-$end"
        } else "—"
        val weeklyPeriodStr = if (generalModel.weeklyStartMs > 0 && generalModel.weeklyEndMs > 0) {
            val start = java.time.Instant.ofEpochMilli(generalModel.weeklyStartMs).atZone(beijingZone).format(dateFormatter)
            val end = java.time.Instant.ofEpochMilli(generalModel.weeklyEndMs).atZone(beijingZone).format(dateFormatter)
            "$start ~ $end"
        } else "—"

        val json = JSONObject().apply {
            put("modelName", generalModel.modelName)
            put("intervalRemainingPercent", generalModel.intervalRemainingPercent)
            put("intervalStartMs", generalModel.intervalStartMs)
            put("intervalEndMs", generalModel.intervalEndMs)
            put("intervalRemainsMs", generalModel.intervalRemainsMs)
            put("weeklyRemainingPercent", generalModel.weeklyRemainingPercent)
            put("weeklyStartMs", generalModel.weeklyStartMs)
            put("weeklyEndMs", generalModel.weeklyEndMs)
            put("weeklyRemainsMs", generalModel.weeklyRemainsMs)
            put("lastUpdate", nowStr)
            put("intervalPeriod", intervalPeriodStr)
            put("weeklyPeriod", weeklyPeriodStr)
            // Widget 字号
            put("widgetTitleSize", widgetFontSizes.title)
            put("widgetNumberSize", widgetFontSizes.number)
            put("widgetProgressBarSize", widgetFontSizes.progressBar)
            put("widgetPercentSize", widgetFontSizes.percent)
            put("widgetUpdateTimeSize", widgetFontSizes.updateTime)
            // 主题设置
            put("dynamicColor", themeSettings?.dynamicColor ?: true)
            put("darkMode", themeSettings?.darkMode?.ordinal ?: 0)
            put("pureBlack", themeSettings?.pureBlack ?: false)
        }
        val file = File(context.filesDir, WIDGET_DATA_FILE)
        withContext(Dispatchers.IO) {
            file.writeText(json.toString())
        }
        android.util.Log.d("TokenWidget", "Widget data written to file: ${file.absolutePath}")

        bumpWidgetState(context)
    }

    /**
     * 仅刷新 Widget 状态（字号或主题变更后调用，不重新拉取数据）
     */
    suspend fun refreshWidgets(context: Context, fontSizeRepository: FontSizeRepository) {
        val widgetFontSizes = fontSizeRepository.widgetFontSizes.first()

        // 读取主题设置
        val themeSettings = try {
            val entryPoint = EntryPoints.get(context, ThemeRepositoryEntryPoint::class.java)
            entryPoint.themeRepository().themeSettings.first()
        } catch (e: Exception) {
            null
        }

        val file = File(context.filesDir, WIDGET_DATA_FILE)
        if (file.exists()) {
            try {
                val json = JSONObject(file.readText())
                json.put("widgetTitleSize", widgetFontSizes.title)
                json.put("widgetNumberSize", widgetFontSizes.number)
                json.put("widgetProgressBarSize", widgetFontSizes.progressBar)
                json.put("widgetPercentSize", widgetFontSizes.percent)
                json.put("widgetUpdateTimeSize", widgetFontSizes.updateTime)
                json.put("dynamicColor", themeSettings?.dynamicColor ?: true)
                json.put("darkMode", themeSettings?.darkMode?.ordinal ?: 0)
                json.put("pureBlack", themeSettings?.pureBlack ?: false)
                withContext(Dispatchers.IO) {
                    file.writeText(json.toString())
                }
            } catch (e: Exception) {
                android.util.Log.e("TokenWidget", "Failed to refresh widget", e)
            }
        }

        bumpWidgetState(context)
    }

    /**
     * 仅刷新 Widget 显示（从 widget_data.json 读取现有数据，不重新拉取）
     * 打开 app 刷新数据后调用，同步 widget 显示
     */
    suspend fun refreshWidgetDisplay(context: Context) {
        bumpWidgetState(context)
    }

    /**
     * 刷新所有 widget（点击触发，不重新拉取数据，只更新显示）
     */
    suspend fun updateAllWidgets(context: Context) {
        bumpWidgetState(context)
    }

    private suspend fun bumpWidgetState(context: Context) {
        // Update Glance widgets
        updateGlanceWidgets(context)
    }

    private suspend fun updateGlanceWidgets(context: Context) {
        val manager = GlanceAppWidgetManager(context)

        // Update GlanceIntervalWidget
        try {
            val intervalGlanceIds = manager.getGlanceIds(GlanceIntervalWidget::class.java)
            Log.d("TokenWidget", "Found ${intervalGlanceIds.size} GlanceIntervalWidget instances")
            intervalGlanceIds.forEach { glanceId ->
                GlanceIntervalWidget().update(context, glanceId)
            }
        } catch (e: Exception) {
            Log.e("TokenWidget", "Failed to update GlanceIntervalWidget", e)
        }

        // Update GlanceWeeklyWidget
        try {
            val weeklyGlanceIds = manager.getGlanceIds(GlanceWeeklyWidget::class.java)
            Log.d("TokenWidget", "Found ${weeklyGlanceIds.size} GlanceWeeklyWidget instances")
            weeklyGlanceIds.forEach { glanceId ->
                GlanceWeeklyWidget().update(context, glanceId)
            }
        } catch (e: Exception) {
            Log.e("TokenWidget", "Failed to update GlanceWeeklyWidget", e)
        }
    }
}
