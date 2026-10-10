package io.github.brightennnn.tokenusage.widget

import android.content.Context
import android.util.Log
import androidx.glance.appwidget.GlanceAppWidgetManager
import io.github.brightennnn.tokenusage.domain.model.GlmQuota
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GlmWidgetUpdater @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val beijingZone = ZoneId.of("Asia/Shanghai")
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("MM-dd HH:mm")

    private val GLM_WIDGET_DATA_FILE = "glm_widget_data.json"

    suspend fun updateGlmWidget(quotas: List<GlmQuota>) {
        Log.d("GlmWidget", "updateGlmWidget called with ${quotas.size} quotas")

        val intervalQuota = quotas.find { it.rawUnit == 3 }
        val weeklyQuota = quotas.find { it.rawUnit == 6 }

        val nowStr = Instant.now().atZone(beijingZone).format(timeFormatter)

        val intervalPeriod = if (
            intervalQuota?.windowStartMs != null &&
            intervalQuota.windowEndMs != null &&
            intervalQuota.windowEndMs > intervalQuota.windowStartMs
        ) {
            val startStr = Instant.ofEpochMilli(intervalQuota.windowStartMs).atZone(beijingZone).format(timeFormatter)
            val endStr = Instant.ofEpochMilli(intervalQuota.windowEndMs).atZone(beijingZone).format(timeFormatter)
            "$startStr-$endStr"
        } else "—"

        val weeklyPeriod = if (weeklyQuota != null && weeklyQuota.nextResetMs > 0) {
            val resetInstant = Instant.ofEpochMilli(weeklyQuota.nextResetMs).atZone(beijingZone)
            val weekStart = resetInstant.minusDays(7)
            "${weekStart.format(dateTimeFormatter)} ~ ${resetInstant.format(dateTimeFormatter)}"
        } else "—"

        val json = JSONObject().apply {
            put("glmIntervalRemainingPercent", intervalQuota?.remainingPercent ?: 0)
            put("glmIntervalPeriod", intervalPeriod)
            put("glmIntervalRemainsMs", intervalQuota?.remainsMs ?: 0L)
            put("glmWeeklyRemainingPercent", weeklyQuota?.remainingPercent ?: 0)
            put("glmWeeklyPeriod", weeklyPeriod)
            put("glmWeeklyRemainsMs", weeklyQuota?.remainsMs ?: 0L)
            put("glmLastUpdate", nowStr)
        }

        val file = File(context.filesDir, GLM_WIDGET_DATA_FILE)
        withContext(Dispatchers.IO) {
            file.writeText(json.toString())
        }
        Log.d("GlmWidget", "GLM widget data written: $file")

        bumpGlanceWidgets()
    }

    private suspend fun bumpGlanceWidgets() {
        val manager = GlanceAppWidgetManager(context)

        try {
            val intervalIds = manager.getGlanceIds(GlanceGlmIntervalWidget::class.java)
            Log.d("GlmWidget", "Found ${intervalIds.size} GLM interval widget instances")
            intervalIds.forEach { glanceId ->
                GlanceGlmIntervalWidget().update(context, glanceId)
            }
        } catch (e: Exception) {
            Log.e("GlmWidget", "Failed to update GLM interval widget", e)
        }

        try {
            val weeklyIds = manager.getGlanceIds(GlanceGlmWeeklyWidget::class.java)
            Log.d("GlmWidget", "Found ${weeklyIds.size} GLM weekly widget instances")
            weeklyIds.forEach { glanceId ->
                GlanceGlmWeeklyWidget().update(context, glanceId)
            }
        } catch (e: Exception) {
            Log.e("GlmWidget", "Failed to update GLM weekly widget", e)
        }

    }
}
