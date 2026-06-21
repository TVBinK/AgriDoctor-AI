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
internal fun FocusCorners(
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
