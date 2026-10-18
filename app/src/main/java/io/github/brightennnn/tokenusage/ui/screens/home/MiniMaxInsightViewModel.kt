package io.github.brightennnn.tokenusage.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.brightennnn.tokenusage.domain.model.MiniMaxCredit
import io.github.brightennnn.tokenusage.domain.model.UsagePeriod
import io.github.brightennnn.tokenusage.domain.model.UsageStats
import io.github.brightennnn.tokenusage.domain.model.UsageSummary
import io.github.brightennnn.tokenusage.domain.repository.MiniMaxWebTokenRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * MiniMax 积分余额 + 四周期用量明细。
 *
 * 独立于 [HomeViewModel]：5小时/本周额度走 API Key（sk-cp），这两块走网页 `_token`，
 * 两套凭证互不影响 —— 没配 _token 时本 ViewModel 静默保持空态，不影响主用量展示。
 */
data class MiniMaxInsightUiState(
    val hasWebToken: Boolean = false,
    val credit: MiniMaxCredit? = null,
    val isCreditLoading: Boolean = false,
    val creditError: String? = null,
    // 用量明细
    val period: UsagePeriod = UsagePeriod.DAY,
    val stats: UsageStats? = null,
    val summary: UsageSummary? = null,
    val isStatsLoading: Boolean = false,
    val statsError: String? = null
)

@HiltViewModel
class MiniMaxInsightViewModel @Inject constructor(
    private val webRepository: MiniMaxWebTokenRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MiniMaxInsightUiState())
    val uiState: StateFlow<MiniMaxInsightUiState> = _uiState.asStateFlow()

    /** 切周期时取消上一次未完成的请求，避免慢响应覆盖新周期 */
    private var statsJob: Job? = null

    init {
        observeWebToken()
    }

    private fun observeWebToken() {
        viewModelScope.launch {
            webRepository.webToken.collect { token ->
                val has = !token.isNullOrBlank()
                val had = _uiState.value.hasWebToken
                _uiState.update { it.copy(hasWebToken = has) }
                when {
                    // 首次配置或从无到有 → 拉积分
                    has && !had -> fetchCredit()
                    // token 被删除 → 清空所有 _token 相关数据
                    !has -> _uiState.update {
                        it.copy(
                            credit = null,
                            creditError = null,
                            stats = null,
                            summary = null,
                            statsError = null
                        )
                    }
                }
            }
        }
    }

    fun fetchCredit() {
        viewModelScope.launch {
            _uiState.update { it.copy(isCreditLoading = true, creditError = null) }
            webRepository.fetchCredit()
                .onSuccess { credit ->
                    _uiState.update { it.copy(credit = credit, isCreditLoading = false) }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(credit = null, isCreditLoading = false, creditError = e.message)
                    }
                }
        }
    }

    fun selectPeriod(period: UsagePeriod) {
        if (_uiState.value.period == period && _uiState.value.stats != null) return
        _uiState.update { it.copy(period = period) }
        fetchStats(period)
    }

    fun refreshStats() {
        fetchStats(_uiState.value.period)
    }

    fun fetchStats(period: UsagePeriod = _uiState.value.period) {
        if (!_uiState.value.hasWebToken) return
        statsJob?.cancel()
        statsJob = viewModelScope.launch {
            _uiState.update { it.copy(isStatsLoading = true, statsError = null) }
            webRepository.fetchUsageStats(period)
                .onSuccess { stats ->
                    // 「全部」周期额外拉长期指标（累计天数/连续活跃/排名）
                    if (period == UsagePeriod.ALL) {
                        webRepository.fetchUsageSummary()
                            .onSuccess { s -> _uiState.update { it.copy(summary = s) } }
                    }
                    _uiState.update {
                        it.copy(stats = stats, isStatsLoading = false, statsError = null)
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(stats = null, isStatsLoading = false, statsError = e.message)
                    }
                }
        }
    }

    /** 首次进入且已配 token 时预加载默认周期 */
    fun ensureLoaded() {
        viewModelScope.launch {
            if (!webRepository.webToken.first().isNullOrBlank()) {
                if (_uiState.value.credit == null) fetchCredit()
                if (_uiState.value.stats == null) fetchStats(UsagePeriod.DAY)
            }
        }
    }
}
