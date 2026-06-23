package com.baothanhbin.feature.camera.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.baothanhbin.feature.camera.CameraRoute
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.baothanhbin.core.ui.navigation.slideInFromBottomHalf
import com.baothanhbin.core.ui.navigation.slideOutToBottomHalf

const val CAMERA_ROUTE = "CAMERA_ROUTE"
const val CAMERA_MODE_ARG = "modeIndex"

fun NavController.navigateToCamera(modeIndex: Int = 0, navOptions: NavOptions? = null) {
    navigate(
        route = "$CAMERA_ROUTE/$modeIndex",
        navOptions = navOptions
    )
}

fun NavGraphBuilder.cameraScreen(
    navController: NavHostController
) {
    composable(
        route = "$CAMERA_ROUTE/{$CAMERA_MODE_ARG}",
        arguments = listOf(
            navArgument(CAMERA_MODE_ARG) {
                type = NavType.IntType
                defaultValue = 0
            }
        ),
        enterTransition = { slideInFromBottomHalf() },
        popExitTransition = { slideOutToBottomHalf() }
    ) { backStackEntry ->
        val modeIndex = backStackEntry.arguments?.getInt(CAMERA_MODE_ARG) ?: 0
        CameraRoute(
            navController = navController,
            initialModeIndex = modeIndex
        )
    }
}
