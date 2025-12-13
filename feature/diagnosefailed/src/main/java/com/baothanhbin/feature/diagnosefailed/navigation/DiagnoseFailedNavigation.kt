package com.baothanhbin.feature.diagnosefailed.navigation

import android.net.Uri
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.baothanhbin.core.model.ApiType
import com.baothanhbin.feature.diagnosefailed.DiagnoseFailedRoute

const val DIAGNOSE_FAILED_ROUTE = "DIAGNOSE_FAILED_ROUTE"
private const val ARG_IMAGE_URI = "imageUri"
private const val ARG_API_TYPE = "apiType"
private const val DIAGNOSE_FAILED_ROUTE_WITH_ARG = "$DIAGNOSE_FAILED_ROUTE/{$ARG_IMAGE_URI}?$ARG_API_TYPE={$ARG_API_TYPE}"

fun NavController.navigateToDiagnoseFailed(
    imageUri: Uri? = null,
    apiType: ApiType = ApiType.DETECT,
    navOptions: NavOptions? = null
) {
    val apiTypeString = if (apiType == ApiType.CLASSIFY) "CLASSIFY" else "DETECT"
    val route = if (imageUri != null) {
        val encoded = Uri.encode(imageUri.toString())
        "$DIAGNOSE_FAILED_ROUTE/$encoded?$ARG_API_TYPE=$apiTypeString"
    } else {
        "$DIAGNOSE_FAILED_ROUTE?$ARG_API_TYPE=$apiTypeString"
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
            },
            navArgument(ARG_API_TYPE) {
                type = NavType.StringType
                defaultValue = "DETECT"
            }
        )
    ) {
        val uriString = it.arguments?.getString(ARG_IMAGE_URI)
        val imageUri = uriString?.let { s -> Uri.parse(Uri.decode(s)) }
        val apiTypeString = it.arguments?.getString(ARG_API_TYPE) ?: "DETECT"
        val apiType = when (apiTypeString) {
            "CLASSIFY" -> ApiType.CLASSIFY
            else -> ApiType.DETECT
        }
        DiagnoseFailedRoute(
            navController = navController,
            imageUri = imageUri,
            apiType = apiType
        )
    }
    // Route without argument (for backward compatibility)
    composable(
        route = "$DIAGNOSE_FAILED_ROUTE?$ARG_API_TYPE={$ARG_API_TYPE}",
        arguments = listOf(
            navArgument(ARG_API_TYPE) {
                type = NavType.StringType
                defaultValue = "DETECT"
            }
        )
    ) {
        val apiTypeString = it.arguments?.getString(ARG_API_TYPE) ?: "DETECT"
        val apiType = when (apiTypeString) {
            "CLASSIFY" -> ApiType.CLASSIFY
            else -> ApiType.DETECT
        }
        DiagnoseFailedRoute(
            navController = navController,
            imageUri = null,
            apiType = apiType
        )
    }
}

