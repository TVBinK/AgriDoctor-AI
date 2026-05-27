package com.baothanhbin.feature.login.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.baothanhbin.core.model.OtpVerificationPurpose
import com.baothanhbin.feature.login.LoginRoute

const val LOGIN_ROUTE = "login"

fun NavController.navigateToLogin() {
    navigate(LOGIN_ROUTE)
}

fun NavGraphBuilder.loginScreen(
    onNavigateToSignup: () -> Unit,
    onNavigateToPin: (String, OtpVerificationPurpose) -> Unit,
    onNavigateToForgotPassword: () -> Unit,
) {
    composable(route = LOGIN_ROUTE) {
        LoginRoute(
            onNavigateToSignup = onNavigateToSignup,
            onNavigateToPin = onNavigateToPin,
            onNavigateToForgotPassword = onNavigateToForgotPassword
        )
    }
}
