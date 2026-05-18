package io.github.brightennnn.mmtokenmonitor.ui.screens.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.brightennnn.mmtokenmonitor.data.repository.FontSizeRepository
import io.github.brightennnn.mmtokenmonitor.domain.repository.TokenRepository
import io.github.brightennnn.mmtokenmonitor.widget.TokenWidgetUpdater
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val quotas: List<io.github.brightennnn.mmtokenmonitor.domain.model.ModelQuota> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val hasKey: Boolean = false,
    val isRefreshing: Boolean = false
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: TokenRepository,
    private val fontSizeRepository: FontSizeRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        checkHasKey()
    }

    private fun checkHasKey() {
        viewModelScope.launch {
            val key = repository.activeApiKey.first()
            val hasKey = !key.isNullOrBlank()
            _uiState.update { it.copy(hasKey = hasKey) }
            if (hasKey) {
                fetchQuotas(key!!)
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            val savedKey = repository.activeApiKey.first()
            if (!savedKey.isNullOrBlank()) {
                fetchQuotas(savedKey)
            } else {
                _uiState.update { it.copy(hasKey = false) }
            }
        }
    }

    private fun fetchQuotas(key: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            repository.fetchQuotas(key)
                .onSuccess { quotas ->
                    _uiState.update { it.copy(quotas = quotas, isLoading = false) }
                    // 同步刷新 widget
                    TokenWidgetUpdater.updateWidget(context, quotas, fontSizeRepository)
                }
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message ?: "查询失败", isLoading = false) }
                }
        }
    }
}
