package io.github.brightennnn.mmtokenmonitor.ui.screens.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.brightennnn.mmtokenmonitor.data.repository.FontSizeRepository
import io.github.brightennnn.mmtokenmonitor.domain.model.GlmQuota
import io.github.brightennnn.mmtokenmonitor.domain.model.ModelQuota
import io.github.brightennnn.mmtokenmonitor.domain.repository.GlmRepository
import io.github.brightennnn.mmtokenmonitor.domain.repository.TokenRepository
import io.github.brightennnn.mmtokenmonitor.widget.GlmWidgetUpdater
import io.github.brightennnn.mmtokenmonitor.widget.TokenWidgetUpdater
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    // MiniMax
    val quotas: List<ModelQuota> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val hasKey: Boolean = false,
    val isRefreshing: Boolean = false,
    // GLM — 独立字段,两套系统互不干扰
    val glmQuotas: List<GlmQuota> = emptyList(),
    val glmLevel: String? = null,
    val hasGlmKey: Boolean = false,
    val isGlmLoading: Boolean = false,
    val glmError: String? = null,
    /** 用于通知 HomeScreen 跳转到 GLM 凭证页 */
    val glmNavigateToCredential: Boolean = false
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: TokenRepository,
    private val glmRepository: GlmRepository,
    private val fontSizeRepository: FontSizeRepository,
    private val glmWidgetUpdater: GlmWidgetUpdater,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        checkKeys()
        observeGlmToken()
    }

    private fun checkKeys() {
        viewModelScope.launch {
            val key = repository.activeApiKey.first()
            val groupId = repository.activeGroupId.first()
            val hasKey = !key.isNullOrBlank()
            _uiState.update { it.copy(hasKey = hasKey) }
            if (hasKey) {
                fetchQuotas(key!!, groupId ?: "")
            }
        }
    }

    private fun observeGlmToken() {
        viewModelScope.launch {
            glmRepository.glmToken.collect { token ->
                val hasGlmKey = !token.isNullOrBlank()
                val wasEmpty = !_uiState.value.hasGlmKey
                _uiState.update { it.copy(hasGlmKey = hasGlmKey) }
                if (hasGlmKey && (wasEmpty || _uiState.value.glmQuotas.isEmpty())) {
                    fetchGlm()
                } else if (!hasGlmKey) {
                    _uiState.update { it.copy(glmQuotas = emptyList(), glmError = null, glmLevel = null) }
                }
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            val savedKey = repository.activeApiKey.first()
            val groupId = repository.activeGroupId.first()
            if (!savedKey.isNullOrBlank()) {
                fetchQuotas(savedKey, groupId ?: "")
            } else {
                _uiState.update { it.copy(hasKey = false) }
            }
            val glmToken = glmRepository.glmToken.first()
            if (!glmToken.isNullOrBlank()) {
                fetchGlm()
            }
        }
    }

    private fun fetchQuotas(key: String, groupId: String = "") {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            repository.fetchQuotas(key, groupId)
                .onSuccess { quotas ->
                    _uiState.update { it.copy(quotas = quotas, isLoading = false) }
                    TokenWidgetUpdater.updateWidget(context, quotas, fontSizeRepository)
                }
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message ?: "查询失败", isLoading = false) }
                }
        }
    }

    private fun fetchGlm() {
        viewModelScope.launch {
            _uiState.update { it.copy(isGlmLoading = true, glmError = null) }
            glmRepository.fetchQuotas()
                .onSuccess { quotas ->
                    val level = quotas.firstOrNull()?.level
                    _uiState.update { it.copy(
                        glmQuotas = quotas,
                        glmLevel = level,
                        isGlmLoading = false,
                        glmError = null,
                    ) }
                    glmWidgetUpdater.updateGlmWidget(quotas)
                }
                .onFailure { e ->
                    _uiState.update { it.copy(
                        isGlmLoading = false,
                        glmError = e.message ?: "查询失败"
                    ) }
                }
        }
    }

    /** 由 GLM 错误卡点击触发 — 通知 Home 跳转到 Settings 的 GLM 凭证页 */
    fun requestGlmCredentialNavigation() {
        _uiState.update { it.copy(glmNavigateToCredential = true) }
    }

    fun consumeGlmCredentialNavigation() {
        _uiState.update { it.copy(glmNavigateToCredential = false) }
    }
}
