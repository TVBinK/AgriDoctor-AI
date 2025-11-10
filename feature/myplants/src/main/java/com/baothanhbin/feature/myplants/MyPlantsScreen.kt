package com.baothanhbin.feature.myplants

import android.Manifest
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
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

// Theme colors
private val GreenSurface = Color(0xFF4CAF50)
private val Subtitle = Color(0xFF757575)

data class Plant(
    val id: Int,
    val name: String,
    val imageRes: Int,
    val nextAction: String,
    val nextActionDate: String,
    val hasReminder: Boolean = true
)

@Composable
fun MyplantRoute() {
    MyplantScreen()
}

@Composable
fun MyplantScreen() {
    var selectedTab by remember { mutableIntStateOf(0) }
    var showLocationDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    
    // Function để lấy vị trí
    fun getCurrentLocation() {
        try {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            
            // Thử lấy cached location trước
            var location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            
            if (location != null) {
                Log.d("MyPlantsScreen", "Vị trí: Latitude=${location.latitude}, Longitude=${location.longitude}")
            } else {
                // Request location update một lần
                val locationListener = object : LocationListener {
                    override fun onLocationChanged(location: Location) {
                        Log.d("MyPlantsScreen", "Vị trí: Latitude=${location.latitude}, Longitude=${location.longitude}")
                        locationManager.removeUpdates(this)
                    }
                    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                    override fun onProviderEnabled(provider: String) {}
                    override fun onProviderDisabled(provider: String) {}
                }
                
                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        0L,
                        0f,
                        locationListener
                    )
                } else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    locationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        0L,
                        0f,
                        locationListener
                    )
                }
            }
        } catch (e: SecurityException) {
            Log.e("MyPlantsScreen", "Không có quyền truy cập vị trí", e)
        } catch (e: Exception) {
            Log.e("MyPlantsScreen", "Lỗi lấy vị trí", e)
        }
    }
    
    // Launcher để request location permission
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            getCurrentLocation()
        }
        showLocationDialog = false
    }
    
    val tabs = listOf(
        stringResource(R.string.my_plants),
        stringResource(R.string.reminder)
    )
    
    // Sample data
    val plants = listOf(
        Plant(
            id = 1,
            name = "Aspidistra elatior",
            imageRes = R.drawable.img_lavender,
            nextAction = stringResource(R.string.no_reminders),
            nextActionDate = "",
            hasReminder = false
        ),
        Plant(
            id = 2,
            name = "Spiral Aloe",
            imageRes = R.drawable.img_lavender,
            nextAction = stringResource(R.string.next_report_on),
            nextActionDate = "Jun 20"
        ),
        Plant(
            id = 3,
            name = "List item",
            imageRes = R.drawable.img_lavender,
            nextAction = stringResource(R.string.next_water_on),
            nextActionDate = "Jun 20"
        )
    )
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
            onLocationClick = { 
                showLocationDialog = true
            }
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 140.dp, bottom = 24.dp)
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
            when (selectedTab) {
                0 -> MyPlantsContent(plants = plants)
                1 -> ReminderContent()
            }
        }
        
        // Hiển thị LocationDialog
        if (showLocationDialog) {
            LocationDialog(
                onAllowClick = {
                    locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                },
                onCancelClick = {
                    showLocationDialog = false
                }
            )
        }
    }
}

@Composable
fun MyPlantsTopBar(
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
                .clickable(onClick = onLocationClick)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_location),
                contentDescription = null,
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(50.dp)
            )
            Spacer(modifier = Modifier.width(15.dp))
            Text(
                stringResource(R.string.allow_location_tracking),
                style = MaterialTheme.typography.Body1
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
private fun MyPlantsContent(plants: List<Plant>) {
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
        
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(plants) { plant ->
                PlantItemCard(plant = plant)
            }
        }
    }
}

@Composable
private fun PlantItemCard(plant: Plant) {
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
            Image(
                painter = painterResource(id = plant.imageRes),
                contentDescription = plant.name,
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Plant Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = plant.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                if (plant.hasReminder) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = plant.nextAction,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Subtitle,
                            fontSize = 14.sp
                        )
                        if (plant.nextActionDate.isNotEmpty()) {
                            Text(
                                text = " ${plant.nextActionDate}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = GreenSurface,
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp
                            )
                        }
                    }
                } else {
                    Text(
                        text = plant.nextAction,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Subtitle,
                        fontSize = 14.sp
                    )
                }
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
                            // Handle set reminder
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ReminderContent() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_selected_plant),
                contentDescription = null,
                tint = Subtitle.copy(alpha = 0.5f),
                modifier = Modifier.size(80.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = stringResource(R.string.no_reminders_set),
                style = MaterialTheme.typography.bodyLarge,
                color = Subtitle
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = stringResource(R.string.add_reminder_description),
                style = MaterialTheme.typography.bodyMedium,
                color = Subtitle.copy(alpha = 0.7f),
                fontSize = 14.sp
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = { },
                colors = ButtonDefaults.buttonColors(
                    containerColor = GreenSurface
                ),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.padding(horizontal = 32.dp)
            ) {
                Text(
                    text = stringResource(R.string.add_plant),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MyPlantsScreenPreview() {
    MyplantScreen()
}

