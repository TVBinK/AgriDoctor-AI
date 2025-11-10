package com.baothanhbin.agridoctorai.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import com.baothanhbin.feature.camera.navigation.cameraScreen
import com.baothanhbin.feature.chatbot.navigation.chatbotScreen
import com.baothanhbin.feature.diagnose.navigation.diagnoseScreen
import com.baothanhbin.feature.home.HomeScreen
import com.baothanhbin.feature.home.navigation.HOME_ROUTE
import com.baothanhbin.feature.home.navigation.homeScreen
import com.baothanhbin.feature.myplants.navigation.myplantScreen
import com.baothanhbin.feature.processimage.navigation.processImageScreen
import com.baothanhbin.feature.diagnoseresult.navigation.diagnoseResultScreen
import com.baothanhbin.feature.diagnosefailed.navigation.diagnoseFailedScreen
import com.baothanhbin.feature.lightmeter.navigation.lightMeterScreen

@Composable
fun MainNavHost(
    modifier: Modifier = Modifier,
    appState: AppState
) {
    val navController = appState.navController

    NavHost(
        modifier = modifier,
        startDestination = HOME_ROUTE,
        navController = navController
    ) {
        homeScreen(
            navController = navController,
            onNavigateToChatbot = {
                appState.navigateToTopLevelDestination(com.baothanhbin.agridoctorai.navigation.TopLevelDestination.CHATBOT)
            }
        )
        diagnoseScreen(navController = navController)
        myplantScreen()
        chatbotScreen(navController = navController)
        cameraScreen(navController = navController)
        processImageScreen(navController = navController)
        diagnoseResultScreen(navController = navController)
        diagnoseFailedScreen(navController = navController)
        lightMeterScreen(navController = navController)
    }
}