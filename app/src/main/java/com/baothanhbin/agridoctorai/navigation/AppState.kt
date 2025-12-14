package com.baothanhbin.agridoctorai.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import com.baothanhbin.core.ui.util.LocationStateHolder
import com.baothanhbin.core.ui.util.rememberLocationStateHolder
import com.baothanhbin.feature.chatbot.navigation.CHATBOT_ROUTE
import com.baothanhbin.feature.chatbot.navigation.navigateToChatbot
import com.baothanhbin.feature.diagnose.navigation.DIAGNOSE_ROUTE
import com.baothanhbin.feature.diagnose.navigation.navigateToDiagnose
import com.baothanhbin.feature.home.navigation.HOME_ROUTE
import com.baothanhbin.feature.home.navigation.navigateToHome
import com.baothanhbin.feature.myplants.navigation.MY_PLANTS_ROUTE
import com.baothanhbin.feature.myplants.navigation.navigateToMyplants

@Composable
fun rememberAppState(): AppState {
    val navController = rememberNavController()
    val locationStateHolder = rememberLocationStateHolder()

    return AppState(navController, locationStateHolder)
}

class AppState(
    val navController: NavHostController,
    val locationStateHolder: LocationStateHolder
) {
    val topLevelDestinations: List<TopLevelDestination> = TopLevelDestination.entries
    val currentTopLevelDestination: TopLevelDestination?
        @Composable get() {
            val currentDestination = navController.currentBackStackEntryAsState().value?.destination
            return when (currentDestination?.route) {
                HOME_ROUTE -> TopLevelDestination.HOME
                DIAGNOSE_ROUTE -> TopLevelDestination.DIAGNOSE
                MY_PLANTS_ROUTE -> TopLevelDestination.MY_PLANTS
                CHATBOT_ROUTE -> TopLevelDestination.CHATBOT
                else -> null
            }
        }

    fun navigateToTopLevelDestination(destination: TopLevelDestination) {
        val topLevelNavOptions = navOptions {
            popUpTo(HOME_ROUTE) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }

        when (destination) {
            TopLevelDestination.HOME -> navController.navigateToHome(topLevelNavOptions)
            TopLevelDestination.DIAGNOSE -> navController.navigateToDiagnose(topLevelNavOptions)
            TopLevelDestination.MY_PLANTS -> navController.navigateToMyplants(topLevelNavOptions)
            TopLevelDestination.CHATBOT -> navController.navigateToChatbot(navOptions = topLevelNavOptions)
        }
    }
}