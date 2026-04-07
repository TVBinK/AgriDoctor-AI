package com.baothanhbin.feature.myplants

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.baothanhbin.agridoctorai.resources.R
import com.baothanhbin.core.theme.Body1
import com.baothanhbin.core.ui.dialog.LocationDialog
import kotlinx.coroutines.launch
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.icons.filled.Add
import androidx.compose.ui.graphics.asImageBitmap
import android.graphics.Bitmap
import androidx.compose.foundation.lazy.LazyRow
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import com.baothanhbin.core.database.model.PlantEntity
import coil.compose.AsyncImage
import com.baothanhbin.core.theme.GreenSurface
import com.baothanhbin.core.theme.Subtitle
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import com.baothanhbin.core.database.model.ReminderEntity
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.core.content.ContextCompat
import com.baothanhbin.feature.myplants.components.*
@Composable
fun MyplantRoute(
    locationStateHolder: com.baothanhbin.core.ui.util.LocationStateHolder
) {
    MyplantScreen(locationStateHolder = locationStateHolder)
}

@Composable
fun MyplantScreen(
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
        // Top Bar
        MyPlantsTopBar(
            currentAddress = locationStateHolder.currentAddress,
            onLocationClick = { 
                viewModel.checkAndGetLocation(context, locationStateHolder)
            }
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
                        onAddReminderClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }
                            }
                            showSetReminderDialog = PlantEntity(
                                plantName = "Tất cả cây",
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
                        onSetReminder = { plantName, actionName, timestamp ->
                            viewModel.scheduleCareReminder(context, plantName, actionName, timestamp)
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
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Add plant")
                    }
                }
            }
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
