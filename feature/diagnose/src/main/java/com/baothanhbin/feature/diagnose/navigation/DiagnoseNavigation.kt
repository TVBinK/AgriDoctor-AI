package com.baothanhbin.feature.diagnose.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.baothanhbin.feature.diagnose.DiagnoseRoute

const val DIAGNOSE_ROUTE = "DIAGNOSE_ROUTE"

fun NavController.navigateToDiagnose(navOptions: NavOptions? = null) {
    navigate(
        route = DIAGNOSE_ROUTE,
        navOptions = navOptions
    )
}

fun NavGraphBuilder.diagnoseScreen(
) {
    composable(route = DIAGNOSE_ROUTE) {
        DiagnoseRoute()
    }
}