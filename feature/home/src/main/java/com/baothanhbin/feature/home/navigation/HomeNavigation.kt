package com.baothanhbin.feature.home.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.baothanhbin.feature.home.HomeRoute
import com.baothanhbin.feature.home.HomeScreen

const val HOME_ROUTE = "HOME_ROUTE"

fun NavController.navigateToHome(navOptions: NavOptions? = null) {
    navigate(
        route = HOME_ROUTE,
        navOptions = navOptions
    )
}

fun NavGraphBuilder.homeScreen(
) {
    composable(route = HOME_ROUTE) {
        HomeRoute()
    }
}