package com.baothanhbin.feature.verificationotp.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.baothanhbin.core.model.OtpVerificationPurpose
import com.baothanhbin.feature.verificationotp.VerifycationOTPScreen

const val VERIFICATION_OTP_ROUTE = "verification_otp_route"
const val EMAIL_ARG = "email"
const val PURPOSE_ARG = "purpose"

fun NavController.navigateToVerificationOTP(
    email: String,
    purpose: OtpVerificationPurpose
) {
    navigate("$VERIFICATION_OTP_ROUTE/$email?$PURPOSE_ARG=${purpose.name}")
}

fun NavGraphBuilder.verificationOTPScreen(
    onBackClick: () -> Unit,
    onVerifySuccess: () -> Unit
) {
    composable(
        route = "$VERIFICATION_OTP_ROUTE/{$EMAIL_ARG}?$PURPOSE_ARG={$PURPOSE_ARG}",
        arguments = listOf(
            navArgument(EMAIL_ARG) { type = NavType.StringType },
            navArgument(PURPOSE_ARG) {
                type = NavType.StringType
                defaultValue = OtpVerificationPurpose.LOGIN.name
            }
        )
    ) {
        VerifycationOTPScreen(
            onBackClick = onBackClick,
            onVerifySuccess = onVerifySuccess
        )
    }
}
