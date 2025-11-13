package com.baothanhbin.feature.processimage.navigation

import android.net.Uri
import androidx.navigation.*
import androidx.navigation.compose.composable
import com.baothanhbin.core.ui.util.LocationStateHolder
import com.baothanhbin.feature.processimage.ProcessImageRoute

const val PROCESS_IMAGE_ROUTE = "PROCESS_IMAGE_ROUTE"
private const val ARG_IMAGE_URI = "imageUri"
private const val PROCESS_IMAGE_ROUTE_WITH_ARG = "$PROCESS_IMAGE_ROUTE/{$ARG_IMAGE_URI}"

fun NavController.navigateToProcessImage(imageUri: Uri, navOptions: NavOptions? = null) {
    val encoded = Uri.encode(imageUri.toString())
    navigate("$PROCESS_IMAGE_ROUTE/$encoded", navOptions)
}

fun NavGraphBuilder.processImageScreen(
    navController: NavHostController,
    locationStateHolder: LocationStateHolder
) {
    composable(
        route = PROCESS_IMAGE_ROUTE_WITH_ARG,
        arguments = listOf(navArgument(ARG_IMAGE_URI) { type = NavType.StringType })
    ) {
        val uriString = it.arguments?.getString(ARG_IMAGE_URI)
        val uri = uriString?.let { s -> Uri.parse(Uri.decode(s)) }
        ProcessImageRoute(
            navController = navController,
            imageUri = uri,
            locationStateHolder = locationStateHolder
        )
    }
}


