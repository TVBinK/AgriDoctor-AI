package com.baothanhbin.feature.diagnoseresult.navigation

import android.net.Uri
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.baothanhbin.feature.diagnoseresult.DiagnoseResultRoute

const val DIAGNOSE_RESULT_ROUTE = "DIAGNOSE_RESULT_ROUTE"
private const val ARG_IMAGE_URI = "imageUri"
private const val DIAGNOSE_RESULT_ROUTE_WITH_ARG = "$DIAGNOSE_RESULT_ROUTE/{$ARG_IMAGE_URI}"

fun NavController.navigateToDiagnoseResult(
    imageUri: Uri? = null,
    navOptions: NavOptions? = null
) {
    val route = if (imageUri != null) {
        val encoded = Uri.encode(imageUri.toString())
        "$DIAGNOSE_RESULT_ROUTE/$encoded"
    } else {
        DIAGNOSE_RESULT_ROUTE
    }
    navigate(route, navOptions)
}

fun NavGraphBuilder.diagnoseResultScreen(navController: NavHostController? = null) {
    // Route with imageUri argument
    composable(
        route = DIAGNOSE_RESULT_ROUTE_WITH_ARG,
        arguments = listOf(
            navArgument(ARG_IMAGE_URI) {
                type = NavType.StringType
                nullable = true
            }
        )
    ) {
        val uriString = it.arguments?.getString(ARG_IMAGE_URI)
        val imageUri = uriString?.let { s -> Uri.parse(Uri.decode(s)) }
        DiagnoseResultRoute(
            navController = navController,
            imageUri = imageUri
        )
    }
    // Route without argument (for backward compatibility)
    composable(route = DIAGNOSE_RESULT_ROUTE) {
        DiagnoseResultRoute(
            navController = navController,
            imageUri = null
        )
    }
}
