package io.github.brightennnn.mmtokenmonitor.ui.screens.settings

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.github.brightennnn.mmtokenmonitor.data.repository.ThemeRepository
import io.github.brightennnn.mmtokenmonitor.ui.components.HapticFeedback
import io.github.brightennnn.mmtokenmonitor.ui.components.LocalHapticFeedbackEnabled
import io.github.brightennnn.mmtokenmonitor.ui.screens.settings.apikey.ApiKeyManagementScreenWrapper

object SettingsRoute {
    const val MAIN = "settings_main"
    const val APPEARANCE = "settings_appearance"
    const val API_KEY = "settings_api_key"
    const val ABOUT = "settings_about"
    const val CREDITS = "settings_credits"
}

private const val ANIM_DURATION = 300

private fun AnimatedContentTransitionScope<NavBackStackEntry>.defaultEnterTransition() =
    fadeIn(animationSpec = tween(ANIM_DURATION)) +
        scaleIn(initialScale = 0.92f, animationSpec = tween(ANIM_DURATION))

private fun AnimatedContentTransitionScope<NavBackStackEntry>.defaultExitTransition() =
    fadeOut(animationSpec = tween(ANIM_DURATION)) +
        scaleOut(targetScale = 0.92f, animationSpec = tween(ANIM_DURATION))

@Composable
fun SettingsNavHost(
    themeRepository: ThemeRepository
) {
    val navController = rememberNavController()
    val settingsViewModel: SettingsViewModel = hiltViewModel()
    val themeSettings by themeRepository.themeSettings.collectAsState(initial = io.github.brightennnn.mmtokenmonitor.domain.model.ThemeSettings())

    androidx.compose.runtime.CompositionLocalProvider(
        LocalHapticFeedbackEnabled provides themeSettings.hapticFeedback
    ) {
        NavHost(
            navController = navController,
            startDestination = SettingsRoute.MAIN,
            enterTransition = { defaultEnterTransition() },
            exitTransition = { defaultExitTransition() },
            popEnterTransition = { defaultEnterTransition() },
            popExitTransition = { defaultExitTransition() }
        ) {
            composable(SettingsRoute.MAIN) {
                SettingsMainScreen(
                    onNavigateToAppearance = { navController.navigate(SettingsRoute.APPEARANCE) },
                    onNavigateToApiKey = { navController.navigate(SettingsRoute.API_KEY) },
                    onNavigateToAbout = { navController.navigate(SettingsRoute.ABOUT) },
                    onNavigateToCredits = { navController.navigate(SettingsRoute.CREDITS) }
                )
            }

            composable(
                route = SettingsRoute.APPEARANCE,
                enterTransition = { defaultEnterTransition() },
                exitTransition = { defaultExitTransition() },
                popEnterTransition = { defaultEnterTransition() },
                popExitTransition = { defaultExitTransition() }
            ) {
                SettingsAppearanceScreen(
                    themeSettings = themeSettings,
                    onDarkModeChange = { settingsViewModel.setDarkMode(it) },
                    onDynamicColorChange = { settingsViewModel.setDynamicColor(it) },
                    onPureBlackChange = { settingsViewModel.setPureBlack(it) },
                    onHapticFeedbackChange = {
                        HapticFeedback.isEnabled = it
                        settingsViewModel.setHapticFeedback(it)
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = SettingsRoute.API_KEY,
                enterTransition = { defaultEnterTransition() },
                exitTransition = { defaultExitTransition() },
                popEnterTransition = { defaultEnterTransition() },
                popExitTransition = { defaultExitTransition() }
            ) {
                ApiKeyManagementScreenWrapper(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = SettingsRoute.ABOUT,
                enterTransition = { defaultEnterTransition() },
                exitTransition = { defaultExitTransition() },
                popEnterTransition = { defaultEnterTransition() },
                popExitTransition = { defaultExitTransition() }
            ) {
                SettingsAboutScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = SettingsRoute.CREDITS,
                enterTransition = { defaultEnterTransition() },
                exitTransition = { defaultExitTransition() },
                popEnterTransition = { defaultEnterTransition() },
                popExitTransition = { defaultExitTransition() }
            ) {
                SettingsCreditsScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
