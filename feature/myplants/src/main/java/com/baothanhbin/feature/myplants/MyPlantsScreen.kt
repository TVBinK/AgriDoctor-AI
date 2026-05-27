package com.baothanhbin.feature.myplants

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.baothanhbin.agridoctorai.resources.R
import com.baothanhbin.core.database.model.PlantEntity
import com.baothanhbin.core.theme.GreenSurface
import com.baothanhbin.core.theme.Subtitle
import com.baothanhbin.core.ui.dialog.LocationDialog
import com.baothanhbin.feature.myplants.components.AddPlantBottomSheet
import com.baothanhbin.feature.myplants.components.EditPlantBottomSheet
import com.baothanhbin.feature.myplants.components.MyPlantsContent
import com.baothanhbin.feature.myplants.components.MyPlantsTopBar
import com.baothanhbin.feature.myplants.components.ReminderContent
import com.baothanhbin.feature.myplants.components.SetReminderDialog
import com.baothanhbin.feature.settings.navigation.navigateToSettings

@Composable
fun MyplantRoute(
    navController: NavHostController? = null,
    locationStateHolder: com.baothanhbin.core.ui.util.LocationStateHolder
) {
    MyplantScreen(
        navController = navController,
        locationStateHolder = locationStateHolder
    )
}

@Composable
fun MyplantScreen(
    navController: NavHostController? = null,
    locationStateHolder: com.baothanhbin.core.ui.util.LocationStateHolder,
    viewModel: MyPlantsViewModel = hiltViewModel()
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val context = LocalContext.current
    val showLocationDialog by viewModel.showLocationDialog.collectAsState()
    
    // Kiểm tra permission khi khởi động - chỉ load nếu chưa có address
    LaunchedEffect(Unit) {
        if (viewModel.shouldLoadLocationOnStart(context, locationStateHolder)) {
            viewModel.getCurrentLocation(context, locationStateHolder)
        }
    }

    LaunchedEffect(viewModel.locationError, context) {
        viewModel.locationError.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }
    
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            viewModel.onPermissionGranted(context, locationStateHolder)
        } else {
            viewModel.onPermissionDenied()
        }
    }
    
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { /* Do nothing here since we just want to ensure it is requested */ }
    
    val tabs = listOf(
        stringResource(R.string.my_plants),
        stringResource(R.string.reminder)
    )
    
    val myPlants by viewModel.myPlants.collectAsState()
    val reminders by viewModel.reminders.collectAsState()
    var showAddPlantDialog by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(White)
    ) {
        Image(
            painter = painterResource(id = R.drawable.bg_myplants),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            contentScale = ContentScale.FillWidth
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 140.dp)
        ) {
            // Tab Row
            TabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier.fillMaxWidth(),
                containerColor = Color.White,
                contentColor = GreenSurface,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = GreenSurface,
                        height = 3.dp
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == index) GreenSurface else Subtitle
                            )
                        }
                    )
                }
            }

            // Content
            Box(modifier = Modifier.fillMaxSize()) {
                var showSetReminderDialog by remember { mutableStateOf<PlantEntity?>(null) }
                var plantToEdit by remember { mutableStateOf<PlantEntity?>(null) }
                
                when (selectedTab) {
                    0 -> MyPlantsContent(
                        plants = myPlants,
                        onSetReminderClick = { plant ->
                            showSetReminderDialog = plant
                        },
                        onEditClick = { plant ->
                            plantToEdit = plant
                        },
                        onDeleteClick = { plant ->
                            viewModel.deletePlant(plant)
                        }
                    )
                    1 -> ReminderContent(
                        reminders = reminders,
                        plants = myPlants,
                        onAddReminderClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }
                            }
                            showSetReminderDialog = PlantEntity(
                                plantName = context.getString(R.string.all_plants),
                                imageUri = null,
                                location = null
                            )
                        },
                        onToggleReminderStatus = { id, isCompleted ->
                            viewModel.toggleReminderStatus(id, isCompleted)
                        }
                    )
                }
                
                if (showSetReminderDialog != null) {
                    SetReminderDialog(
                        initialPlantName = showSetReminderDialog!!.plantName,
                        availablePlants = myPlants,
                        onDismissRequest = { showSetReminderDialog = null },
                        onSetReminder = { plant, actionName, timestamp ->
                            viewModel.scheduleCareReminder(context, plant, actionName, timestamp)
                            showSetReminderDialog = null
                        }
                    )
                }

                if (plantToEdit != null) {
                    EditPlantBottomSheet(
                        plant = plantToEdit!!,
                        onDismissRequest = { plantToEdit = null },
                        onSave = { name, imageUri ->
                            // TODO: Add viewmodel logic to update plant
                            plantToEdit = null
                        }
                    )
                }
                
                if (selectedTab == 0) {
                    FloatingActionButton(
                        onClick = { showAddPlantDialog = true },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp),
                        containerColor = GreenSurface,
                        contentColor = Color.White
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = stringResource(R.string.add_plant)
                        )
                    }
                }
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .align(Alignment.TopStart)
        ) {
            MyPlantsTopBar(
                currentAddress = locationStateHolder.currentAddress,
                onLocationClick = {
                    viewModel.checkAndGetLocation(context, locationStateHolder)
                },
                onSettingsClick = {
                    navController?.navigateToSettings()
                }
            )
        }
        
        if (showAddPlantDialog) {
            AddPlantBottomSheet(
                onDismissRequest = { showAddPlantDialog = false },
                onAddPlant = { name, imageUri -> 
                    viewModel.addPlant(name, imageUri, locationStateHolder.currentAddress)
                    showAddPlantDialog = false
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


@Preview(showBackground = true)
@Composable
private fun MyPlantsScreenPreview() {
    MyplantScreen(
        locationStateHolder = com.baothanhbin.core.ui.util.LocationStateHolder()
    )
}
