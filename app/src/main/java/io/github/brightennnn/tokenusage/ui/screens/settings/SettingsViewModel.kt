package io.github.brightennnn.tokenusage.ui.screens.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.brightennnn.tokenusage.data.repository.FontSizeRepository
import io.github.brightennnn.tokenusage.data.repository.ThemeRepository
import io.github.brightennnn.tokenusage.domain.model.ApiKeyProfile
import io.github.brightennnn.tokenusage.domain.model.DarkMode
import io.github.brightennnn.tokenusage.domain.repository.TokenRepository
import io.github.brightennnn.tokenusage.widget.TokenWidgetUpdater
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val apiKey: String = "",
    val hasKey: Boolean = false,
    val isEditing: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: TokenRepository,
    private val fontSizeRepository: FontSizeRepository,
    private val themeRepository: ThemeRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    // 多 Key 管理
    private val _apiKeyProfiles = MutableStateFlow<List<ApiKeyProfile>>(emptyList())
    val apiKeyProfiles: StateFlow<List<ApiKeyProfile>> = _apiKeyProfiles.asStateFlow()

    private val _activeApiKeyId = MutableStateFlow<String?>(null)
    val activeApiKeyId: StateFlow<String?> = _activeApiKeyId.asStateFlow()

    init {
        viewModelScope.launch {
            repository.apiKeyProfiles.collect { profiles ->
                _apiKeyProfiles.value = profiles
            }
        }
        viewModelScope.launch {
            repository.activeApiKey.collect { key ->
                val active = _apiKeyProfiles.value.find { it.key == key }
                _activeApiKeyId.value = active?.id
            }
        }
    }

    init {
        loadSavedKey()
    }

    private fun loadSavedKey() {
        viewModelScope.launch {
            repository.activeApiKey.collect { key ->
                _uiState.update { it.copy(apiKey = key ?: "", hasKey = !key.isNullOrBlank(), isEditing = false) }
            }
        }
    }

    fun updateApiKey(key: String) {
        _uiState.update { it.copy(apiKey = key, error = null) }
    }

    fun startEditing() {
        _uiState.update { it.copy(isEditing = true, apiKey = it.apiKey) }
    }

    fun cancelEditing() {
        viewModelScope.launch {
            val key = repository.activeApiKey.first()
            _uiState.update { it.copy(isEditing = false, apiKey = key ?: "") }
        }
    }

    fun saveAndFetch() {
        val key = _uiState.value.apiKey.trim()
        if (key.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            repository.saveApiKey(key, "默认", "")
            _uiState.update { it.copy(hasKey = true, isEditing = false, isLoading = false) }
            repository.fetchQuotas(key, "")
                .onSuccess { quotas ->
                    TokenWidgetUpdater.updateWidget(context, quotas, fontSizeRepository)
                }
                .onFailure { _ -> }
        }
    }

    fun clearKey() {
        viewModelScope.launch {
            repository.clearAllApiKeys()
            _uiState.update { it.copy(apiKey = "", hasKey = false, isEditing = false) }
        }
    }

    // --- 多 Key 管理 ---

    fun addApiKey(name: String, key: String, groupId: String = "") {
        viewModelScope.launch {
            repository.saveApiKey(key.trim(), name.trim(), groupId.trim())
        }
    }

    fun activateApiKey(id: String) {
        viewModelScope.launch {
            repository.setActiveApiKey(id)
            val profiles = repository.apiKeyProfiles.first()
            val profile = profiles.find { it.id == id }
            if (profile != null) {
                repository.fetchQuotas(profile.key, profile.groupId)
                    .onSuccess { quotas ->
                        TokenWidgetUpdater.updateWidget(context, quotas, fontSizeRepository)
                    }
            }
        }
    }

    fun deleteApiKey(id: String) {
        viewModelScope.launch {
            val profiles = repository.apiKeyProfiles.first()
            repository.deleteApiKeyProfile(id)
            val remaining = profiles.filter { it.id != id }
            if (remaining.isNotEmpty()) {
                repository.setActiveApiKey(remaining.first().id)
            }
        }
    }

    fun updateApiKeyProfile(profile: ApiKeyProfile) {
        viewModelScope.launch {
            repository.updateApiKeyProfile(profile)
        }
    }

    // --- Theme Settings ---

    fun setDynamicColor(enabled: Boolean) {
        viewModelScope.launch {
            themeRepository.saveDynamicColor(enabled)
            TokenWidgetUpdater.refreshWidgets(context, fontSizeRepository)
        }
    }

    fun setDarkMode(mode: DarkMode) {
        viewModelScope.launch {
            themeRepository.saveDarkMode(mode)
            TokenWidgetUpdater.refreshWidgets(context, fontSizeRepository)
        }
    }

    fun setPureBlack(enabled: Boolean) {
        viewModelScope.launch {
            themeRepository.savePureBlack(enabled)
            TokenWidgetUpdater.refreshWidgets(context, fontSizeRepository)
        }
    }

    fun setHapticFeedback(enabled: Boolean) {
        viewModelScope.launch {
            themeRepository.saveHapticFeedback(enabled)
        }
    }
}
