package com.baothanhbin.feature.chatbot.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.baothanhbin.feature.chatbot.ChatbotRoute

import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.navArgument

const val CHATBOT_ROUTE_BASE = "CHATBOT_ROUTE"
const val CHATBOT_ROUTE = "$CHATBOT_ROUTE_BASE?initialMessage={initialMessage}"

fun NavController.navigateToChatbot(
    initialMessage: String? = null,
    navOptions: NavOptions? = null
) {
    val route = if (initialMessage != null) {
        "$CHATBOT_ROUTE_BASE?initialMessage=$initialMessage"
    } else {
        CHATBOT_ROUTE_BASE
    }
    navigate(
        route = route,
        navOptions = navOptions
    )
}

fun NavGraphBuilder.chatbotScreen(
    navController: NavHostController? = null
) {
    composable(
        route = CHATBOT_ROUTE,
        arguments = listOf(
            navArgument("initialMessage") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            }
        )
    ) { entry ->
        val initialMessage = entry.arguments?.getString("initialMessage")
        ChatbotRoute(
            navController = navController,
            initialMessage = initialMessage
        )
    }
}