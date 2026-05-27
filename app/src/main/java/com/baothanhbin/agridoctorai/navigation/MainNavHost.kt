package com.baothanhbin.agridoctorai.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.Spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import androidx.navigation.compose.NavHost
import com.baothanhbin.core.model.OtpVerificationPurpose
import com.baothanhbin.feature.camera.navigation.cameraScreen
import com.baothanhbin.feature.chatbot.navigation.chatbotScreen
import com.baothanhbin.feature.chatbot.navigation.navigateToChatbot
import com.baothanhbin.feature.diagnose.navigation.diagnoseScreen
import com.baothanhbin.feature.forgotpassword.navigation.forgotPasswordScreen
import com.baothanhbin.feature.forgotpassword.navigation.navigateToForgotPassword
import com.baothanhbin.feature.home.navigation.HOME_ROUTE
import com.baothanhbin.feature.home.navigation.homeScreen
import com.baothanhbin.feature.home.navigation.navigateToHome
import com.baothanhbin.feature.login.navigation.LOGIN_ROUTE
import com.baothanhbin.feature.login.navigation.loginScreen
import com.baothanhbin.feature.login.navigation.navigateToLogin
import com.baothanhbin.feature.myplants.navigation.myplantScreen
import com.baothanhbin.feature.myplants.navigation.MY_PLANTS_ROUTE
import com.baothanhbin.feature.processimage.navigation.processImageScreen
import com.baothanhbin.feature.diagnoseresult.navigation.diagnoseResultScreen
import com.baothanhbin.feature.diagnosefailed.navigation.diagnoseFailedScreen
import com.baothanhbin.feature.lightmeter.navigation.lightMeterScreen
import com.baothanhbin.feature.settings.navigation.settingsScreen
import com.baothanhbin.feature.signup.navigation.signupScreen
import com.baothanhbin.feature.signup.navigation.navigateToSignup
import com.baothanhbin.feature.verificationotp.navigation.verificationOTPScreen
import com.baothanhbin.feature.verificationotp.navigation.navigateToVerificationOTP
import com.baothanhbin.feature.chatbot.navigation.CHATBOT_ROUTE_BASE
import com.baothanhbin.feature.chatbot.navigation.CHATBOT_ROUTE
import com.baothanhbin.feature.diagnose.navigation.DIAGNOSE_ROUTE

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
        navController = navController,
        enterTransition = { forwardEnterTransition() },
        exitTransition = { forwardExitTransition() },
        popEnterTransition = { backwardEnterTransition() },
        popExitTransition = { backwardExitTransition() }
    ) {
        loginScreen(
            onNavigateToSignup = {
                navController.navigateToSignup()
            },
            onNavigateToPin = { email: String, purpose: OtpVerificationPurpose ->
                navController.navigateToVerificationOTP(email, purpose)
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
                navController.navigateToVerificationOTP(identifier, OtpVerificationPurpose.SIGNUP)
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
        myplantScreen(
            navController = navController,
            locationStateHolder = appState.locationStateHolder
        )
        chatbotScreen(navController = navController)
        cameraScreen(navController = navController)
        processImageScreen(
            navController = navController,
            locationStateHolder = appState.locationStateHolder,
            onRequireLogin = {
                navController.navigate(LOGIN_ROUTE) {
                    popUpTo(0) { inclusive = true }
                    launchSingleTop = true
                }
            }
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

private const val NAV_FADE_DURATION_MS = 140
private const val NAV_PARALLAX_DIVISOR = 4
private const val TOP_LEVEL_FADE_DURATION_MS = 110
private const val TOP_LEVEL_PARALLAX_DIVISOR = 2

private val topLevelRoutes = setOf(
    HOME_ROUTE,
    DIAGNOSE_ROUTE,
    MY_PLANTS_ROUTE,
    CHATBOT_ROUTE_BASE
)

private val topLevelRouteOrder = listOf(
    HOME_ROUTE,
    DIAGNOSE_ROUTE,
    MY_PLANTS_ROUTE,
    CHATBOT_ROUTE_BASE
)

private fun NavBackStackEntry.baseRoute(): String? {
    return destination.route
        ?.substringBefore('?')
        ?.substringBefore('/')
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.isTopLevelTransition(): Boolean {
    val initialRoute = initialState.baseRoute()
    val targetRoute = targetState.baseRoute()
    return initialRoute in topLevelRoutes && targetRoute in topLevelRoutes
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.topLevelSlideDirection():
    AnimatedContentTransitionScope.SlideDirection {
    val initialIndex = topLevelRouteOrder.indexOf(initialState.baseRoute())
    val targetIndex = topLevelRouteOrder.indexOf(targetState.baseRoute())
    return if (targetIndex >= initialIndex) {
        AnimatedContentTransitionScope.SlideDirection.Left
    } else {
        AnimatedContentTransitionScope.SlideDirection.Right
    }
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.forwardEnterTransition(): EnterTransition {
    if (initialState.destination.route == targetState.destination.route) {
        return EnterTransition.None
    }
    if (isTopLevelTransition()) {
        return slideIntoContainer(
            towards = topLevelSlideDirection(),
            animationSpec = spring(
                stiffness = Spring.StiffnessLow,
                dampingRatio = Spring.DampingRatioNoBouncy
            ),
            initialOffset = { fullOffset -> fullOffset / TOP_LEVEL_PARALLAX_DIVISOR }
        ) + fadeIn(animationSpec = tween(durationMillis = TOP_LEVEL_FADE_DURATION_MS))
    }
    return slideIntoContainer(
        towards = AnimatedContentTransitionScope.SlideDirection.Left,
        animationSpec = spring(
            stiffness = Spring.StiffnessMediumLow,
            dampingRatio = Spring.DampingRatioNoBouncy
        ),
        initialOffset = { fullOffset -> fullOffset / NAV_PARALLAX_DIVISOR }
    ) + fadeIn(animationSpec = tween(durationMillis = NAV_FADE_DURATION_MS))
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.forwardExitTransition(): ExitTransition {
    if (initialState.destination.route == targetState.destination.route) {
        return ExitTransition.None
    }
    if (isTopLevelTransition()) {
        return slideOutOfContainer(
            towards = topLevelSlideDirection(),
            animationSpec = spring(
                stiffness = Spring.StiffnessLow,
                dampingRatio = Spring.DampingRatioNoBouncy
            ),
            targetOffset = { fullOffset -> fullOffset / TOP_LEVEL_PARALLAX_DIVISOR }
        ) + fadeOut(animationSpec = tween(durationMillis = TOP_LEVEL_FADE_DURATION_MS))
    }
    return slideOutOfContainer(
        towards = AnimatedContentTransitionScope.SlideDirection.Left,
        animationSpec = spring(
            stiffness = Spring.StiffnessMediumLow,
            dampingRatio = Spring.DampingRatioNoBouncy
        ),
        targetOffset = { fullOffset -> fullOffset / NAV_PARALLAX_DIVISOR }
    ) + fadeOut(animationSpec = tween(durationMillis = NAV_FADE_DURATION_MS))
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.backwardEnterTransition(): EnterTransition {
    if (initialState.destination.route == targetState.destination.route) {
        return EnterTransition.None
    }
    if (isTopLevelTransition()) {
        return slideIntoContainer(
            towards = topLevelSlideDirection(),
            animationSpec = spring(
                stiffness = Spring.StiffnessLow,
                dampingRatio = Spring.DampingRatioNoBouncy
            ),
            initialOffset = { fullOffset -> fullOffset / TOP_LEVEL_PARALLAX_DIVISOR }
        ) + fadeIn(animationSpec = tween(durationMillis = TOP_LEVEL_FADE_DURATION_MS))
    }
    return slideIntoContainer(
        towards = AnimatedContentTransitionScope.SlideDirection.Right,
        animationSpec = spring(
            stiffness = Spring.StiffnessMediumLow,
            dampingRatio = Spring.DampingRatioNoBouncy
        ),
        initialOffset = { fullOffset -> fullOffset / NAV_PARALLAX_DIVISOR }
    ) + fadeIn(animationSpec = tween(durationMillis = NAV_FADE_DURATION_MS))
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.backwardExitTransition(): ExitTransition {
    if (initialState.destination.route == targetState.destination.route) {
        return ExitTransition.None
    }
    if (isTopLevelTransition()) {
        return slideOutOfContainer(
            towards = topLevelSlideDirection(),
            animationSpec = spring(
                stiffness = Spring.StiffnessLow,
                dampingRatio = Spring.DampingRatioNoBouncy
            ),
            targetOffset = { fullOffset -> fullOffset / TOP_LEVEL_PARALLAX_DIVISOR }
        ) + fadeOut(animationSpec = tween(durationMillis = TOP_LEVEL_FADE_DURATION_MS))
    }
    return slideOutOfContainer(
        towards = AnimatedContentTransitionScope.SlideDirection.Right,
        animationSpec = spring(
            stiffness = Spring.StiffnessMediumLow,
            dampingRatio = Spring.DampingRatioNoBouncy
        ),
        targetOffset = { fullOffset -> fullOffset / NAV_PARALLAX_DIVISOR }
    ) + fadeOut(animationSpec = tween(durationMillis = NAV_FADE_DURATION_MS))
}
