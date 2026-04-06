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
import androidx.core.content.ContextCompat


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
                
                when (selectedTab) {
                    0 -> MyPlantsContent(
                        plants = myPlants,
                        onSetReminderClick = { plant ->
                            showSetReminderDialog = plant
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
                        plantName = showSetReminderDialog!!.plantName,
                        onDismissRequest = { showSetReminderDialog = null },
                        onSetReminder = { timestamp ->
                            viewModel.scheduleWateringReminder(context, showSetReminderDialog!!.plantName, timestamp)
                            showSetReminderDialog = null
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
            AddPlantDialog(
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

@Composable
fun MyPlantsTopBar(
    currentAddress: String? = null,
    onLocationClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp)
            .padding(top = 25.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .height(64.dp)
                .weight(1f)
                .clickable(
                    onClick = onLocationClick,
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                )
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_location),
                contentDescription = null,
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(50.dp)
            )
            Spacer(modifier = Modifier.width(15.dp))
            Text(
                text = currentAddress ?: stringResource(R.string.allow_location_tracking),
                style = MaterialTheme.typography.Body1,
                maxLines = 2
            )
        }
        Box(modifier = Modifier.clickable { }) {
            IconButton(onClick = { }, modifier = Modifier.size(40.dp)) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = stringResource(R.string.settings),
                    tint = com.baothanhbin.core.theme.GreenSurface
                )
            }
        }
    }
}

@Composable
private fun MyPlantsContent(
    plants: List<PlantEntity>,
    onSetReminderClick: (PlantEntity) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(R.string.my_plants_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        if (plants.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.no_plant),
                    color = Subtitle
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(plants) { plant ->
                    PlantItemCard(
                        plant = plant,
                        onSetReminderClick = { onSetReminderClick(plant) }
                    )
                }
            }
        }
    }
}

@Composable
private fun PlantItemCard(
    plant: PlantEntity,
    onSetReminderClick: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Plant Image
            if (plant.imageUri != null) {
                AsyncImage(
                    model = File(plant.imageUri),
                    contentDescription = plant.plantName,
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.img_lavender), // Default avatar
                    contentDescription = plant.plantName,
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Plant Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = plant.plantName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = plant.location ?: "Chưa có vị trí",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Subtitle,
                    fontSize = 14.sp
                )
            }
            
            // Menu Button
            Box {
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = stringResource(R.string.more_options),
                        tint = Subtitle
                    )
                }
                
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.edit)) },
                        onClick = {
                            showMenu = false
                            // Handle edit
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.delete)) },
                        onClick = {
                            showMenu = false
                            // Handle delete
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.set_reminder)) },
                        onClick = {
                            showMenu = false
                            onSetReminderClick()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ReminderContent(
    reminders: List<ReminderEntity>,
    onAddReminderClick: () -> Unit,
    onToggleReminderStatus: (Long, Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(White)
    ) {
        // Top section: Add Reminder Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.End
        ) {
            Surface(
                color = Color(0xFFE8F5E9), // Light green background
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.clickable { onAddReminderClick() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(R.string.add_reminder),
                        tint = GreenSurface,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.add_reminder),
                        style = MaterialTheme.typography.bodyMedium,
                        color = GreenSurface,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Calendar Strip
        val todayStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().time) }
        var selectedDateStr by remember { mutableStateOf(todayStr) }

        val days = remember {
            val list = mutableListOf<Pair<String, Pair<Int, String>>>()
            val tempCal = Calendar.getInstance()
            tempCal.add(Calendar.DAY_OF_MONTH, -2) // Start from 2 days ago
            for (i in 0..30) { // Generate next 30 days
                val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(tempCal.time)
                val date = tempCal.get(Calendar.DAY_OF_MONTH)
                val dayOfWeek = SimpleDateFormat("EEE", Locale.getDefault()).format(tempCal.time)
                list.add(dateStr to Pair(date, dayOfWeek))
                tempCal.add(Calendar.DAY_OF_MONTH, 1)
            }
            list
        }
        
        val displayYear = remember(selectedDateStr) {
            try {
                val time = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(selectedDateStr)
                SimpleDateFormat("yyyy", Locale.getDefault()).format(time!!)
            } catch (e: Exception) { "2025" }
        }
        
        val displayMonth = remember(selectedDateStr) {
            try {
                val time = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(selectedDateStr)
                SimpleDateFormat("MMM", Locale.getDefault()).format(time!!)
            } catch (e: Exception) { "Jun" }
        }

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp)
                ) {
                    Text(displayYear, color = Subtitle.copy(alpha = 0.7f), fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp)) // Giống với khoảng cách ở cột ngày
                    Text(displayMonth, color = Subtitle.copy(alpha = 0.7f), fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(14.dp)) // Bù khoảng trống 14dp phía cuối cho bằng với Box dấu chấm
                }
            }

            items(days) { (dateStr, dateInfo) ->
                val (date, dayOfWeek) = dateInfo
                val isSelected = dateStr == selectedDateStr
                Column(
                    modifier = Modifier
                        .background(
                            color = if (isSelected) GreenSurface else Color.Transparent,
                            shape = RoundedCornerShape(24.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 12.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { selectedDateStr = dateStr },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = String.format(Locale.getDefault(), "%02d", date),
                        color = if (isSelected) White else Subtitle.copy(alpha = 0.8f),
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 15.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = dayOfWeek,
                        color = if (isSelected) Color.White else Subtitle.copy(alpha = 0.8f),
                        fontSize = 13.sp
                    )
                    if (isSelected) {
                        Spacer(Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(Color.White, CircleShape)
                        )
                    } else {
                        Spacer(Modifier.height(14.dp)) // To keep alignment consistent
                    }
                }
            }
        }

        val tasksForSelectedDate = reminders.filter { task ->
            try {
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(task.targetTimestamp) == selectedDateStr
            } catch (e: Exception) { false }
        }

        if (tasksForSelectedDate.isEmpty()) {
            Spacer(modifier = Modifier.weight(0.8f))

            // Empty state
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_selected_plant),
                    contentDescription = null,
                    tint = Subtitle.copy(alpha = 0.4f),
                    modifier = Modifier.size(100.dp)
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = "You have no care tasks today",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Subtitle.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Normal
                )
            }

            Spacer(modifier = Modifier.weight(1f))
        } else {
            // Task list
            androidx.compose.foundation.lazy.LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(tasksForSelectedDate) { task ->
                    val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(task.targetTimestamp)
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable {
                            onToggleReminderStatus(task.id, !task.isCompleted)
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (task.isCompleted) Color(0xFFECEFF1) else Color(0xFFF1F8F1)
                        ),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(if (task.isCompleted) Subtitle.copy(alpha = 0.3f) else Color.White, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_water),
                                    contentDescription = null,
                                    tint = if (task.isCompleted) Color.White else GreenSurface,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = task.plantName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = if (task.isCompleted) Subtitle else GreenSurface,
                                    textDecoration = if (task.isCompleted) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = if (task.isCompleted) "Đã hoàn thành lúc $timeStr" else "Đang chờ lúc $timeStr",
                                    fontSize = 14.sp,
                                    color = Subtitle,
                                    fontWeight = if (task.isCompleted) FontWeight.Normal else FontWeight.Medium
                                )
                            }
                            Spacer(Modifier.width(16.dp))
                            Checkbox(
                                checked = task.isCompleted,
                                onCheckedChange = { isChecked ->
                                    onToggleReminderStatus(task.id, isChecked)
                                },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = GreenSurface
                                )
                            )
                        }
                    }
                }
            }
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

