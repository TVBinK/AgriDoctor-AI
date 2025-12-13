package com.baothanhbin.feature.processimage.navigation

import android.net.Uri
import android.util.Log
import androidx.navigation.*
import androidx.navigation.compose.composable
import com.baothanhbin.core.model.ApiType
import com.baothanhbin.core.ui.util.LocationStateHolder
import com.baothanhbin.feature.processimage.ProcessImageRoute

const val PROCESS_IMAGE_ROUTE = "PROCESS_IMAGE_ROUTE"
private const val ARG_IMAGE_URI = "imageUri"
private const val ARG_API_TYPE = "apiType"
private const val PROCESS_IMAGE_ROUTE_WITH_ARG = "$PROCESS_IMAGE_ROUTE/{$ARG_IMAGE_URI}"

fun NavController.navigateToProcessImage(
    imageUri: Uri, 
    apiType: String = "DETECT",
    navOptions: NavOptions? = null
) {
    val encoded = Uri.encode(imageUri.toString())
    val route = "$PROCESS_IMAGE_ROUTE/$encoded?$ARG_API_TYPE=$apiType"
    Log.d("ProcessImageNavigation", "Navigating to ProcessImage: apiType=$apiType, route=$route")
    navigate(route, navOptions)
}

fun NavGraphBuilder.processImageScreen(
    navController: NavHostController,
    locationStateHolder: LocationStateHolder
) {
    composable(
        route = "$PROCESS_IMAGE_ROUTE_WITH_ARG?$ARG_API_TYPE={$ARG_API_TYPE}",
        arguments = listOf(
            navArgument(ARG_IMAGE_URI) { type = NavType.StringType },
            navArgument(ARG_API_TYPE) { 
                type = NavType.StringType
                defaultValue = "DETECT"
            }
        )
    ) {
        val uriString = it.arguments?.getString(ARG_IMAGE_URI)
        val uri = uriString?.let { s -> Uri.parse(Uri.decode(s)) }
        val apiTypeString = it.arguments?.getString(ARG_API_TYPE) ?: "DETECT"
        val apiType = when (apiTypeString) {
            "CLASSIFY" -> ApiType.CLASSIFY
            else -> ApiType.DETECT
        }
        Log.d("ProcessImageNavigation", "ProcessImageRoute: apiTypeString=$apiTypeString, apiType=$apiType")
        ProcessImageRoute(
            navController = navController,
            imageUri = uri,
            locationStateHolder = locationStateHolder,
            apiType = apiType
        )
    }
}


