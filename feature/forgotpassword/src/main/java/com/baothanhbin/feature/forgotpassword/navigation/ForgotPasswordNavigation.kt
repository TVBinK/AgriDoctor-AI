package com.baothanhbin.feature.forgotpassword.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.baothanhbin.feature.forgotpassword.ForgotPasswordRoute

const val FORGOT_PASSWORD_ROUTE = "forgot_password"

fun NavController.navigateToForgotPassword() {
    navigate(FORGOT_PASSWORD_ROUTE)
}

fun NavGraphBuilder.forgotPasswordScreen(
    onBackClick: () -> Unit,
    onResetSuccess: () -> Unit
) {
    composable(route = FORGOT_PASSWORD_ROUTE) {
        ForgotPasswordRoute(
            onBackClick = onBackClick,
            onResetSuccess = onResetSuccess
        )
    }
}
