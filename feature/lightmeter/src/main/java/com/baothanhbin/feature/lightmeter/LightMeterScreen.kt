package com.baothanhbin.feature.lightmeter

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.baothanhbin.agridoctorai.resources.R
import kotlin.math.cos
import kotlin.math.sin
import com.baothanhbin.feature.lightmeter.component.*

@Composable
fun LightMeterRoute(
    navController: NavHostController,
    viewModel: LightMeterViewModel = androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    LightMeterScreen(
        navController = navController,
        uiState = uiState,
        onBindCamera = viewModel::bindCamera,
        onPermissionResult = viewModel::onPermissionResult
    )
}

@Composable
fun LightMeterScreen(
    navController: NavHostController,
    uiState: LightMeterUiState,
    onBindCamera: (LifecycleOwner, PreviewView) -> Unit,
    onPermissionResult: (Boolean) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Camera Preview
        PermissionedCamera(
            permissionGranted = uiState.permissionGranted,
            onBindCamera = onBindCamera,
            onPermissionResult = onPermissionResult,
            modifier = Modifier.fillMaxSize()
        )
        
        // Overlay với thông tin đo
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                 IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color.Black.copy(alpha = 0.5f), shape = CircleShape)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_close),
                        contentDescription = stringResource(R.string.close),
                        tint = Color.White
                    )
                }
                
                 Text(
                    text = stringResource(R.string.light_meter_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                
                // Placeholder để cân đối layout
                Spacer(modifier = Modifier.size(48.dp))
            }
            
            Spacer(modifier = Modifier.height(40.dp))
            
            // Circular Light Meter Display
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                CircularLightMeter(
                    luxValue = uiState.luxValue,
                    brightness = uiState.brightness,
                    modifier = Modifier.size(280.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Information Cards
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                 // Light Level Card
                 InfoCard(
                     title = stringResource(R.string.light_level_label),
                     value = uiState.lightLevel,
                     icon = R.drawable.ic_sun
                 )
                
                // Measurement Values Card
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                     MeasurementCard(
                         title = stringResource(R.string.lux_unit),
                         value = String.format("%.1f", uiState.luxValue),
                         modifier = Modifier.weight(1f)
                     )
                     
                     MeasurementCard(
                         title = stringResource(R.string.ev_unit),
                         value = String.format("%.2f", uiState.evValue),
                         modifier = Modifier.weight(1f)
                     )
                }
                
                // Recommendation Card
                RecommendationCard(
                    recommendation = uiState.recommendation
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
