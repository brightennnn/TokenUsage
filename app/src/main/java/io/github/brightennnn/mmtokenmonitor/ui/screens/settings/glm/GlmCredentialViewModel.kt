package io.github.brightennnn.mmtokenmonitor.ui.screens.settings.glm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.brightennnn.mmtokenmonitor.domain.repository.GlmRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GlmCredentialUiState(
    val tokenMasked: String = "",   // 脱敏显示用：前 4 + **** + 后 4
    val hasToken: Boolean = false,
    val isEditing: Boolean = false,
    val draftToken: String = "",    // 编辑时的临时输入
    val showToken: Boolean = false, // 编辑时是否明文显示
    val isLoading: Boolean = false,
    val testResult: String? = null, // "测试成功" / 错误信息
    val error: String? = null
)

@HiltViewModel
class GlmCredentialViewModel @Inject constructor(
    private val repository: GlmRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GlmCredentialUiState())
    val uiState: StateFlow<GlmCredentialUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.glmToken.collect { token ->
                _uiState.update {
                    it.copy(
                        hasToken = !token.isNullOrBlank(),
                        tokenMasked = maskToken(token.orEmpty())
                    )
                }
            }
        }
    }

    fun startEditing() {
        viewModelScope.launch {
            val current = repository.glmToken.first().orEmpty()
            _uiState.update {
                it.copy(
                    isEditing = true,
                    draftToken = current,
                    showToken = false,
                    testResult = null,
                    error = null
                )
            }
        }
    }

    fun cancelEditing() {
        _uiState.update { it.copy(isEditing = false, draftToken = "", showToken = false, error = null, testResult = null) }
    }

    fun updateDraft(token: String) {
        _uiState.update { it.copy(draftToken = token, error = null, testResult = null) }
    }

    fun toggleShowToken() {
        _uiState.update { it.copy(showToken = !it.showToken) }
    }

    fun save() {
        val token = _uiState.value.draftToken.trim()
        if (token.isBlank()) {
            _uiState.update { it.copy(error = "JWT 不能为空") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            // 立刻保存，再异步测一次
            repository.saveToken(token)
            _uiState.update { it.copy(isLoading = false, isEditing = false, draftToken = "") }
        }
    }

    /** 保存并立即测一次 — 用户填完想确认对不对 */
    fun saveAndTest() {
        val token = _uiState.value.draftToken.trim()
        if (token.isBlank()) {
            _uiState.update { it.copy(error = "JWT 不能为空") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, testResult = null) }
            repository.saveToken(token)
            // 用刚保存的 token 测
            val result = repository.fetchQuotas(token)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    isEditing = false,
                    draftToken = "",
                    testResult = if (result.isSuccess) "测试成功" else (result.exceptionOrNull()?.message ?: "测试失败")
                )
            }
        }
    }

    fun deleteToken() {
        viewModelScope.launch {
            repository.clearToken()
            _uiState.update {
                it.copy(
                    isEditing = false,
                    draftToken = "",
                    testResult = null,
                    error = null
                )
            }
        }
    }

    private fun maskToken(token: String): String {
        if (token.isEmpty()) return ""
        return if (token.length > 12) {
            token.take(4) + "****" + token.takeLast(4)
        } else {
            "****"
        }
    }
}
