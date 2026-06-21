package com.baothanhbin.feature.camera.component

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
import com.baothanhbin.feature.camera.*
import com.baothanhbin.feature.camera.*
import com.baothanhbin.feature.camera.component.*

@Composable
internal fun SlidingSegmentedToggle(
    modifier: Modifier = Modifier,
    options: List<String> = listOf(stringResource(R.string.diagnose),stringResource(R.string.identify_plant)),
    selectedIndex: Int,
    onOptionSelected: (Int) -> Unit
) {
    val cornerRadius = 50.dp
    val backgroundColor = Color(0xFF4CAF50)

    BoxWithConstraints(
        modifier = modifier
            .height(50.dp)
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
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { 
                            val modeName = if (index == 0) "Chuẩn đoán (DETECT)" else "Nhận diện cây (CLASSIFY)"
                            Log.d("BottomControlPanel", "Tab được click: index=$index, mode=$modeName, text=$text")
                            onOptionSelected(index) 
                        }
                        .padding(vertical = 8.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
