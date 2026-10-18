package io.github.brightennnn.tokenusage.ui.screens.settings.minimaxweb

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.brightennnn.tokenusage.domain.repository.MiniMaxWebTokenRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.TimeZone
import javax.inject.Inject

/**
 * 从用户输入中提取 MiniMax 网页登录态 `_token`。
 *
 * 支持两种输入（与 GLM 的 JWT 提取完全对称）：
 *   1. 整段 Cookie（含 `_token=eyJ...;`）
 *   2. 裸 JWT（`eyJ.eyJ.signature`）
 *
 * 返回 Result.failure 时，message 是用户可读的错误。
 */
private const val MINIMAX_COOKIE = "_token"
private val JWT_REGEX = Regex("^[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+$")

private fun extractMinimaxToken(raw: String): Result<String> {
    val trimmed = raw.trim()
    if (trimmed.isBlank()) {
        return Result.failure(IllegalArgumentException("Token 不能为空"))
    }
    // 1) 含 cookie 名称 → 整段 cookie 模式
    if (trimmed.contains(MINIMAX_COOKIE)) {
        val match = Regex("(?:^|;\\s*)$MINIMAX_COOKIE=([^;]+)").find(trimmed)
            ?: return Result.failure(IllegalArgumentException("Cookie 中未找到 $MINIMAX_COOKIE"))
        val value = match.groupValues[1].trim()
        if (!JWT_REGEX.matches(value)) {
            return Result.failure(IllegalArgumentException("提取的值不是合法 JWT 格式"))
        }
        return Result.success(value)
    }
    // 2) 不含 cookie 名称 → 当作裸 JWT
    if (JWT_REGEX.matches(trimmed)) {
        return Result.success(trimmed)
    }
    return Result.failure(IllegalArgumentException("格式无法识别：请粘贴整段 Cookie 或裸 JWT（eyJ...）"))
}

/**
 * 解析 JWT payload 里的 exp（秒）→ 毫秒时间戳。
 * 不用 Base64 库，纯手写 URL-safe 解码 + JSON 正则，避免 android.util 依赖与 R8 混淆问题。
 */
private fun jwtExpiryMs(jwt: String): Long? {
    val parts = jwt.split('.')
    if (parts.size < 2) return null
    val payload = runCatching {
        String(
            java.util.Base64.getUrlDecoder().decode(parts[1]),
            Charsets.UTF_8
        )
    }.getOrNull() ?: return null
    val exp = Regex("\"exp\"\\s*:\\s*(\\d+)").find(payload)
        ?.groupValues?.get(1)?.toLongOrNull() ?: return null
    return exp * 1000L
}

/** 毫秒时间戳 → "MM-dd HH:mm"（本机时区）；无效返回 null。 */
private fun formatExpiry(ms: Long): String? {
    if (ms <= 0L) return null
    val cal = java.util.Calendar.getInstance(TimeZone.getDefault())
    cal.timeInMillis = ms
    return "%02d-%02d %02d:%02d".format(
        cal.get(java.util.Calendar.MONTH) + 1,
        cal.get(java.util.Calendar.DAY_OF_MONTH),
        cal.get(java.util.Calendar.HOUR_OF_DAY),
        cal.get(java.util.Calendar.MINUTE)
    )
}

data class MiniMaxWebTokenUiState(
    val tokenMasked: String = "",      // 脱敏显示用：前 4 + **** + 后 4
    val hasToken: Boolean = false,
    val isEditing: Boolean = false,
    val draftToken: String = "",       // 编辑时的临时输入
    val showToken: Boolean = false,    // 编辑时是否明文显示
    val isLoading: Boolean = false,
    val testResult: String? = null,    // "测试成功" / 错误信息
    val error: String? = null,
    val expiryText: String? = null,    // 设置项描述用："有效期至 10-24 12:00"
    val isExpired: Boolean = false
)

@HiltViewModel
class MiniMaxWebTokenViewModel @Inject constructor(
    private val repository: MiniMaxWebTokenRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MiniMaxWebTokenUiState())
    val uiState: StateFlow<MiniMaxWebTokenUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.webToken.collect { token ->
                val raw = token.orEmpty()
                val expiryMs = raw.takeIf { it.isNotBlank() }?.let { jwtExpiryMs(it) }
                _uiState.update {
                    it.copy(
                        hasToken = raw.isNotBlank(),
                        tokenMasked = maskToken(raw),
                        expiryText = expiryMs?.let { ms -> formatExpiry(ms)?.let { t -> "有效期至 $t" } },
                        isExpired = expiryMs != null && expiryMs <= System.currentTimeMillis()
                    )
                }
            }
        }
    }

    fun startEditing() {
        viewModelScope.launch {
            val current = repository.webToken.first().orEmpty()
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
        val raw = _uiState.value.draftToken
        val token = extractMinimaxToken(raw).fold(
            onSuccess = { it },
            onFailure = { e ->
                _uiState.update { it.copy(error = e.message ?: "Token 不能为空") }
                return
            }
        )
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            repository.saveToken(token)
            _uiState.update { it.copy(isLoading = false, isEditing = false, draftToken = "") }
        }
    }

    /** 保存并立即测一次 — 用户填完想确认对不对（测的是重置券接口） */
    fun saveAndTest() {
        val raw = _uiState.value.draftToken
        val token = extractMinimaxToken(raw).fold(
            onSuccess = { it },
            onFailure = { e ->
                _uiState.update { it.copy(error = e.message ?: "Token 不能为空") }
                return
            }
        )
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, testResult = null) }
            repository.saveToken(token)
            val result = repository.fetchResetCards(token)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    isEditing = false,
                    draftToken = "",
                    testResult = if (result.isSuccess) {
                        "测试成功 · 可用重置券 ${result.getOrDefault(emptyList()).size} 张"
                    } else {
                        (result.exceptionOrNull()?.message ?: "测试失败")
                    }
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
