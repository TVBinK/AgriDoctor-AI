package com.baothanhbin.feature.diagnose

import android.Manifest
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.baothanhbin.agridoctorai.resources.R
import com.baothanhbin.core.database.model.DiagnoseResultEntity
import com.baothanhbin.core.database.model.PlantEntity
import com.baothanhbin.core.database.model.displayName
import com.baothanhbin.core.theme.Body1
import com.baothanhbin.core.theme.GreenSurface
import com.baothanhbin.core.theme.Label4
import com.baothanhbin.core.theme.Subtitle
import com.baothanhbin.core.theme.TitleLarge3
import com.baothanhbin.core.theme.White
import com.baothanhbin.core.ui.dialog.LocationDialog
import com.baothanhbin.feature.camera.navigation.navigateToCamera
import com.baothanhbin.feature.diagnoseresult.navigation.navigateToDiagnoseResult
import com.baothanhbin.feature.settings.navigation.navigateToSettings
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DiagnoseRoute(
    navController: NavHostController? = null,
    locationStateHolder: com.baothanhbin.core.ui.util.LocationStateHolder,
    viewModel: DiagnoseViewModel = hiltViewModel()
) {
    // Handle navigation events
    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { event ->
            when (event) {
                is DiagnoseNavigationEvent.NavigateToResult -> {
                    navController?.navigateToDiagnoseResult(
                        imageUri = event.imageUri,
                        apiType = event.apiType,
                        classifyData = event.classifyData
                    )
                }

                is DiagnoseNavigationEvent.NavigateToCamera -> {
                    // Navigate to Camera with Diagnose mode (index 0)
                    navController?.navigateToCamera(modeIndex = 0)
                }
            }
        }
    }

    DiagnoseScreen(
        navController = navController,
        viewModel = viewModel,
        locationStateHolder = locationStateHolder
    )
}

@Composable
fun DiagnoseScreen(
    navController: NavHostController? = null,
    viewModel: DiagnoseViewModel = hiltViewModel(),
    locationStateHolder: com.baothanhbin.core.ui.util.LocationStateHolder
) {
    val context = LocalContext.current
    val showLocationDialog by viewModel.showLocationDialog.collectAsState()

    LaunchedEffect(viewModel, context) {
        viewModel.uiMessage.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    // Kiểm tra permission khi khởi động - chỉ load nếu chưa có address
    LaunchedEffect(Unit) {
        viewModel.loadHistory()
        if (viewModel.shouldLoadLocationOnStart(context, locationStateHolder)) {
            viewModel.getCurrentLocation(context, locationStateHolder)
        }
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
            painter = painterResource(id = R.drawable.bg_diagnose),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            contentScale = ContentScale.FillWidth
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 140.dp, bottom = 24.dp)
        ) {
            DiagnoseCard(
                onAutoDiagnoseClick = { viewModel.onAutoDiagnoseClick() }
            )
            Spacer(modifier = Modifier.height(16.dp))
            CommonProblemsSection()
            Spacer(modifier = Modifier.height(24.dp))
            HistorySection(
                viewModel = viewModel
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .align(Alignment.TopStart)
        ) {
            DiagnoseTopBar(
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

@Composable
private fun DiagnoseTopBar(
    currentAddress: String? = null,
    onLocationClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {}
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
        Box {
            IconButton(onClick = onSettingsClick, modifier = Modifier.size(40.dp)) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = stringResource(R.string.settings),
                    tint = GreenSurface
                )
            }
        }
    }
}

@Composable
private fun DiagnoseCard(
    onAutoDiagnoseClick: () -> Unit
) {
    val shape = RoundedCornerShape(16.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(240.dp)
    ) {
        // Card body
        Card(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(top = 30.dp),
            shape = shape,
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(40.dp))
                Text(
                    stringResource(R.string.plant_health),
                    style = MaterialTheme.typography.TitleLarge3
                )
                Text(
                    stringResource(R.string.help_plants_get_health),
                    style = MaterialTheme.typography.Label4,
                    color = Subtitle
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onAutoDiagnoseClick,
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_camera),
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        stringResource(R.string.auto_diagnose),
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Floating flower layered above the card (not clipped)
        Image(
            painter = painterResource(id = R.drawable.ic_flower),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .size(110.dp)
                .offset(y = (-18).dp),
            contentScale = ContentScale.Fit
        )
    }
}

private data class Problem(val title: String, val imageRes: Int)

@Composable
private fun CommonProblemsSection() {
    val problems = listOf(
        Problem(stringResource(R.string.botrytis), R.drawable.img_ca_chua),
        Problem(stringResource(R.string.late_blight), R.drawable.img_late_blight),
        Problem(stringResource(R.string.leaf_miner), R.drawable.img_leaf_miner)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(stringResource(R.string.common_problems), style = MaterialTheme.typography.TitleLarge3)
        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = Subtitle)
    }

    Spacer(modifier = Modifier.height(10.dp))

    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(problems) { problem ->
            ProblemCard(problem)
        }
    }
}

