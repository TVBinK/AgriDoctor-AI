package com.baothanhbin.feature.diagnoseresult.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.baothanhbin.feature.diagnoseresult.DiagnoseResultRoute

const val DIAGNOSE_RESULT_ROUTE = "DIAGNOSE_RESULT_ROUTE"

fun NavController.navigateToDiagnoseResult(navOptions: NavOptions? = null) {
    navigate(DIAGNOSE_RESULT_ROUTE, navOptions)
}

fun NavGraphBuilder.diagnoseResultScreen() {
    composable(route = DIAGNOSE_RESULT_ROUTE) {
        DiagnoseResultRoute()
    }
}
