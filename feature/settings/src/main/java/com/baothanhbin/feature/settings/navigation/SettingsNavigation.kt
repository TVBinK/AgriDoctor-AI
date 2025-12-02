package com.baothanhbin.feature.settings.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.baothanhbin.feature.settings.SettingsRoute

const val SETTINGS_ROUTE = "settings"

fun NavController.navigateToSettings() {
    navigate(SETTINGS_ROUTE)
}

fun NavGraphBuilder.settingsScreen(
    onBackClick: () -> Unit
) {
    composable(route = SETTINGS_ROUTE) {
        SettingsRoute(onBackClick = onBackClick)
    }
}

