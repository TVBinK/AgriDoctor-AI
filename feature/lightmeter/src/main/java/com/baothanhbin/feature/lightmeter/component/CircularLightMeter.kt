package com.baothanhbin.feature.lightmeter.component

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
import com.baothanhbin.feature.lightmeter.*
import com.baothanhbin.feature.lightmeter.*
import com.baothanhbin.feature.lightmeter.component.*

@Composable
internal fun CircularLightMeter(
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
