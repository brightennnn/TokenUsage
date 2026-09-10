package io.github.brightennnn.mmtokenmonitor.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.LocalView
import androidx.compose.runtime.snapshotFlow
import io.github.brightennnn.mmtokenmonitor.data.repository.ThemeRepository
import io.github.brightennnn.mmtokenmonitor.ui.components.BottomNavItem
import io.github.brightennnn.mmtokenmonitor.ui.components.FloatingBottomNav
import io.github.brightennnn.mmtokenmonitor.ui.components.HapticFeedback
import io.github.brightennnn.mmtokenmonitor.ui.screens.home.HomeScreen
import io.github.brightennnn.mmtokenmonitor.ui.screens.settings.SettingsNavHost

private val bottomNavDestinations = listOf(
    BottomNavItem(label = "用量", icon = Icons.Default.Home),
    BottomNavItem(label = "设置", icon = Icons.Default.Settings)
)

private const val ANIM_DURATION = 300

@Composable
fun AppNavHost(
    themeRepository: ThemeRepository
) {
    val view = LocalView.current
    val statusBarPadding = WindowInsets.statusBars.asPaddingValues()
    val navBarPadding = WindowInsets.navigationBars.asPaddingValues()

    // Shared LazyListState for scroll direction detection
    val homeListState = rememberLazyListState()

    // Track current tab index
    var currentTabIndex by rememberSaveable { mutableStateOf(0) }

    // 来自 Home 的 deep-link 请求 — 切到 Settings tab 并跳到 GLM 凭证页
    var pendingGlmCredentialNav by remember { mutableStateOf(false) }

    // Bottom bar visibility:下滑隐藏，上滑显示
    var isBottomBarVisible by rememberSaveable { mutableStateOf(true) }

    // Detect scroll direction using LazyListState
    LaunchedEffect(homeListState, currentTabIndex) {
        // Only react to scroll on the home tab (index 0)
        if (currentTabIndex != 0) return@LaunchedEffect

        snapshotFlow {
            val firstVisible = homeListState.firstVisibleItemIndex
            val scrollOffset = homeListState.firstVisibleItemScrollOffset
            firstVisible to scrollOffset
        }.collect { (firstVisible, scrollOffset) ->
            // First item visible and scrolled to top (or near top) → show
            // First item not visible or scrolled down → determine direction
            if (firstVisible == 0 && scrollOffset <= 0) {
                // At top → show
                isBottomBarVisible = true
            } else if (firstVisible > 0) {
                // Scrolled past first item → hide
                isBottomBarVisible = false
            }
            // When firstVisible == 0 and scrollOffset > 0, we're still at top but
            // the content may be scrolling. The direction is determined by scrollOffset
            // change, which we track below
        }
    }

    // Track scroll direction more precisely
    var previousScrollOffset by remember { mutableStateOf(0) }
    LaunchedEffect(homeListState.firstVisibleItemScrollOffset, currentTabIndex) {
        if (currentTabIndex != 0) return@LaunchedEffect
        val currentOffset = homeListState.firstVisibleItemScrollOffset
        val firstVisible = homeListState.firstVisibleItemIndex

        if (firstVisible == 0) {
            // At the first item — show when scrolled to top
            if (currentOffset <= 10) {
                isBottomBarVisible = true
            }
        } else {
            // Past first item — determine direction
            if (currentOffset > previousScrollOffset) {
                // Scrolling down → hide
                isBottomBarVisible = false
            } else if (currentOffset < previousScrollOffset) {
                // Scrolling up → show
                isBottomBarVisible = true
            }
        }
        previousScrollOffset = currentOffset
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Content layer
        when (currentTabIndex) {
            0 -> HomeScreen(
                themeRepository = themeRepository,
                lazyListState = homeListState,
                onNavigateToGlmCredential = {
                    HapticFeedback.perform(view)
                    pendingGlmCredentialNav = true
                    currentTabIndex = 1
                }
            )
            1 -> SettingsNavHost(
                themeRepository = themeRepository,
                pendingGlmCredential = pendingGlmCredentialNav,
                onPendingGlmCredentialConsumed = { pendingGlmCredentialNav = false }
            )
        }

        // Floating bottom nav with MD3E-style animation
        AnimatedVisibility(
            visible = isBottomBarVisible,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(
                    durationMillis = ANIM_DURATION,
                    easing = FastOutSlowInEasing
                )
            ) + fadeIn(
                animationSpec = tween(
                    durationMillis = ANIM_DURATION,
                    easing = FastOutSlowInEasing
                )
            ),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(
                    durationMillis = ANIM_DURATION,
                    easing = FastOutSlowInEasing
                )
            ) + fadeOut(
                animationSpec = tween(
                    durationMillis = ANIM_DURATION,
                    easing = FastOutSlowInEasing
                )
            ),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = navBarPadding.calculateBottomPadding())
        ) {
            FloatingBottomNav(
                selectedIndex = currentTabIndex,
                items = bottomNavDestinations,
                onItemClick = { index ->
                    HapticFeedback.perform(view)
                    currentTabIndex = index
                }
            )
        }
    }
}
