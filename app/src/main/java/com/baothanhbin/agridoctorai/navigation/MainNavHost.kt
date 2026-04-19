package com.baothanhbin.agridoctorai.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import com.baothanhbin.feature.camera.navigation.cameraScreen
import com.baothanhbin.feature.chatbot.navigation.chatbotScreen
import com.baothanhbin.feature.chatbot.navigation.navigateToChatbot
import com.baothanhbin.feature.diagnose.navigation.diagnoseScreen
import com.baothanhbin.feature.forgotpassword.navigation.forgotPasswordScreen
import com.baothanhbin.feature.forgotpassword.navigation.navigateToForgotPassword
import com.baothanhbin.feature.home.HomeScreen
import com.baothanhbin.feature.home.navigation.HOME_ROUTE
import com.baothanhbin.feature.home.navigation.homeScreen
import com.baothanhbin.feature.home.navigation.navigateToHome
import com.baothanhbin.feature.login.navigation.LOGIN_ROUTE
import com.baothanhbin.feature.login.navigation.loginScreen
import com.baothanhbin.feature.login.navigation.navigateToLogin
import com.baothanhbin.feature.myplants.navigation.myplantScreen
import com.baothanhbin.feature.processimage.navigation.processImageScreen
import com.baothanhbin.feature.diagnoseresult.navigation.diagnoseResultScreen
import com.baothanhbin.feature.diagnosefailed.navigation.diagnoseFailedScreen
import com.baothanhbin.feature.lightmeter.navigation.lightMeterScreen
import com.baothanhbin.feature.settings.navigation.settingsScreen
import com.baothanhbin.feature.signup.navigation.SIGNUP_ROUTE
import com.baothanhbin.feature.signup.navigation.signupScreen
import com.baothanhbin.feature.signup.navigation.navigateToSignup
import com.baothanhbin.feature.verificationotp.navigation.verificationOTPScreen
import com.baothanhbin.feature.verificationotp.navigation.navigateToVerificationOTP

@Composable
fun MainNavHost(
    modifier: Modifier = Modifier,
    appState: AppState,
    startDestination: String = LOGIN_ROUTE
) {
    val navController = appState.navController

    NavHost(
        modifier = modifier,
        startDestination = startDestination,
        navController = navController
    ) {
        loginScreen(
            onNavigateToSignup = {
                navController.navigateToSignup()
            },
            onContinueAsGuest = {
                navController.navigateToHome()
            },
            onNavigateToPin = { email: String ->
                navController.navigateToVerificationOTP(email) 
            },
            onNavigateToForgotPassword = {
                navController.navigateToForgotPassword()
            }
        )
        forgotPasswordScreen(
            onBackClick = { navController.navigateUp() },
            onResetSuccess = {
                navController.navigateToLogin()
            }
        )
        verificationOTPScreen(
            onBackClick = { navController.navigateUp() },
            onVerifySuccess = { 
                // Navigate to home after successful OTP verification
                val navOptions = androidx.navigation.navOptions {
                    popUpTo(LOGIN_ROUTE) { 
                        inclusive = true 
                    }
                }
                navController.navigateToHome(navOptions) 
            }
        )
        signupScreen(
            onNavigateToLogin = {
                navController.navigateToLogin()
            },
            onSignupSuccess = { identifier ->
                navController.navigateToVerificationOTP(identifier)
            }
        )
        homeScreen(
            navController = navController,
            locationStateHolder = appState.locationStateHolder,
            onNavigateToChatbot = {
                appState.navigateToTopLevelDestination(com.baothanhbin.agridoctorai.navigation.TopLevelDestination.CHATBOT)
            }
        )
        diagnoseScreen(
            navController = navController,
            locationStateHolder = appState.locationStateHolder
        )
        myplantScreen(locationStateHolder = appState.locationStateHolder)
        chatbotScreen(navController = navController)
        cameraScreen(navController = navController)
        processImageScreen(
            navController = navController,
            locationStateHolder = appState.locationStateHolder
        )


        diagnoseResultScreen(
            navController = navController,
            locationStateHolder = appState.locationStateHolder,
            onNavigateToChatbot = { message ->
                // Navigate to chatbot as top-level destination, popping DiagnoseResultScreen
                val navOptions = androidx.navigation.navOptions {
                    popUpTo(com.baothanhbin.feature.home.navigation.HOME_ROUTE) { 
                        saveState = true 
                        inclusive = false
                    }
                    launchSingleTop = true
                    restoreState = true
                }
                navController.navigateToChatbot(initialMessage = message, navOptions = navOptions)
            }
        )
        diagnoseFailedScreen(navController = navController)
        lightMeterScreen(navController = navController)
        settingsScreen(
            onBackClick = {
                navController.navigateUp()
            }
        )
    }
}
