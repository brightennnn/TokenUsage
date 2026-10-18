package io.github.brightennnn.tokenusage.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import io.github.brightennnn.tokenusage.data.repository.ThemeRepository
import io.github.brightennnn.tokenusage.ui.components.GlmErrorCard
import io.github.brightennnn.tokenusage.ui.components.GlmNotConfiguredCard
import io.github.brightennnn.tokenusage.ui.components.GlmQuotaCard
import io.github.brightennnn.tokenusage.ui.components.GlmSectionHeader
import io.github.brightennnn.tokenusage.ui.components.HapticFeedback
import io.github.brightennnn.tokenusage.ui.components.LocalHapticFeedbackEnabled

/**
 * 智谱（GLM Coding Plan）独立页。
 *
 * 从原 HomeScreen 拆出（原页面 MiniMax + GLM 混排），本页只管 GLM：
 * 5小时窗 + 周窗（过滤掉 MCP 月窗 rawUnit=5），以及未配置凭证 / 拉取失败的引导卡。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlmHomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    themeRepository: ThemeRepository? = null,
    lazyListState: LazyListState = rememberLazyListState(),
    onNavigateToGlmCredential: () -> Unit = {},
) {
    val view = LocalView.current
    val uiState by viewModel.uiState.collectAsState()

    val themeSettings by themeRepository?.themeSettings?.collectAsState(initial = null)
        ?: remember { mutableStateOf(null) }

    val pullToRefreshState = rememberPullToRefreshState()

    // GLM 凭证页跳转触发
    LaunchedEffect(uiState.glmNavigateToCredential) {
        if (uiState.glmNavigateToCredential) {
            viewModel.consumeGlmCredentialNavigation()
            onNavigateToGlmCredential()
        }
    }

    CompositionLocalProvider(LocalHapticFeedbackEnabled provides (themeSettings?.hapticFeedback ?: true)) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("智谱") },
                    actions = {
                        if (uiState.hasGlmKey) {
                            IconButton(
                                onClick = {
                                    HapticFeedback.perform(view)
                                    viewModel.refreshGlm()
                                }
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = "刷新")
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        ) { innerPadding ->
            PullToRefreshBox(
                isRefreshing = uiState.isGlmLoading,
                onRefresh = { viewModel.refreshGlm() },
                state = pullToRefreshState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = innerPadding.calculateTopPadding())
            ) {
                if (uiState.hasGlmKey || uiState.glmError != null) {
                    LazyColumn(
                        state = lazyListState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item { GlmSectionHeader(level = uiState.glmLevel, hasError = uiState.glmError != null) }

                        when {
                            !uiState.hasGlmKey -> {
                                item {
                                    GlmNotConfiguredCard(
                                        onClick = { viewModel.requestGlmCredentialNavigation() }
                                    )
                                }
                            }
                            uiState.glmError != null && uiState.glmQuotas.isEmpty() -> {
                                item {
                                    GlmErrorCard(
                                        message = uiState.glmError ?: "未知错误",
                                        onClick = { viewModel.requestGlmCredentialNavigation() }
                                    )
                                }
                            }
                            else -> {
                                // 5小时窗、周窗（过滤掉 MCP 月窗 rawUnit=5）
                                uiState.glmQuotas.filter { it.rawUnit != 5 }.forEach { quota ->
                                    item(key = "glm-${quota.rawUnit}") {
                                        GlmQuotaCard(quota = quota)
                                    }
                                }
                                if (uiState.isGlmLoading) {
                                    item {
                                        Box(
                                            modifier = Modifier.fillMaxWidth().padding(8.dp),
                                            contentAlignment = Alignment.Center
                                        ) { CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp) }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "暂无用量数据",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
