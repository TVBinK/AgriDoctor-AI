package com.baothanhbin.feature.chatbot.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.baothanhbin.feature.chatbot.ChatbotRoute

const val CHATBOT_ROUTE = "CHATBOT_ROUTE"

fun NavController.navigateToChatbot(navOptions: NavOptions? = null) {
    navigate(
        route = CHATBOT_ROUTE,
        navOptions = navOptions
    )
}

fun NavGraphBuilder.chatbotScreen(
    navController: NavHostController? = null
) {
    composable(route = CHATBOT_ROUTE) {
        ChatbotRoute(navController = navController)
    }
}