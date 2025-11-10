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

@Composable
fun LightMeterRoute(
    navController: NavHostController,
    viewModel: LightMeterViewModel = hiltViewModel()
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

@Composable
private fun PermissionedCamera(
    permissionGranted: Boolean,
    onBindCamera: (LifecycleOwner, PreviewView) -> Unit,
    onPermissionResult: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        onPermissionResult(isGranted)
    }
    
    LaunchedEffect(Unit) {
        if (!permissionGranted) {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }
    
    if (permissionGranted) {
        CameraPreview(
            onBindCamera = onBindCamera,
            modifier = modifier
        )
    } else {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
             Text(
                 text = stringResource(R.string.camera_permission_required),
                 color = Color.White,
                 textAlign = TextAlign.Center
             )
        }
    }
}

@Composable
private fun CameraPreview(
    onBindCamera: (LifecycleOwner, PreviewView) -> Unit,
    modifier: Modifier = Modifier
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    
    AndroidView(
        modifier = modifier,
        factory = { ctx -> PreviewView(ctx) },
        update = { previewView ->
            onBindCamera(lifecycleOwner, previewView)
        }
    )
}

@Composable
private fun CircularLightMeter(
    luxValue: Float,
    brightness: Float,
    modifier: Modifier = Modifier
) {
    // Animation cho hiệu ứng pulse
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )
    
    // Animation cho giá trị
    val animatedBrightness by animateFloatAsState(
        targetValue = brightness,
        animationSpec = tween(500),
        label = "brightness"
    )
    
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val centerX = canvasWidth / 2
            val centerY = canvasHeight / 2
            val radius = (canvasWidth.coerceAtMost(canvasHeight) / 2) - 40f
            
            // Outer glow circle
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFFFF176).copy(alpha = pulseAlpha),
                        Color.Transparent
                    ),
                    center = Offset(centerX, centerY),
                    radius = radius + 40f
                ),
                center = Offset(centerX, centerY),
                radius = radius + 40f
            )
            
            // Background circle
            drawCircle(
                color = Color.White.copy(alpha = 0.1f),
                center = Offset(centerX, centerY),
                radius = radius,
                style = Stroke(width = 20f)
            )
            
            // Progress arc based on brightness
            val sweepAngle = (animatedBrightness / 255f) * 360f
            val gradient = Brush.sweepGradient(
                colors = listOf(
                    Color(0xFF1E88E5),
                    Color(0xFF26C6DA),
                    Color(0xFF66BB6A),
                    Color(0xFFFFEE58),
                    Color(0xFFFF9800),
                    Color(0xFFF44336)
                ),
                center = Offset(centerX, centerY)
            )
            
            drawArc(
                brush = gradient,
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(centerX - radius, centerY - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = 20f, cap = StrokeCap.Round)
            )
            
            // Tick marks
            for (i in 0..11) {
                val angle = (i * 30 - 90) * Math.PI / 180
                val startRadius = radius - 10f
                val endRadius = radius - 30f
                
                drawLine(
                    color = Color.White.copy(alpha = 0.5f),
                    start = Offset(
                        centerX + (startRadius * cos(angle)).toFloat(),
                        centerY + (startRadius * sin(angle)).toFloat()
                    ),
                    end = Offset(
                        centerX + (endRadius * cos(angle)).toFloat(),
                        centerY + (endRadius * sin(angle)).toFloat()
                    ),
                    strokeWidth = 2f
                )
            }
        }
        
        // Center content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_sun),
                contentDescription = null,
                tint = Color(0xFFFDD835),
                modifier = Modifier.size(48.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = String.format("%.0f", luxValue),
                style = MaterialTheme.typography.displayLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            
             Text(
                 text = stringResource(R.string.lux_unit),
                 style = MaterialTheme.typography.bodyLarge,
                 color = Color.White.copy(alpha = 0.7f)
             )
        }
    }
}

@Composable
private fun InfoCard(
    title: String,
    value: String,
    icon: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.15f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = null,
                tint = Color(0xFFFDD835),
                modifier = Modifier.size(32.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.7f)
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun MeasurementCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.15f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp
            )
        }
    }
}

@Composable
private fun RecommendationCard(
    recommendation: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF4CAF50).copy(alpha = 0.9f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_lightbulb),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
             Column(modifier = Modifier.weight(1f)) {
                 Text(
                     text = stringResource(R.string.recommendation_label),
                     style = MaterialTheme.typography.bodyMedium,
                     color = Color.White,
                     fontWeight = FontWeight.Bold
                 )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = recommendation,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.95f)
                )
            }
        }
    }
}
@Preview
@Composable
private fun LightMeterScreenPreview() {
    val context = LocalContext.current
    LightMeterScreen(
        navController = NavHostController(context),
        uiState = LightMeterUiState(
            permissionGranted = true,
            luxValue = 5500f,
            brightness = 200f,
            evValue = 12.5f,
            lightLevel = context.getString(R.string.light_level_very_bright),
            recommendation = context.getString(R.string.recommendation_outdoor_plants)
        ),
        onBindCamera = { _, _ -> },
        onPermissionResult = {}
    )
}

