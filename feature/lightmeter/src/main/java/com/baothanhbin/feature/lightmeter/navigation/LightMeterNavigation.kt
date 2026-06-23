package com.baothanhbin.feature.lightmeter.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.baothanhbin.core.ui.navigation.slideInFromBottomHalf
import com.baothanhbin.core.ui.navigation.slideOutToBottomHalf
import com.baothanhbin.feature.lightmeter.LightMeterRoute

const val LIGHT_METER_ROUTE = "light_meter"

fun NavController.navigateToLightMeter(navOptions: NavOptions? = null) {
    navigate(LIGHT_METER_ROUTE, navOptions)
}

fun NavGraphBuilder.lightMeterScreen(
    navController: NavHostController
) {
    composable(
        route = LIGHT_METER_ROUTE,
        enterTransition = { slideInFromBottomHalf() },
        popExitTransition = { slideOutToBottomHalf() }
    ) {
        LightMeterRoute(
            navController = navController
        )
    }
}