@Composable
fun AddPlantDialog(
    onDismissRequest: () -> Unit,
    onAddPlant: (name: String, imageUri: String?) -> Unit
) {
    val context = LocalContext.current
    var plantName by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<String?>(null) }
    var bitmapState by remember { mutableStateOf<Bitmap?>(null) }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            bitmapState = bitmap
            val tempFile = File(context.cacheDir, "plant_${UUID.randomUUID()}.jpg")
            val outputStream = FileOutputStream(tempFile)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            outputStream.flush()
            outputStream.close()
            imageUri = tempFile.absolutePath
        }
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(stringResource(R.string.add_plant)) },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (bitmapState != null) {
                    Image(
                        bitmap = bitmapState!!.asImageBitmap(),
                        contentDescription = "Avatar",
                        modifier = Modifier
                            .size(120.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { takePictureLauncher.launch(null) },
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .background(Color(0xFFE8F5E9), RoundedCornerShape(12.dp))
                            .clickable { takePictureLauncher.launch(null) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Chụp ảnh\ncây của bạn", textAlign = androidx.compose.ui.text.style.TextAlign.Center, color = GreenSurface)
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = plantName,
                    onValueChange = { plantName = it },
                    label = { Text("Tên cây") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GreenSurface,
                        focusedLabelColor = GreenSurface
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onAddPlant(plantName, imageUri) },
                enabled = plantName.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = GreenSurface)
            ) {
                Text(stringResource(R.string.add_plant))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismissRequest,
                colors = ButtonDefaults.textButtonColors(contentColor = Subtitle)
            ) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
fun SetReminderDialog(
    plantName: String,
    onDismissRequest: () -> Unit,
    onSetReminder: (Long) -> Unit
) {
    val context = LocalContext.current
    var selectedTimestamp by remember { mutableStateOf<Long?>(null) }
    var dateTimeText by remember { mutableStateOf("") }
    
    val calendar = Calendar.getInstance()
    
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            TimePickerDialog(
                context,
                { _, hourOfDay, minute ->
                    val cal = Calendar.getInstance()
                    cal.set(year, month, dayOfMonth, hourOfDay, minute, 0)
                    selectedTimestamp = cal.timeInMillis
                    dateTimeText = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(cal.time)
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            ).show()
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )
    
    // Ensure the user cannot select past dates
    datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Hẹn giờ tưới nước") },
        text = {
            Column {
                Text("Cài đặt nhắc nhở tưới nước cho $plantName:", color = Subtitle)
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = dateTimeText,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Ngày giờ đặt hẹn") },
                    placeholder = { Text("Chọn thời gian...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { datePickerDialog.show() },
                    enabled = false,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = GreenSurface,
                        disabledLabelColor = GreenSurface,
                        disabledPlaceholderColor = Subtitle
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    selectedTimestamp?.let { onSetReminder(it) }
                },
                enabled = selectedTimestamp != null,
                colors = ButtonDefaults.buttonColors(containerColor = GreenSurface)
            ) {
                Text("Cài đặt")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismissRequest,
                colors = ButtonDefaults.textButtonColors(contentColor = Subtitle)
            ) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
