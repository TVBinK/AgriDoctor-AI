package com.baothanhbin.agridoctorai.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.baothanhbin.agridoctorai.navigation.AppState
import com.baothanhbin.agridoctorai.navigation.MainNavHost
import androidx.compose.ui.Modifier

import com.baothanhbin.feature.login.navigation.LOGIN_ROUTE

@Composable
fun MainApp(
    modifier: Modifier = Modifier,
    appState: AppState,
    startDestination: String = LOGIN_ROUTE
) {
    Surface(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.background(Color.White)) {
            Box(
                modifier = Modifier
                    .weight(1f) // chiếm toàn bộ phần trên
                    .fillMaxSize()
            ) {
                MainNavHost(modifier, appState, startDestination)
            }
            if(appState.currentTopLevelDestination !=null){
                MainBottomNavBar(
                    destinations = appState.topLevelDestinations,
                    currentDestination = appState.currentTopLevelDestination,
                    onNavigateToDestination = { appState.navigateToTopLevelDestination(it) },
                    navController = appState.navController
                )
            }
        }
    }
}