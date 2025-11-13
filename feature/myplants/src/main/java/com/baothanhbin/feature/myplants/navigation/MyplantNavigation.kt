package com.baothanhbin.feature.myplants.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.baothanhbin.feature.myplants.MyplantRoute

const val MY_PLANTS_ROUTE = "MY_PLANTS_ROUTE"

fun NavController.navigateToMyplants(navOptions: NavOptions? = null) {
    navigate(
        route = MY_PLANTS_ROUTE,
        navOptions = navOptions
    )
}

fun NavGraphBuilder.myplantScreen(
    locationStateHolder: com.baothanhbin.core.ui.util.LocationStateHolder
) {
    composable(route = MY_PLANTS_ROUTE) {
        MyplantRoute(locationStateHolder = locationStateHolder)
    }
}