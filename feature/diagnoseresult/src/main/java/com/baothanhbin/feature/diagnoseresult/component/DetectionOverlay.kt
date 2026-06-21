package com.baothanhbin.feature.diagnoseresult.component

import android.net.Uri
import android.os.Build
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.ChatBubble
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
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import com.baothanhbin.core.model.ApiType
import com.baothanhbin.core.model.ClassifyData
import com.baothanhbin.core.model.DetectionData
import com.baothanhbin.core.ui.util.LocationStateHolder
import com.baothanhbin.agridoctorai.resources.R
import com.baothanhbin.core.model.RecoveryItem
import com.baothanhbin.core.model.TreatmentItem
import com.baothanhbin.core.theme.Button1
import com.baothanhbin.core.theme.GreenSurface
import com.baothanhbin.core.theme.Label4
import com.baothanhbin.core.theme.Subtitle
import com.baothanhbin.core.theme.TitleLarge1
import com.baothanhbin.core.theme.TitleLarge3
import com.baothanhbin.core.theme.White
import kotlinx.serialization.json.Json
import android.util.Base64
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import com.baothanhbin.feature.diagnoseresult.*
import com.baothanhbin.feature.diagnoseresult.*
import com.baothanhbin.feature.diagnoseresult.component.*

@Composable
internal fun DetectionOverlay(
    detections: List<DetectionData>,
    sourceImageSize: IntSize,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val scale = maxOf(
            size.width / sourceImageSize.width.toFloat(),
            size.height / sourceImageSize.height.toFloat()
        )
        val scaledWidth = sourceImageSize.width * scale
        val scaledHeight = sourceImageSize.height * scale
        val offsetX = (size.width - scaledWidth) / 2f
        val offsetY = (size.height - scaledHeight) / 2f
        val labelTextPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = 11.dp.toPx()
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
            isAntiAlias = true
        }
        val labelBounds = android.graphics.Rect()
        val labelHorizontalPadding = 6.dp.toPx()
        val labelVerticalPadding = 3.dp.toPx()
        val labelCornerRadius = 6.dp.toPx()

        detections.forEach { detection ->
            if (detection.confidence <= MIN_DETECTION_CONFIDENCE_TO_DRAW) return@forEach
            if (detection.box.size < 4) return@forEach

            val boxColor = if (
                detection.name.contains("khoe", ignoreCase = true) ||
                detection.name.contains("healthy", ignoreCase = true)
            ) {
                Color(0xFF2E7D32)
            } else {
                Color(0xFFD32F2F)
            }

            val left = detection.box[0].toFloat() * scale + offsetX
            val top = detection.box[1].toFloat() * scale + offsetY
            val right = detection.box[2].toFloat() * scale + offsetX
            val bottom = detection.box[3].toFloat() * scale + offsetY

            drawRect(
                color = boxColor,
                topLeft = androidx.compose.ui.geometry.Offset(left, top),
                size = androidx.compose.ui.geometry.Size(right - left, bottom - top),
                style = Stroke(width = 4.dp.toPx())
            )

            val confidenceLabel = formatDetectionConfidence(detection.confidence)
            labelTextPaint.getTextBounds(confidenceLabel, 0, confidenceLabel.length, labelBounds)

            val labelWidth = labelBounds.width() + (labelHorizontalPadding * 2f)
            val labelHeight = labelBounds.height() + (labelVerticalPadding * 2f)
            val maxLabelLeft = (size.width - labelWidth).coerceAtLeast(0f)
            val maxLabelTop = (size.height - labelHeight).coerceAtLeast(0f)
            val labelLeft = left.coerceIn(0f, maxLabelLeft)
            val preferredLabelTop = top - labelHeight
            val labelTop = if (preferredLabelTop >= 0f) {
                preferredLabelTop.coerceAtMost(maxLabelTop)
            } else {
                top.coerceIn(0f, maxLabelTop)
            }

            drawRoundRect(
                color = boxColor,
                topLeft = androidx.compose.ui.geometry.Offset(labelLeft, labelTop),
                size = androidx.compose.ui.geometry.Size(labelWidth, labelHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(
                    x = labelCornerRadius,
                    y = labelCornerRadius
                )
            )

            val baseline = labelTop + labelVerticalPadding - labelBounds.top
            drawContext.canvas.nativeCanvas.drawText(
                confidenceLabel,
                labelLeft + labelHorizontalPadding,
                baseline,
                labelTextPaint
            )
        }
    }
}
