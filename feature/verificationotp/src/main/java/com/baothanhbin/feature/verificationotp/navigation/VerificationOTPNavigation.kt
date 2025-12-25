package com.baothanhbin.feature.verificationotp.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.baothanhbin.feature.verificationotp.VerifycationOTPScreen

const val VERIFICATION_OTP_ROUTE = "verification_otp_route"
const val EMAIL_ARG = "email"

fun NavController.navigateToVerificationOTP(email: String) {
    navigate("$VERIFICATION_OTP_ROUTE/$email")
}

fun NavGraphBuilder.verificationOTPScreen(
    onBackClick: () -> Unit,
    onVerifySuccess: () -> Unit
) {
    composable(
        route = "$VERIFICATION_OTP_ROUTE/{$EMAIL_ARG}",
        arguments = listOf(
            navArgument(EMAIL_ARG) { type = NavType.StringType }
        )
    ) {
        VerifycationOTPScreen(
            onBackClick = onBackClick,
            onVerifySuccess = onVerifySuccess
        )
    }
}
