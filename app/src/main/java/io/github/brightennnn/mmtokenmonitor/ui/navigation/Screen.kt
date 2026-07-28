package io.github.brightennnn.mmtokenmonitor.ui.navigation

sealed class Screen(val route: String) {
    data object Usage : Screen("/usage")
    data object Settings : Screen("/settings")
    data object AppFontSettings : Screen("/settings/app-font")
}
