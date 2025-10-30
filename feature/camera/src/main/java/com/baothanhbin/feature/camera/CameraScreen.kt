package com.baothanhbin.feature.camera

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.Dp
import com.baothanhbin.agridoctorai.resources.R
import com.baothanhbin.core.theme.TitleLarge3
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.camera.view.PreviewView
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview as CameraXPreview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.pm.PackageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun CameraRoute() {
    CameraScreen()
}

@Composable
fun CameraScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Camera preview area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(420.dp)
                .align(Alignment.TopCenter)
                .padding(top=20.dp)
        ) {
            PermissionedCamera(modifier = Modifier.matchParentSize())
            // Side scrims to mimic camera overlay
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .fillMaxHeight()
                    .width(60.dp)
                    .background(Color.Black.copy(alpha = 0.55f))
            )
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight()
                    .width(60.dp)
                    .background(Color.Black.copy(alpha = 0.55f))
            )

            // Focus corner brackets
            FocusCorners(
                modifier = Modifier
                    .align(Alignment.Center)
                    .width(340.dp)
                    .height(240.dp),
                cornerLength = 28.dp,
                strokeWidth = 4.dp,
                color = Color.White.copy(alpha = 0.9f),
                radius = 12.dp
            )
            // Camera close button (top left)
            IconButton(
                onClick = { /* Close action */ },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(10.dp)
                    .size(36.dp)
                    .background(Color.Black.copy(alpha = 0.3f), shape = CircleShape)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_close),
                    contentDescription = "Close",
                    tint = Color.White
                )
            }
            // Camera flip button (top right)
            IconButton(
                onClick = { /* Switch camera */ },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.dp)
                    .size(36.dp)
                    .background(Color.Black.copy(alpha = 0.3f), shape = CircleShape)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_switch_camera),
                    contentDescription = "Switch camera",
                    tint = Color.White
                )
            }
        }

        // Bottom control panel
        BottomControlPanel(
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}
@Composable
private fun PermissionedCamera(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var granted by remember { mutableStateOf(
        ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    ) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        granted = isGranted
    }

    LaunchedEffect(Unit) {
        if (!granted) launcher.launch(Manifest.permission.CAMERA)
    }

    if (granted) {
        CameraPreview(modifier = modifier)
    } else {
        // Fallback UI when permission denied
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text("Camera permission required", color = Color.White)
        }
    }
}

// Sliding segmented control like the provided design
@Composable
private fun SlidingSegmentedToggle(
    modifier: Modifier = Modifier,
    options: List<String> = listOf("Diagnose", "Identify"),
    selectedIndex: Int,
    onOptionSelected: (Int) -> Unit
) {
    val cornerRadius = 50.dp
    val backgroundColor = Color(0xFF4CAF50)

    BoxWithConstraints(
        modifier = modifier
            .height(40.dp)
            .clip(RoundedCornerShape(cornerRadius))
            .background(backgroundColor)
            .padding(4.dp)
    ) {
        val segmentWidth = this.maxWidth / options.size
        val targetOffset = segmentWidth * selectedIndex
        val animatedOffset = animateDpAsState(
            targetValue = targetOffset,
            animationSpec = tween(durationMillis = 250, easing = LinearOutSlowInEasing),
            label = "seg_offset"
        )

        // Sliding white indicator
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(segmentWidth)
                .offset(x = animatedOffset.value)
                .clip(RoundedCornerShape(cornerRadius))
                .background(Color.White)
        )

        Row(
            Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            options.forEachIndexed { index, text ->
                Text(
                    text = text,
                    fontWeight = if (selectedIndex == index) FontWeight.Medium else FontWeight.Normal,
                    color = if (selectedIndex == index) Color(0xFF4CAF50) else Color(0xFF1A1A1A),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onOptionSelected(index) }
                        .padding(vertical = 8.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun BottomControlPanel(modifier: Modifier = Modifier) {
    val (selectedIndex, setSelectedIndex) = remember { mutableStateOf(0) }
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color(0xFF4CAF50),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SlidingSegmentedToggle(
                selectedIndex = selectedIndex,
                onOptionSelected = { setSelectedIndex(it) },
                modifier = Modifier.fillMaxWidth(0.6f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    border = BorderStroke(2.dp, Color.White),
                    modifier = Modifier.size(55.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_lavender),
                        contentDescription = "Plant preview",
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(Modifier.width(32.dp))

                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(78.dp)) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(Color.White, CircleShape)
                            .border(BorderStroke(2.dp, Color.White), CircleShape)
                    )
                    Box(
                        modifier = Modifier
                            .size(58.dp)
                            .border(BorderStroke(5.dp, Color(0xFF2ECC71)), CircleShape)
                    )
                }

                Spacer(Modifier.width(32.dp))

                Surface(
                    color = Color.Transparent,
                    shape = CircleShape,
                    border = BorderStroke(2.dp, Color.White),
                    modifier = Modifier.size(55.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_help),
                            contentDescription = "Help",
                            tint = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .width(120.dp)
                    .height(4.dp)
                    .background(Color(0xFF2B6E36), RoundedCornerShape(2.dp))
            )
        }
    }
}

@Composable
private fun FocusCorners(
    modifier: Modifier,
    cornerLength: Dp,
    strokeWidth: Dp,
    color: Color,
    radius: Dp
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val l = cornerLength.toPx()
        val sw = strokeWidth.toPx()
        val r = radius.toPx()

        // top-left
        drawLine(color, Offset(r, 0f), Offset(l, 0f), sw, cap = StrokeCap.Round)
        drawLine(color, Offset(0f, r), Offset(0f, l), sw, cap = StrokeCap.Round)
        // top-right
        drawLine(color, Offset(w - r, 0f), Offset(w - l, 0f), sw, cap = StrokeCap.Round)
        drawLine(color, Offset(w, r), Offset(w, l), sw, cap = StrokeCap.Round)
        // bottom-left
        drawLine(color, Offset(r, h), Offset(l, h), sw, cap = StrokeCap.Round)
        drawLine(color, Offset(0f, h - r), Offset(0f, h - l), sw, cap = StrokeCap.Round)
        // bottom-right
        drawLine(color, Offset(w - r, h), Offset(w - l, h), sw, cap = StrokeCap.Round)
        drawLine(color, Offset(w, h - r), Offset(w, h - l), sw, cap = StrokeCap.Round)
    }
}

@Composable
private fun CameraPreview(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            previewView
        },
        update = { previewView ->
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                val preview: CameraXPreview = CameraXPreview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview)
                } catch (_: Exception) {}
            }, context.mainExecutor)
        }
    )
}

@Preview
@Composable
fun CameraScreenPreview() {
    CameraScreen()
}
