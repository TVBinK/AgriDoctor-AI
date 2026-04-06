package com.baothanhbin.feature.verificationotp.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
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
        ),
        exitTransition = {
            if (targetState.destination.route == "HOME_ROUTE") {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(500)
                ) + fadeOut(animationSpec = tween(500))
            } else {
                null
            }
        }
    ) {
        VerifycationOTPScreen(
            onBackClick = onBackClick,
            onVerifySuccess = onVerifySuccess
        )
    }
}
