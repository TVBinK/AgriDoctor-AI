package com.baothanhbin.feature.camera

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.baothanhbin.agridoctorai.resources.R
import com.baothanhbin.feature.processimage.navigation.navigateToProcessImage

@Composable
fun CameraRoute(
    navController: NavHostController,
    viewModel: CameraViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()
    CameraScreen(
        navController = navController,
        selectedIndex = uiState.value.selectedModeIndex,
        permissionGranted = uiState.value.permissionGranted,
        onOptionSelected = viewModel::selectMode,
        onBindPreview = viewModel::bindPreview,
        onPermissionResult = viewModel::onPermissionResult,
        onTakePhoto = { pw, ph, fw, fh, onSaved, onError ->
            viewModel.takePhoto(pw, ph, fw, fh,
                { uri ->
                    onSaved(uri)
                    navController.navigateToProcessImage(uri)
                },
                onError
            )
        },
        onClose = { navController.popBackStack() },
        onSwitchCamera = viewModel::switchCamera
    )
}

@Composable
fun CameraScreen(
    navController: NavHostController,
    selectedIndex: Int,
    permissionGranted: Boolean,
    onOptionSelected: (Int) -> Unit,
    onBindPreview: (LifecycleOwner, PreviewView) -> Unit,
    onPermissionResult: (Boolean) -> Unit,
    onTakePhoto: (Int, Int, Int, Int, (android.net.Uri) -> Unit, (String) -> Unit) -> Unit,
    onClose: () -> Unit,
    onSwitchCamera: () -> Unit
) {
    val density = LocalDensity.current
    val focusWidthPx = remember { with(density) { 300.dp.toPx().toInt() } }
    val focusHeightPx = remember { with(density) { 350.dp.toPx().toInt() } }
    var previewWidth by remember { mutableStateOf(0) }
    var previewHeight by remember { mutableStateOf(0) }
    
    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: android.net.Uri? ->
        uri?.let {
            navController.navigateToProcessImage(it)
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Spacer(modifier = Modifier.height(15.dp))
        // Camera preview area fills remaining space above BottomControlPanel
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            SideEffect {
                previewWidth = with(density) { maxWidth.toPx().toInt() }
                previewHeight = with(density) { maxHeight.toPx().toInt() }
            }
            
            PermissionedCamera(
                permissionGranted = permissionGranted,
                onBindPreview = onBindPreview,
                onPermissionResult = onPermissionResult,
                modifier = Modifier.matchParentSize()
            )
            // Dim everything outside of the focus rect
            FocusOverlay(
                modifier = Modifier.matchParentSize(),
                focusWidth = 300.dp,
                focusHeight = 350.dp,
                scrimColor = Color.Black.copy(alpha = 0.55f)
            )

            // Focus corner brackets
            FocusCorners(
                modifier = Modifier
                    .align(Alignment.Center)
                    .width(300.dp)
                    .height(350.dp),
                cornerLength = 28.dp,
                strokeWidth = 2.dp,
                color = Color.White.copy(alpha = 0.9f)
            )
            // Camera close button (top left)
            IconButton(
                onClick = onClose,
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
                onClick = onSwitchCamera,
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
            modifier = Modifier.fillMaxWidth(),
            selectedIndex = selectedIndex,
            onOptionSelected = onOptionSelected,
            onTakePhoto = { onPhotoSaved, onError ->
                onTakePhoto(previewWidth, previewHeight, focusWidthPx, focusHeightPx, onPhotoSaved, onError)
            },
            onSelectFromGallery = {
                imagePickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }
        )
    }
}
@Composable
private fun PermissionedCamera(
    permissionGranted: Boolean,
    onBindPreview: (LifecycleOwner, PreviewView) -> Unit,
    onPermissionResult: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        onPermissionResult(isGranted)
    }

    LaunchedEffect(Unit) {
        if (!permissionGranted) launcher.launch(Manifest.permission.CAMERA)
    }

    if (permissionGranted) {
        CameraPreview(onBindPreview = onBindPreview, modifier = modifier)
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
private fun BottomControlPanel(
    modifier: Modifier = Modifier,
    selectedIndex: Int,
    onOptionSelected: (Int) -> Unit,
    onTakePhoto: ((android.net.Uri) -> Unit, (String) -> Unit) -> Unit,
    onSelectFromGallery: () -> Unit
) {
    val context = LocalContext.current
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color(0xFF4CAF50),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .padding(bottom = 15.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SlidingSegmentedToggle(
                selectedIndex = selectedIndex,
                onOptionSelected = onOptionSelected,
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
                    modifier = Modifier
                        .size(55.dp)
                        .clickable { onSelectFromGallery() }
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_lavender),
                        contentDescription = "Plant preview",
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(Modifier.width(50.dp))

                Box(
                    contentAlignment = Alignment.Center, 
                    modifier = Modifier
                        .size(78.dp)
                        .clickable {
                            onTakePhoto(
                                { uri ->
                                    Toast.makeText(
                                        context,
                                        "Photo saved: $uri",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                { error ->
                                    Toast.makeText(
                                        context,
                                        error,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            )
                        }
                ) {
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

                Spacer(Modifier.width(50.dp))

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
        }
    }
}

@Composable
private fun FocusCorners(
    modifier: Modifier,
    cornerLength: Dp,
    strokeWidth: Dp,
    color: Color
) {
    Canvas(modifier = modifier) {
        val sw = strokeWidth.toPx()
        val l = cornerLength.toPx()
        // Inset by half stroke to avoid clipping
        val left = sw / 2f
        val top = sw / 2f
        val right = size.width - sw / 2f
        val bottom = size.height - sw / 2f

        val path = androidx.compose.ui.graphics.Path()

        // Top-left corner: ┌
        path.moveTo(left + l, top)
        path.lineTo(left, top)
        path.lineTo(left, top + l)

        // Top-right corner: ┐
        path.moveTo(right - l, top)
        path.lineTo(right, top)
        path.lineTo(right, top + l)

        // Bottom-left corner: └
        path.moveTo(left, bottom - l)
        path.lineTo(left, bottom)
        path.lineTo(left + l, bottom)

        // Bottom-right corner: ┘
        path.moveTo(right, bottom - l)
        path.lineTo(right, bottom)
        path.lineTo(right - l, bottom)

        drawPath(
            path = path,
            color = color,
            style = Stroke(width = sw, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

@Composable
private fun FocusOverlay(
    modifier: Modifier,
    focusWidth: Dp,
    focusHeight: Dp,
    scrimColor: Color
) {
    Canvas(modifier = modifier) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        val focusWidthPx = focusWidth.toPx()
        val focusHeightPx = focusHeight.toPx()

        val left = (canvasWidth - focusWidthPx) / 2f
        val top = (canvasHeight - focusHeightPx) / 2f
        val right = left + focusWidthPx
        val bottom = top + focusHeightPx

        // Top area above focus
        drawRect(
            color = scrimColor,
            topLeft = Offset(0f, 0f),
            size = Size(canvasWidth, top)
        )
        // Bottom area below focus
        drawRect(
            color = scrimColor,
            topLeft = Offset(0f, bottom),
            size = Size(canvasWidth, canvasHeight - bottom)
        )
        // Left area of focus
        drawRect(
            color = scrimColor,
            topLeft = Offset(0f, top),
            size = Size(left, focusHeightPx)
        )
        // Right area of focus
        drawRect(
            color = scrimColor,
            topLeft = Offset(right, top),
            size = Size(canvasWidth - right, focusHeightPx)
        )
    }
}

@Composable
private fun CameraPreview(onBindPreview: (LifecycleOwner, PreviewView) -> Unit, modifier: Modifier = Modifier) {
    val lifecycleOwner = LocalLifecycleOwner.current
    AndroidView(
        modifier = modifier,
        factory = { ctx -> PreviewView(ctx) },
        update = { previewView ->
            onBindPreview(lifecycleOwner, previewView)
        }
    )
}

@Preview
@Composable
fun CameraScreenPreview() {
    val navController = rememberNavController()
    CameraScreen(
        navController = navController,
        selectedIndex = 0,
        permissionGranted = false,
        onOptionSelected = {},
        onBindPreview = { _, _ -> },
        onPermissionResult = {},
        onTakePhoto = { _, _, _, _, _, _ -> },
        onClose = {},
        onSwitchCamera = {}
    )
}
