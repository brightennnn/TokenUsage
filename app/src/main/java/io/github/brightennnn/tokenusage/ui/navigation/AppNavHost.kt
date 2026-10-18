package io.github.brightennnn.tokenusage.ui.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.snapshotFlow
import io.github.brightennnn.tokenusage.data.repository.ThemeRepository
import io.github.brightennnn.tokenusage.ui.components.GlmLogo
import io.github.brightennnn.tokenusage.ui.components.HapticFeedback
import io.github.brightennnn.tokenusage.ui.components.MiniMaxLogo
import io.github.brightennnn.tokenusage.ui.screens.home.GlmHomeScreen
import io.github.brightennnn.tokenusage.ui.screens.home.HomeScreen
import io.github.brightennnn.tokenusage.ui.screens.settings.SettingsNavHost

/** 底栏项：文案 + 图标 */
data class BottomNavItem(
    val label: String,
    val icon: ImageVector
)

private val bottomNavDestinations = listOf(
    BottomNavItem(label = "MiniMax", icon = MiniMaxLogo),
    BottomNavItem(label = "智谱", icon = GlmLogo),
    BottomNavItem(label = "设置", icon = Icons.Default.Settings)
)

private const val ANIM_DURATION = 300

@Composable
fun AppNavHost(
    themeRepository: ThemeRepository
) {
    val view = LocalView.current

    // Shared LazyListState for scroll direction detection
    val homeListState = rememberLazyListState()
    val glmListState = rememberLazyListState()

    // Track current tab index: 0=MiniMax 1=智谱 2=设置
    var currentTabIndex by rememberSaveable { mutableStateOf(0) }

    // 来自 GLM 页的 deep-link 请求 — 切到 Settings tab 并跳到 GLM 凭证页
    var pendingGlmCredentialNav by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        // 外层不消费 systemBars inset，否则 innerPadding 会先加一份状态栏高度，
        // 内层各页面的 TopAppBar 再加一份 → 标题栏上方出现双倍空隙。
        // NavigationBar 自身默认消费 navigationBars inset，不受影响。
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            NavigationBar(
                // 与页面背景一致：各页 Scaffold 底色都是 surface，这里用同一个色。
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp
            ) {
                bottomNavDestinations.forEachIndexed { index, item ->
                    val selected = index == currentTabIndex
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            HapticFeedback.perform(view)
                            currentTabIndex = index
                        },
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label
                            )
                        },
                        label = { Text(item.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            selectedTextColor = MaterialTheme.colorScheme.onSurface,
                            indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // tab 切换动画：按索引变化方向决定左右滑动
            // 从 MiniMax(0) → 智谱(1) 目标从右进；智谱(1) → MiniMax(0) 反向同理。
            AnimatedContent(
                targetState = currentTabIndex,
                transitionSpec = {
                    val forward = targetState > initialState
                    val offset = if (forward) 1 else -1
                    (slideInHorizontally(
                        animationSpec = tween(ANIM_DURATION, easing = FastOutSlowInEasing),
                        initialOffsetX = { width -> offset * width / 4 }
                    ) + fadeIn(
                        animationSpec = tween(ANIM_DURATION, easing = FastOutSlowInEasing)
                    )) togetherWith (slideOutHorizontally(
                        animationSpec = tween(ANIM_DURATION, easing = FastOutSlowInEasing),
                        targetOffsetX = { width -> -offset * width / 4 }
                    ) + fadeOut(
                        animationSpec = tween(ANIM_DURATION, easing = FastOutSlowInEasing)
                    ))
                },
                label = "tabSwitch"
            ) { target ->
                when (target) {
                    0 -> HomeScreen(
                        themeRepository = themeRepository,
                        lazyListState = homeListState
                    )
                    1 -> GlmHomeScreen(
                        themeRepository = themeRepository,
                        lazyListState = glmListState,
                        onNavigateToGlmCredential = {
                            HapticFeedback.perform(view)
                            pendingGlmCredentialNav = true
                            currentTabIndex = 2
                        }
                    )
                    else -> SettingsNavHost(
                        themeRepository = themeRepository,
                        pendingGlmCredential = pendingGlmCredentialNav,
                        onPendingGlmCredentialConsumed = { pendingGlmCredentialNav = false }
                    )
                }
            }
        }
    }
}
