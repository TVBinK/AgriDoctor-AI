package com.baothanhbin.feature.camera.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.baothanhbin.feature.camera.CameraRoute

const val CAMERA_ROUTE = "CAMERA_ROUTE"

fun NavController.navigateToCamera(navOptions: NavOptions? = null) {
    navigate(
        route = CAMERA_ROUTE,
        navOptions = navOptions
    )
}

fun NavGraphBuilder.cameraScreen(
) {
    composable(route = CAMERA_ROUTE) {
        CameraRoute()
    }
}