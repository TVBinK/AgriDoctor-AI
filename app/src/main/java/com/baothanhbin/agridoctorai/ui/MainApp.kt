package com.baothanhbin.agridoctorai.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.baothanhbin.agridoctorai.navigation.AppState
import com.baothanhbin.agridoctorai.navigation.MainNavHost
import com.baothanhbin.agridoctorai.navigation.TopLevelDestination

import com.baothanhbin.feature.login.navigation.LOGIN_ROUTE

@Composable
fun MainApp(
    modifier: Modifier = Modifier,
    appState: AppState,
    startDestination: String = LOGIN_ROUTE
) {
    val currentDestination = appState.currentTopLevelDestination
    var bottomBarDestination by remember { mutableStateOf<TopLevelDestination?>(null) }

    if (currentDestination != null) {
        bottomBarDestination = currentDestination
    }

    Surface(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.background(Color.White)) {
            Box(
                modifier = Modifier
                    .weight(1f) // chiếm toàn bộ phần trên
                    .fillMaxSize()
            ) {
                MainNavHost(modifier, appState, startDestination)
            }
            AnimatedVisibility(
                visible = currentDestination != null,
                enter = fadeIn(animationSpec = tween(durationMillis = 220)) +
                    slideInVertically(
                        initialOffsetY = { it / 2 },
                        animationSpec = tween(durationMillis = 280)
                    ),
                exit = fadeOut(animationSpec = tween(durationMillis = 180)) +
                    slideOutVertically(
                        targetOffsetY = { it / 2 },
                        animationSpec = tween(durationMillis = 220)
                    )
            ) {
                bottomBarDestination?.let { destination ->
                    MainBottomNavBar(
                        destinations = appState.topLevelDestinations,
                        currentDestination = destination,
                        onNavigateToDestination = { appState.navigateToTopLevelDestination(it) },
                        navController = appState.navController
                    )
                }
            }
        }
    }
}
