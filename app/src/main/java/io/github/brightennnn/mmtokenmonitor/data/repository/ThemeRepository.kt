package io.github.brightennnn.mmtokenmonitor.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import io.github.brightennnn.mmtokenmonitor.domain.model.DarkMode
import io.github.brightennnn.mmtokenmonitor.domain.model.ThemeSettings
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.EntryPoint
import dagger.hilt.EntryPoints
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.themeDataStore: DataStore<Preferences> by preferencesDataStore(name = "theme_prefs")

@EntryPoint
@InstallIn(SingletonComponent::class)
interface ThemeRepositoryEntryPoint {
    fun themeRepository(): ThemeRepository
}

@Singleton
class ThemeRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        val DARK_MODE = intPreferencesKey("dark_mode")
        val PURE_BLACK = booleanPreferencesKey("pure_black")
        val HAPTIC_FEEDBACK = booleanPreferencesKey("haptic_feedback")
    }

    val themeSettings: Flow<ThemeSettings> = context.themeDataStore.data.map { prefs ->
        ThemeSettings(
            dynamicColor = prefs[Keys.DYNAMIC_COLOR] ?: true,
            darkMode = DarkMode.fromOrdinal(prefs[Keys.DARK_MODE] ?: 0),
            pureBlack = prefs[Keys.PURE_BLACK] ?: false,
            hapticFeedback = prefs[Keys.HAPTIC_FEEDBACK] ?: true
        )
    }

    suspend fun saveDynamicColor(enabled: Boolean) {
        context.themeDataStore.edit { prefs ->
            prefs[Keys.DYNAMIC_COLOR] = enabled
        }
    }

    suspend fun saveDarkMode(mode: DarkMode) {
        context.themeDataStore.edit { prefs ->
            prefs[Keys.DARK_MODE] = mode.ordinal
        }
    }

    suspend fun savePureBlack(enabled: Boolean) {
        context.themeDataStore.edit { prefs ->
            prefs[Keys.PURE_BLACK] = enabled
        }
    }

    suspend fun saveHapticFeedback(enabled: Boolean) {
        context.themeDataStore.edit { prefs ->
            prefs[Keys.HAPTIC_FEEDBACK] = enabled
        }
    }
}
