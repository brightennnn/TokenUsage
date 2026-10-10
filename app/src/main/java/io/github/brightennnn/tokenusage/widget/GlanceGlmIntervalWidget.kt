package io.github.brightennnn.tokenusage.widget

import android.content.Context
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.LinearProgressIndicator
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.action.actionSendBroadcast
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.FontStyle
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import org.json.JSONObject
import java.io.File
import io.github.brightennnn.tokenusage.MainActivity

/** GLM 5小时窗口 Widget */
class GlanceGlmIntervalWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val data = readWidgetData(context)
        val colors = WidgetColors.fromContext(context)

        provideContent {
            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(ColorProvider(colors.background, colors.background))
                    .cornerRadius(16.dp)
                    .clickable(actionStartActivity<MainActivity>()),
            ) {
                Column(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalAlignment = Alignment.Start
                ) {
                    // 紫色标题 = GLM 标识
                    Text(
                        text = "GLM 5小时剩余",
                        style = TextStyle(
                            color = ColorProvider(colors.primary, colors.primary),
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp
                        )
                    )
                    Spacer(GlanceModifier.height(8.dp))
                    Text(
                        text = "${data.intervalRemainingPercent.coerceIn(0, 100)}%",
                        style = TextStyle(
                            color = ColorProvider(colors.primary, colors.primary),
                            fontWeight = FontWeight.Bold,
                            fontSize = 28.sp
                        )
                    )
                    Spacer(GlanceModifier.height(12.dp))
                    Text(
                        text = data.intervalPeriod,
                        style = TextStyle(
                            color = ColorProvider(colors.onSurfaceVariant, colors.onSurfaceVariant),
                            fontSize = 10.sp
                        )
                    )
                    Spacer(GlanceModifier.height(20.dp))
                    val pct = (data.intervalRemainingPercent.coerceIn(0, 100)) / 100f
                    LinearProgressIndicator(
                        progress = pct,
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .cornerRadius(4.dp),
                        color = ColorProvider(colors.primary, colors.primary),
                        backgroundColor = ColorProvider(colors.surfaceVariant, colors.surfaceVariant)
                    )
                    Spacer(GlanceModifier.height(6.dp))
                    Text(
                        text = "剩余 ${data.intervalRemainingPercent.coerceIn(0, 100)}%",
                        style = TextStyle(
                            color = ColorProvider(colors.secondary, colors.secondary),
                            fontSize = 11.sp
                        )
                    )
                    Text(
                        text = data.lastUpdate,
                        style = TextStyle(
                            color = ColorProvider(colors.outline, colors.outline),
                            fontSize = 9.sp,
                            fontStyle = FontStyle.Italic
                        )
                    )
                }

                // 刷新按钮
                Box(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .padding(8.dp),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    Box(
                        modifier = GlanceModifier
                            .size(32.dp)
                            .cornerRadius(8.dp)
                            .background(ColorProvider(colors.background, colors.background))
                            .clickable(actionSendBroadcast<WidgetAlarmReceiver>()),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "↻",
                            style = TextStyle(
                                color = ColorProvider(colors.onSurfaceVariant, colors.onSurfaceVariant),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }
    }

    private fun readWidgetData(context: Context): GlmWidgetData {
        val file = File(context.filesDir, WIDGET_DATA_FILE)
        if (!file.exists()) return GlmWidgetData()
        return try {
            val json = JSONObject(file.readText())
            GlmWidgetData(
                intervalRemainingPercent = json.optInt("glmIntervalRemainingPercent", 0),
                intervalPeriod = json.optString("glmIntervalPeriod", "—"),
                intervalRemainsMs = json.optLong("glmIntervalRemainsMs", 0L),
                weeklyRemainingPercent = json.optInt("glmWeeklyRemainingPercent", 0),
                weeklyPeriod = json.optString("glmWeeklyPeriod", "—"),
                weeklyRemainsMs = json.optLong("glmWeeklyRemainsMs", 0L),
                lastUpdate = json.optString("glmLastUpdate", "—")
            )
        } catch (e: Exception) {
            GlmWidgetData()
        }
    }

    companion object {
        const val WIDGET_DATA_FILE = "glm_widget_data.json"
    }
}
