package com.baothanhbin.feature.camera

import android.Manifest
import android.util.Log
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
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import kotlinx.coroutines.delay
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
import androidx.compose.ui.res.stringResource
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
import com.baothanhbin.feature.processimage.ProcessImageViewModel
import com.baothanhbin.feature.processimage.navigation.navigateToProcessImage
import com.baothanhbin.feature.camera.component.*

@Composable
fun CameraRoute(
    navController: NavHostController,
    initialModeIndex: Int = 0,
    viewModel: CameraViewModel = androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel()
) {
    // Set initial mode when screen is first composed
    LaunchedEffect(initialModeIndex) {
        viewModel.selectMode(initialModeIndex)
    }
    
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()
    
    val currentSelectedIndex = uiState.value.selectedModeIndex
    
    CameraScreen(
        navController = navController,
        selectedIndex = currentSelectedIndex,
        permissionGranted = uiState.value.permissionGranted,
        onOptionSelected = viewModel::selectMode,
        onBindPreview = viewModel::bindPreview,
        onPermissionResult = viewModel::onPermissionResult,
        onTakePhoto = { pw, ph, fw, fh, onSaved, onError ->
            viewModel.takePhoto(pw, ph, fw, fh,
                { uri ->
                    onSaved(uri)
                    // Sử dụng currentSelectedIndex để quyết định API type: 0 = DETECT, 1 = CLASSIFY
                    // Đọc lại từ uiState để đảm bảo có giá trị mới nhất
                    val selectedIndex = viewModel.uiState.value.selectedModeIndex
                    val apiType = if (selectedIndex == 0) "DETECT" else "CLASSIFY"
                    val modeName = if (selectedIndex == 0) "Chuẩn đoán (DETECT)" else "Nhận diện cây (CLASSIFY)"
                    Log.d("CameraRoute", "Chụp ảnh với tab: index=$selectedIndex, mode=$modeName, apiType=$apiType")
                    navController.navigateToProcessImage(uri, apiType)
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
    
    // State để lưu URI và apiType từ photo picker, sẽ navigate sau khi activity đã đóng hoàn toàn
    var pendingImageUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var pendingApiType by remember { mutableStateOf<String?>(null) }
    
    // Image picker launcher - sử dụng selectedIndex hiện tại để quyết định apiType
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: android.net.Uri? ->
        uri?.let {
            val apiType = if (selectedIndex == 0) "DETECT" else "CLASSIFY"
            val modeName = if (selectedIndex == 0) "Chuẩn đoán (DETECT)" else "Nhận diện cây (CLASSIFY)"
            Log.d("CameraScreen", "Chọn ảnh từ gallery với tab: index=$selectedIndex, mode=$modeName, apiType=$apiType")
            // Lưu vào state và sẽ navigate sau một delay ngắn để tránh xung đột với transition
            pendingImageUri = it
            pendingApiType = apiType
        }
    }
    
    // Navigate sau khi activity result được xử lý và có một delay ngắn
    LaunchedEffect(pendingImageUri, pendingApiType) {
        val uri = pendingImageUri
        val apiType = pendingApiType
        if (uri != null && apiType != null) {
            // Delay ngắn để đảm bảo photo picker activity đã đóng hoàn toàn
            delay(100)
            navController.navigateToProcessImage(uri, apiType)
            // Reset state
            pendingImageUri = null
            pendingApiType = null
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
                    contentDescription = stringResource(R.string.close),
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
                    contentDescription = stringResource(R.string.switch_camera),
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
