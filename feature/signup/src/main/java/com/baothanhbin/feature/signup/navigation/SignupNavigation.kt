package com.baothanhbin.feature.signup.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.baothanhbin.core.ui.navigation.slideInFromRightHalf
import com.baothanhbin.core.ui.navigation.slideOutToRightHalf
import com.baothanhbin.feature.signup.SignupRoute

const val SIGNUP_ROUTE = "signup"

fun NavController.navigateToSignup() {
    navigate(SIGNUP_ROUTE)
}

fun NavGraphBuilder.signupScreen(
    onNavigateToLogin: () -> Unit,
    onSignupSuccess: (identifier: String) -> Unit,
) {
    composable(
        route = SIGNUP_ROUTE,
        enterTransition = { slideInFromRightHalf() },
        popExitTransition = { slideOutToRightHalf() }
    ) {
        SignupRoute(
            onSignupSuccess = onSignupSuccess, // Use new param name
            onNavigateToLogin = onNavigateToLogin,
        )
    }
}
