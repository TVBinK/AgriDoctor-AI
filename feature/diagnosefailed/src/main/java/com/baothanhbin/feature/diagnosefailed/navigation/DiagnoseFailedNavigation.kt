package com.baothanhbin.feature.diagnosefailed.navigation

import android.net.Uri
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.baothanhbin.feature.diagnosefailed.DiagnoseFailedRoute

const val DIAGNOSE_FAILED_ROUTE = "DIAGNOSE_FAILED_ROUTE"
private const val ARG_IMAGE_URI = "imageUri"
private const val DIAGNOSE_FAILED_ROUTE_WITH_ARG = "$DIAGNOSE_FAILED_ROUTE/{$ARG_IMAGE_URI}"

fun NavController.navigateToDiagnoseFailed(
    imageUri: Uri? = null,
    navOptions: NavOptions? = null
) {
    val route = if (imageUri != null) {
        val encoded = Uri.encode(imageUri.toString())
        "$DIAGNOSE_FAILED_ROUTE/$encoded"
    } else {
        DIAGNOSE_FAILED_ROUTE
    }
    navigate(route, navOptions)
}

fun NavGraphBuilder.diagnoseFailedScreen(navController: NavHostController) {
    // Route with imageUri argument
    composable(
        route = DIAGNOSE_FAILED_ROUTE_WITH_ARG,
        arguments = listOf(
            navArgument(ARG_IMAGE_URI) {
                type = NavType.StringType
                nullable = true
            }
        )
    ) {
        val uriString = it.arguments?.getString(ARG_IMAGE_URI)
        val imageUri = uriString?.let { s -> Uri.parse(Uri.decode(s)) }
        DiagnoseFailedRoute(
            navController = navController,
            imageUri = imageUri
        )
    }
    // Route without argument (for backward compatibility)
    composable(route = DIAGNOSE_FAILED_ROUTE) {
        DiagnoseFailedRoute(
            navController = navController,
            imageUri = null
        )
    }
}

