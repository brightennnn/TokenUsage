package io.github.brightennnn.mmtokenmonitor.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import io.github.brightennnn.mmtokenmonitor.domain.model.AppFontSizes
import io.github.brightennnn.mmtokenmonitor.domain.model.WidgetFontSizes
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton

private val Context.fontSizeDataStore: DataStore<Preferences> by preferencesDataStore(name = "font_size_prefs")

@EntryPoint
@InstallIn(SingletonComponent::class)
interface FontSizeRepositoryEntryPoint {
    fun fontSizeRepository(): FontSizeRepository
}

@Singleton
class FontSizeRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        // App
        val APP_MODEL_TITLE = intPreferencesKey("app_model_title")
        val APP_INTERVAL_LABEL = intPreferencesKey("app_interval_label")
        val APP_INTERVAL_NUMBER = intPreferencesKey("app_interval_number")
        val APP_INTERVAL_PERCENT = intPreferencesKey("app_interval_percent")
        val APP_WEEKLY_LABEL = intPreferencesKey("app_weekly_label")
        val APP_WEEKLY_NUMBER = intPreferencesKey("app_weekly_number")
        val APP_WEEKLY_PERCENT = intPreferencesKey("app_weekly_percent")
        // Widget
        val WIDGET_TITLE = intPreferencesKey("widget_title")
        val WIDGET_NUMBER = intPreferencesKey("widget_number")
        val WIDGET_PROGRESS_BAR = intPreferencesKey("widget_progress_bar")
        val WIDGET_PERCENT = intPreferencesKey("widget_percent")
        val WIDGET_UPDATE_TIME = intPreferencesKey("widget_update_time")
        // Theme
        val MONET_MODE = stringPreferencesKey("monet_mode")
    }

    val appFontSizes: Flow<AppFontSizes> = context.fontSizeDataStore.data.map { prefs ->
        AppFontSizes(
            modelTitle = prefs[Keys.APP_MODEL_TITLE] ?: 16,
            intervalLabel = prefs[Keys.APP_INTERVAL_LABEL] ?: 12,
            intervalNumber = prefs[Keys.APP_INTERVAL_NUMBER] ?: 14,
            intervalPercent = prefs[Keys.APP_INTERVAL_PERCENT] ?: 12,
            weeklyLabel = prefs[Keys.APP_WEEKLY_LABEL] ?: 12,
            weeklyNumber = prefs[Keys.APP_WEEKLY_NUMBER] ?: 14,
            weeklyPercent = prefs[Keys.APP_WEEKLY_PERCENT] ?: 12
        )
    }

    val widgetFontSizes: Flow<WidgetFontSizes> = context.fontSizeDataStore.data.map { prefs ->
        WidgetFontSizes(
            title = prefs[Keys.WIDGET_TITLE] ?: 14,
            number = prefs[Keys.WIDGET_NUMBER] ?: 12,
            progressBar = prefs[Keys.WIDGET_PROGRESS_BAR] ?: 10,
            percent = prefs[Keys.WIDGET_PERCENT] ?: 10,
            updateTime = prefs[Keys.WIDGET_UPDATE_TIME] ?: 8
        )
    }

    val monetMode: Flow<String> = context.fontSizeDataStore.data.map { prefs ->
        prefs[Keys.MONET_MODE] ?: "follow_system"
    }

    suspend fun saveMonetMode(mode: String) {
        context.fontSizeDataStore.edit { prefs ->
            prefs[Keys.MONET_MODE] = mode
        }
    }

    suspend fun saveAppFontSizes(sizes: AppFontSizes) {
        context.fontSizeDataStore.edit { prefs ->
            prefs[Keys.APP_MODEL_TITLE] = sizes.modelTitle
            prefs[Keys.APP_INTERVAL_LABEL] = sizes.intervalLabel
            prefs[Keys.APP_INTERVAL_NUMBER] = sizes.intervalNumber
            prefs[Keys.APP_INTERVAL_PERCENT] = sizes.intervalPercent
            prefs[Keys.APP_WEEKLY_LABEL] = sizes.weeklyLabel
            prefs[Keys.APP_WEEKLY_NUMBER] = sizes.weeklyNumber
            prefs[Keys.APP_WEEKLY_PERCENT] = sizes.weeklyPercent
        }
    }

    suspend fun saveWidgetFontSizes(sizes: WidgetFontSizes) {
        context.fontSizeDataStore.edit { prefs ->
            prefs[Keys.WIDGET_TITLE] = sizes.title
            prefs[Keys.WIDGET_NUMBER] = sizes.number
            prefs[Keys.WIDGET_PROGRESS_BAR] = sizes.progressBar
            prefs[Keys.WIDGET_PERCENT] = sizes.percent
            prefs[Keys.WIDGET_UPDATE_TIME] = sizes.updateTime
        }
    }
}
