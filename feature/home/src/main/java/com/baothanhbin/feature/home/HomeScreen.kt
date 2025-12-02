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
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.baothanhbin.core.ui.dialog.LocationDialog
import com.baothanhbin.core.ui.util.LocationStateHolder
import com.baothanhbin.feature.camera.navigation.navigateToCamera
import com.baothanhbin.feature.lightmeter.navigation.navigateToLightMeter
import com.baothanhbin.feature.settings.navigation.navigateToSettings
import kotlinx.coroutines.launch

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
                .verticalScroll(rememberScrollState())
                .padding(top = 140.dp, bottom = 24.dp)
        ) {
            QuickActionsSection(navController = navController)
            Spacer(modifier = Modifier.height(20.dp))
            TodaysCareSection()
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

@Composable
private fun QuickActionsSection(
    navController: NavController? = null
) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        HomeFeatureCard(
            title = stringResource(R.string.identify_plant),
            subtitle = stringResource(R.string.scan_to_identify),
            icon = R.drawable.ic_camera,
            backgroundRes = R.drawable.bg_home_identify,
            modifier = Modifier.weight(1f),
            onClick = {
                // identify_plant is index 1 (diagnose=0, identify_plant=1)
                navController?.navigateToCamera(modeIndex = 1)
            }
        )
        HomeFeatureCard(
            title = stringResource(R.string.diagnose),
            subtitle = stringResource(R.string.check_plant_health),
            icon = R.drawable.ic_selected_diagnose,
            backgroundRes = R.drawable.bg_home_diagnose,
            modifier = Modifier.weight(1f),
            onClick = {
                // diagnose is index 0
                navController?.navigateToCamera(modeIndex = 0)
            }
        )
    }
}

@Composable
private fun TodaysCareSection() {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            stringResource(R.string.todays_care),
            style = MaterialTheme.typography.TitleLarge3
        )
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            ) {
                Text(
                    stringResource(R.string.tasks_count),
                    color = GreenSurface,
                    style = MaterialTheme.typography.TitleLarge3
                )
                Spacer(modifier = Modifier.height(5.dp))
                Text(
                    stringResource(R.string.view_all),
                    color = BlueDefault,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    textDecoration = TextDecoration.Underline
                )
            }
        }
    }
}

@Composable
private fun MeasurementToolsSection(
    navController: NavController? = null,
    onNavigateToChatbot: (() -> Unit)? = null
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(stringResource(R.string.measurement_tools), style = MaterialTheme.typography.TitleLarge3)
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MeasurementToolCard(
                title = stringResource(R.string.water_meter),
                description = stringResource(R.string.optimize_watering),
                icon = R.drawable.ic_water,
                background = Blue1,
                modifier = Modifier.weight(1f),
                onClick = {

                }
            )
            MeasurementToolCard(
                title = stringResource(R.string.light_meter),
                description = stringResource(R.string.measure_light_intensity),
                icon = R.drawable.ic_light,
                background = Yellow1,
                modifier = Modifier.weight(1f),
                onClick = {
                    navController?.navigateToLightMeter()
                }
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        ChatCard(
            title = stringResource(R.string.need_plant_help),
            description = stringResource(R.string.get_instant_advice),
            background = Color.White,
            animatedRaw = R.raw.ic_tinh_linh,
            onChatNowClick = {
                onNavigateToChatbot?.invoke()
            }
        )
    }
}

@Composable
fun HomeTopBar(
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
fun HomeFeatureCard(
    title: String,
    subtitle: String,
    icon: Int,
    backgroundRes: Int? = null,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(110.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            backgroundRes?.let {
                Image(
                    painter = painterResource(id = it),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            ) {
                Icon(
                    painter = painterResource(id = icon),
                    contentDescription = null,
                    tint = Color(0xFF2E7D32),
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.height(5.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, style = MaterialTheme.typography.Body1)
                    Text(subtitle, style = MaterialTheme.typography.Label2, color = Subtitle)
                }
            }
        }
    }
}

@Composable
fun MeasurementToolCard(
    title: String,
    description: String,
    icon: Int,
    background: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .width(190.dp)
            .height(110.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            // ✅ Dòng đầu tiên: icon bên trái, badge bên phải
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Image(
                    painter = painterResource(id = icon),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                if (title == LocalContext.current.getString(R.string.water_meter)) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_caculator_blue),
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_caculator_yellow),
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            // ✅ Tiêu đề
            Text(
                text = title,
                style = MaterialTheme.typography.Body4
            )
            Spacer(modifier = Modifier.height(2.dp))
            // ✅ Mô tả
            Text(
                text = description,
                style = MaterialTheme.typography.Label5,
                color = Subtitle
            )
        }
    }
}

@Composable
fun ChatCard(
    title: String, 
    description: String, 
    background: Color, 
    animatedRaw: Int? = null,
    onChatNowClick: () -> Unit = {}
) {
    val shape = RoundedCornerShape(16.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .shadow(6.dp, shape = shape, clip = false)
            .border(
                width = 2.dp,
                brush = Brush.linearGradient(
                    listOf(
                        Color(0xFF00E676), // green
                        Color(0xFF00B0FF)  // blue
                    )
                ),
                shape = shape
            )
            .clip(shape)
    ) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .padding(1.5.dp),
            shape = shape,
            colors = CardDefaults.cardColors(containerColor = background),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, style = MaterialTheme.typography.Button1)
                    Text(description, style = MaterialTheme.typography.Label4, color = Subtitle)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = onChatNowClick,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4285F4)),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(stringResource(R.string.chat_now), fontSize = 12.sp, color = Color.White)
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                animatedRaw?.let { GifImage(animatedRaw = it) }
            }
        }
    }
}

@Composable
private fun GifImage(animatedRaw: Int) {
    val context = LocalContext.current
    val imageLoader = remember {
        ImageLoader.Builder(context).components {
            if (Build.VERSION.SDK_INT >= 28) {
                add(ImageDecoderDecoder.Factory())
            } else {
                add(GifDecoder.Factory())
            }
        }.build()
    }

    val resourceEntryName = try {
        context.resources.getResourceEntryName(animatedRaw)
    } catch (e: Exception) {
        "ic_tinh_linh"
    }
    val gifUri = Uri.parse("android.resource://${context.packageName}/raw/$resourceEntryName")

    AsyncImage(
        model = ImageRequest.Builder(context).data(gifUri).crossfade(false).allowRgb565(false)
            .build(),
        contentDescription = null,
        modifier = Modifier.size(100.dp),
        contentScale = ContentScale.Fit,
        imageLoader = imageLoader
    )
}

@Preview
@Composable
fun HomeScreenPreview() {
    HomeScreen(
        locationStateHolder = LocationStateHolder()
    )
}