@Composable
private fun ProblemCard(problem: Problem) {
    val shape = RoundedCornerShape(12.dp)
    Card(
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.width(120.dp)) {
            Image(
                painter = painterResource(id = problem.imageRes),
                contentDescription = problem.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(shape),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = problem.title,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun HistorySection(
    viewModel: DiagnoseViewModel
) {
    val historyItems by viewModel.historyItems.collectAsStateWithLifecycle()
    val plantHistoryItems by viewModel.plantHistoryItems.collectAsStateWithLifecycle()

    // Tab state
    var selectedTab by remember { mutableStateOf(0) } // 0: Disease, 1: Plant

    if (historyItems.isEmpty() && plantHistoryItems.isEmpty()) {
        return
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.health_check_history),
                style = MaterialTheme.typography.TitleLarge3,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        // Custom Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .background(Color(0xFFF0F0F0), RoundedCornerShape(8.dp))
                .padding(4.dp)
        ) {
            TabButton(
                text = stringResource(R.string.list_disease),
                selected = selectedTab == 0,
                modifier = Modifier.weight(1f),
                onClick = { selectedTab = 0 }
            )
            TabButton(
                text = stringResource(R.string.list_plant),
                selected = selectedTab == 1,
                modifier = Modifier.weight(1f),
                onClick = { selectedTab = 1 }
            )
        }

        when (selectedTab) {
            0 -> {
                // Tab Disease - hiển thị từ diagnose_results
                if (historyItems.isEmpty()) {
                    Text(
                        text = stringResource(R.string.no_disease_history),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Subtitle,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(400.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = historyItems,
                            key = { it.id }
                        ) { item ->
                            SwipeToDeleteContainer(
                                onDelete = {
                                    viewModel.deleteHistoryItem(item)
                                }
                            ) {
                                HistoryItem(
                                    entity = item,
                                    onClick = { viewModel.onHistoryItemClick(item) }
                                )
                            }
                        }
                    }
                }
            }
            1 -> {
                // Tab Plant - hiển thị từ plants
                if (plantHistoryItems.isEmpty()) {
                    Text(
                        text = stringResource(R.string.no_plant_history),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Subtitle,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(400.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = plantHistoryItems,
                            key = { it.id }
                        ) { item ->
                            SwipeToDeleteContainer(
                                onDelete = {
                                    viewModel.deletePlantHistoryItem(item)
                                }
                            ) {
                                PlantHistoryItem(
                                    entity = item,
                                    onClick = { viewModel.onPlantHistoryItemClick(item) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TabButton(
    text: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(36.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(if (selected) Color.White else Color.Transparent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) GreenSurface else Subtitle
        )
    }
}

@Composable
private fun PlantHistoryItem(
    entity: PlantEntity,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    
    Log.d("PlantHistoryItem", "Rendering plant history item")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF5F5F5)),
                contentAlignment = Alignment.Center
            ) {
                entity.imageUri?.let { uriString ->
                    val uri = try {
                        Uri.parse(uriString)
                    } catch (e: Exception) {
                        null
                    }

                    if (uri != null) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(uri)
                                .crossfade(true)
                                .build(),
                            contentDescription = entity.plantName,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop,
                            placeholder = painterResource(id = R.drawable.img_lavender),
                            error = painterResource(id = R.drawable.img_lavender)
                        )
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.img_lavender),
                            contentDescription = entity.plantName,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                } ?: Image(
                    painter = painterResource(id = R.drawable.img_lavender),
                    contentDescription = entity.plantName,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = entity.displayName(),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = GreenSurface,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    entity.confidence?.let { conf ->
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${(conf * 100).toInt()}%",
                            style = MaterialTheme.typography.bodyLarge,
                            color = GreenSurface
                        )
                    }
                }

                entity.scientificName?.let { sciName ->
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = sciName,
                        style = MaterialTheme.typography.bodySmall,
                        color = Subtitle,
                        fontWeight = FontWeight.Normal
                    )
                }

                entity.family?.let { familyName ->
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = familyName,
                        style = MaterialTheme.typography.bodySmall,
                        color = Subtitle
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = formatDate(entity.timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = Subtitle
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Subtitle,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun HistoryItem(
    entity: DiagnoseResultEntity,
    onClick: () -> Unit
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Image - sử dụng imageUri từ entity nếu có
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF5F5F5)),
                contentAlignment = Alignment.Center
            ) {
                entity.imageUri?.let { uriString ->
                    // Parse URI trước khi vào Composable
                    val uri = try {
                        val parsed = Uri.parse(uriString)
                        // Log.d("HistoryItem", "Loading image from URI: $uriString")
                        parsed
                    } catch (e: Exception) {
                        Log.e("HistoryItem", "Error parsing image URI", e)
                        null
                    }

                    if (uri != null) {
                        // Sử dụng ImageRequest.Builder với Context để đảm bảo Coil có thể đọc file local
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(uri)
                                .crossfade(true)
                                .listener(
                                    onError = { _, result ->
                                        Log.e(
                                            "HistoryItem",
                                            "Error loading image from URI",
                                            result.throwable
                                        )
                                    },
                                    onSuccess = { _, _ -> }
                                )
                                .build(),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop,
                            error = painterResource(id = R.drawable.img_ca_chua),
                            placeholder = painterResource(id = R.drawable.img_ca_chua)
                        )
                    } else {
                        // Fallback nếu parse URI lỗi
                        Image(
                            painter = painterResource(id = R.drawable.img_ca_chua),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                } ?: Image(
                    painter = painterResource(id = R.drawable.img_ca_chua),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Spacer(modifier = Modifier.height(4.dp))

                // Disease name
                Text(
                    text = entity.diseaseName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFFF9800) // Orange color như trong hình
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Date
                Text(
                    text = formatDate(entity.timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = Subtitle
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Subtitle,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun SwipeToDeleteContainer(
    onDelete: () -> Unit,
    content: @Composable () -> Unit
) {
    var isDeleteRevealed by remember { mutableStateOf(false) }
    val deleteActionWidth = 96.dp
    val contentOffset by animateDpAsState(
        targetValue = if (isDeleteRevealed) (-deleteActionWidth) else 0.dp,
        label = "diagnose_history_delete_reveal"
    )
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                isDeleteRevealed = true
                false
            } else if (dismissValue == SwipeToDismissBoxValue.StartToEnd) {
                isDeleteRevealed = false
                false
            } else {
                false
            }
        },
        positionalThreshold = { distance -> distance * 0.35f }
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
    ) {
        if (isDeleteRevealed) {
            Row(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color(0xFFD32F2F)),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            isDeleteRevealed = false
                        }
                )
                Box(
                    modifier = Modifier
                        .width(deleteActionWidth)
                        .fillMaxHeight()
                        .clickable(onClick = onDelete),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.delete),
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }

        SwipeToDismissBox(
            state = dismissState,
            enableDismissFromStartToEnd = isDeleteRevealed,
            enableDismissFromEndToStart = !isDeleteRevealed,
            backgroundContent = {},
            modifier = Modifier.offset(x = contentOffset)
        ) {
            content()
        }
    }
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

@Preview
@Composable
fun DiagnosePreview() {
    // Preview không cần ViewModel, chỉ hiển thị UI
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(White)
    ) {
        Image(
            painter = painterResource(id = R.drawable.bg_diagnose),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            contentScale = ContentScale.FillWidth
        )
        DiagnoseTopBar()
        Spacer(modifier = Modifier.height(12.dp))
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 140.dp, bottom = 24.dp)
        ) {
            DiagnoseCard(
                onAutoDiagnoseClick = {}
            )
            Spacer(modifier = Modifier.height(16.dp))
            CommonProblemsSection()
        }
    }
}
