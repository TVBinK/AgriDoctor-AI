package com.baothanhbin.feature.home

import android.Manifest
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import com.baothanhbin.agridoctorai.resources.R
import com.baothanhbin.core.theme.Blue1
import com.baothanhbin.core.theme.BlueDefault
import com.baothanhbin.core.theme.Body1
import com.baothanhbin.core.theme.Body4
import com.baothanhbin.core.theme.Button1
import com.baothanhbin.core.theme.GreenSurface
import com.baothanhbin.core.theme.Label2
import com.baothanhbin.core.theme.Label4
import com.baothanhbin.core.theme.Label5
import com.baothanhbin.core.theme.Subtitle
import com.baothanhbin.core.theme.TitleLarge3
import com.baothanhbin.core.theme.White
import com.baothanhbin.core.theme.Yellow1
import com.baothanhbin.core.database.model.PlantEntity
import com.baothanhbin.core.database.model.ReminderEntity
import com.baothanhbin.core.database.model.displayName
import com.baothanhbin.core.database.model.imageModel
import com.baothanhbin.core.ui.dialog.LocationDialog
import com.baothanhbin.core.ui.util.LocationStateHolder
import com.baothanhbin.feature.camera.navigation.navigateToCamera
import com.baothanhbin.feature.myplants.navigation.navigateToMyplants
import com.baothanhbin.feature.chatbot.navigation.navigateToChatbot
import com.baothanhbin.feature.lightmeter.navigation.navigateToLightMeter
import com.baothanhbin.feature.settings.navigation.navigateToSettings
import kotlinx.coroutines.launch
import com.baothanhbin.feature.home.component.*

@Composable
fun HomeRoute(
    navController: NavController? = null,
    locationStateHolder: LocationStateHolder,
    onNavigateToChatbot: (() -> Unit)? = null
) {
    HomeScreen(
        navController = navController,
        locationStateHolder = locationStateHolder,
        onNavigateToChatbot = onNavigateToChatbot
    )
}

@Composable   
fun HomeScreen(
    navController: NavController? = null,
    locationStateHolder: LocationStateHolder,
    onNavigateToChatbot: (() -> Unit)? = null,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val showLocationDialog by viewModel.showLocationDialog.collectAsState()
    val reminders by viewModel.reminders.collectAsState()
    val plants by viewModel.myPlants.collectAsState()

    // Khởi tạo location khi screen start
    LaunchedEffect(Unit) {
        viewModel.initializeLocation(context, locationStateHolder)
    }
    
    // Launcher để request location permission
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            viewModel.onPermissionGranted(context, locationStateHolder)
        } else {
            viewModel.onPermissionDenied()
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(White)
    ) {
        Image(
            painter = painterResource(id = R.drawable.bg_home),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            contentScale = ContentScale.FillWidth
        )

        // Column với padding top để không overlap với TopBar
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 140.dp, bottom = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            QuickActionsSection(navController = navController)
            Spacer(modifier = Modifier.height(20.dp))
            val todayDateStr = remember { java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date()) }
            val todaysReminders = reminders.filter { reminder ->
                val reminderDateStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date(reminder.targetTimestamp))
                !reminder.isCompleted && reminderDateStr == todayDateStr
            }

            TodaysCareSection(
                reminders = todaysReminders,
                plants = plants,
                onToggleReminder = { id, isCompleted -> viewModel.toggleReminderStatus(id, isCompleted) },
                onAddTaskClick = {
                    navController?.navigateToMyplants(
                        androidx.navigation.navOptions {
                            popUpTo(com.baothanhbin.feature.home.navigation.HOME_ROUTE) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    )
                },
                onViewAllClick = { 
                    navController?.navigateToMyplants(
                        androidx.navigation.navOptions {
                            popUpTo(com.baothanhbin.feature.home.navigation.HOME_ROUTE) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    )
                }
            )
            Spacer(modifier = Modifier.height(20.dp))
            MeasurementToolsSection(
                navController = navController,
                onNavigateToChatbot = onNavigateToChatbot
            )
            Spacer(modifier = Modifier.height(80.dp))
        }
        
        // TopBar - đặt SAU Column để đảm bảo nó nằm trên cùng và có thể nhận touch events
        // Sử dụng Box với height để cover vùng TopBar và intercept touch events
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .align(Alignment.TopStart)
        ) {
            HomeTopBar(
                currentAddress = locationStateHolder.currentAddress,
                onLocationClick = { 
                    viewModel.checkAndGetLocation(context, locationStateHolder)
                },
                onSettingsClick = {
                    navController?.navigateToSettings()
                }
            )
        }
        
        // Hiển thị LocationDialog
        if (showLocationDialog) {
            LocationDialog(
                onAllowClick = {
                    locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                },
                onCancelClick = {
                    viewModel.hideLocationDialog()
                }
            )
        }
    }
}
