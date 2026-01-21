package com.baothanhbin.agridoctorai

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import com.baothanhbin.agridoctorai.navigation.rememberAppState
import com.baothanhbin.agridoctorai.ui.MainApp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val isLoggedIn by mainViewModel.isLoggedIn.collectAsStateWithLifecycle()
            val appState = rememberAppState()

            LaunchedEffect(isLoggedIn) {
                if (!isLoggedIn) {
                    appState.navController.navigate(com.baothanhbin.feature.login.navigation.LOGIN_ROUTE) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }

            MainApp(
                modifier = Modifier.fillMaxSize(),
                appState = appState,
                startDestination = if (isLoggedIn) com.baothanhbin.feature.home.navigation.HOME_ROUTE else com.baothanhbin.feature.login.navigation.LOGIN_ROUTE
            )
        }
    }
}