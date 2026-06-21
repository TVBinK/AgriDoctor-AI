package com.baothanhbin.feature.processimage.component

import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lens
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.navOptions
import coil.compose.AsyncImage
import com.baothanhbin.agridoctorai.resources.R
import com.baothanhbin.core.theme.Caption
import com.baothanhbin.core.theme.GreenSurface
import com.baothanhbin.core.theme.Subtitle
import com.baothanhbin.core.theme.TitleLarge1
import com.baothanhbin.core.model.ApiType
import com.baothanhbin.core.model.ClassifyData
import com.baothanhbin.core.theme.White
import com.baothanhbin.core.ui.util.LocationStateHolder
import com.baothanhbin.feature.diagnosefailed.navigation.navigateToDiagnoseFailed
import com.baothanhbin.feature.diagnoseresult.navigation.navigateToDiagnoseResult
import com.baothanhbin.feature.processimage.*
import com.baothanhbin.feature.processimage.*
import com.baothanhbin.feature.processimage.component.*

@Composable
internal fun ImageCard(imageUri: Uri?) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 50.dp)
            .height(330.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (imageUri != null) {
                AsyncImage(
                    model = imageUri,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.img_ca_chua),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            // Animated scanning bar overlay
            val barHeight = 36.dp
            val density = LocalDensity.current
            var containerHeightPx by remember { mutableStateOf(0f) }
            val barHeightPx = with(density) { barHeight.toPx() }

            val infinite = rememberInfiniteTransition(label = "scan_bar")
            val offsetYFraction by infinite.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 1200, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "offsetYFraction"
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .onGloballyPositioned { containerHeightPx = it.size.height.toFloat() }
            ) {
                val translateY =
                    if (containerHeightPx > 0f) (containerHeightPx - barHeightPx) * offsetYFraction else 0f

                // Soft colored band right at the scan line
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(barHeight)
                        .align(Alignment.TopStart)
                        .graphicsLayer { translationY = translateY }
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color(0xFF00C2B2).copy(alpha = 0.65f),
                                    Color.Transparent
                                )
                            )
                        )
                        .zIndex(1f)
                )

                // Bright center line to make the scan bar more visible
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .align(Alignment.TopStart)
                        .graphicsLayer {
                            translationY =
                                translateY + (barHeightPx / 2f) - with(density) { 1.dp.toPx() }
                        }
                        .background(Color(0xFF00E0C2).copy(alpha = 0.95f))
                        .zIndex(2f)
                )

                // Faded shadow below the scanning bar
                val tailHeight = with(density) { 90.dp.toPx() }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(90.dp)
                        .align(Alignment.TopStart)
                        .graphicsLayer { translationY = translateY + (barHeightPx / 2f) }
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF00C2B2).copy(alpha = 0.35f),
                                    Color.Transparent
                                ),
                                startY = 0f,
                                endY = tailHeight
                            )
                        )
                        .zIndex(0.9f)
                )
            }
        }
    }
}
