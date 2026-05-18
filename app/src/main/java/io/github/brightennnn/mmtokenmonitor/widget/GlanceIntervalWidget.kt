package io.github.brightennnn.mmtokenmonitor.widget

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
import io.github.brightennnn.mmtokenmonitor.MainActivity

/** 5小时剩余 Widget — 跟随 app 主题配色 */
class GlanceIntervalWidget : GlanceAppWidget() {

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
                // Main content
                Column(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalAlignment = Alignment.Start
                ) {
                    // Title
                    Text(
                        text = "5小时剩余",
                        style = TextStyle(
                            color = ColorProvider(colors.onSurface, colors.onSurface),
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        )
                    )
                    Spacer(GlanceModifier.height(8.dp))
                    // Remaining
                    Text(
                        text = formatNumber((data.intervalTotal - data.intervalUsed).coerceAtLeast(0)),
                        style = TextStyle(
                            color = ColorProvider(colors.primary, colors.primary),
                            fontWeight = FontWeight.Bold,
                            fontSize = 28.sp
                        )
                    )
                    Spacer(GlanceModifier.height(5.dp))
                    Text(
                        text = data.intervalPeriod,
                        style = TextStyle(
                            color = ColorProvider(colors.onSurfaceVariant, colors.onSurfaceVariant),
                            fontSize = 10.sp
                        )
                    )
                    Spacer(GlanceModifier.height(12.dp))
                    // Progress — stepped bar (10 segments)
                    val pct = if (data.intervalTotal > 0) {
                        ((data.intervalTotal - data.intervalUsed).toFloat() / data.intervalTotal).coerceIn(0f, 1f)
                    } else 0f
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
                        text = "已用 ${data.intervalUsed} / ${data.intervalTotal}",
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

                // Refresh button in top-right corner
                Box(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .padding(8.dp),
                    contentAlignment = Alignment.TopEnd
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

    private fun readWidgetData(context: Context): WidgetData {
        val file = File(context.filesDir, WIDGET_DATA_FILE)
        if (!file.exists()) return WidgetData()
        return try {
            val json = JSONObject(file.readText())
            WidgetData(
                modelName = json.optString("modelName", "—"),
                intervalUsed = json.optInt("intervalUsed", 0),
                intervalTotal = json.optInt("intervalTotal", 0),
                intervalStartMs = json.optLong("intervalStartMs", 0L),
                intervalEndMs = json.optLong("intervalEndMs", 0L),
                weeklyUsed = json.optInt("weeklyUsed", 0),
                weeklyTotal = json.optInt("weeklyTotal", 0),
                lastUpdate = json.optString("lastUpdate", "—"),
                intervalPeriod = json.optString("intervalPeriod", "—"),
                weeklyPeriod = json.optString("weeklyPeriod", "—")
            )
        } catch (e: Exception) {
            WidgetData()
        }
    }

    private fun formatNumber(n: Int): String = when {
        n >= 1_000_000 -> String.format("%.1fM", n / 1_000_000.0)
        n >= 1_000 -> String.format("%.1fK", n / 1_000.0)
        else -> n.toString()
    }

    companion object {
        const val WIDGET_DATA_FILE = "widget_data.json"
    }
}
