package com.baothanhbin.feature.processimage

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.baothanhbin.agridoctorai.resources.R
import com.baothanhbin.core.theme.Caption
import com.baothanhbin.core.theme.GreenSurface
import com.baothanhbin.core.theme.Subtitle
import com.baothanhbin.core.theme.TitleLarge1
import com.baothanhbin.core.theme.TitleLarge3
import com.baothanhbin.core.theme.White
import kotlinx.coroutines.delay
import com.baothanhbin.core.network.NetworkDataSource
import java.net.URLConnection
import android.util.Log

@Composable
fun ProcessImageRoute(
    navController: NavHostController,
    imageUri: Uri? = null
) {
    ProcessImageScreen(
        imageUri = imageUri,
        onBack = { navController.popBackStack() }
    )
}

@Composable
fun ProcessImageScreen(
    imageUri: Uri?,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var apiResult by remember { mutableStateOf<String?>(null) }
    var step by remember { mutableStateOf(0) }

    LaunchedEffect(key1 = imageUri) {
        apiResult = null
        step = 0
        imageUri ?: return@LaunchedEffect
        
        Log.d("ProcessImageScreen", "Image URI: $imageUri")
        
        try {
            step = 0
            Log.d("ProcessImageScreen", "Reading image bytes from URI...")
            val bytes = context.contentResolver.openInputStream(imageUri)?.use { it.readBytes() }
            if (bytes == null) {
                Log.e("ProcessImageScreen", "Failed to read image bytes")
                return@LaunchedEffect
            }
            Log.d("ProcessImageScreen", "Image bytes read: ${bytes.size} bytes")
            
            // Guess mime from URI path
            val name = imageUri.lastPathSegment ?: "image.jpg"
            val mime = URLConnection.guessContentTypeFromName(name) ?: "image/jpeg"
            Log.d("ProcessImageScreen", "File name: $name, Mime type: $mime")
            
            step = 1 // uploading
            Log.d("ProcessImageScreen", "Calling detectImage API...")
            val result = NetworkDataSource.detectImage(bytes, name, mime)
            
            step = 2 // processing done
            Log.d("ProcessImageScreen", "API result received: $result")
            apiResult = result
            step = 3
            Log.d("ProcessImageScreen", "Process completed successfully")
        } catch (e: Exception) {
            Log.e("ProcessImageScreen", "Error processing image: ${e.message}", e)
            Log.e("ProcessImageScreen", "Stack trace: ${e.stackTraceToString()}")
            // keep apiResult null
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(White)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp)
    ) {
        TopBar(onBack = onBack)

        Spacer(modifier = Modifier.height(50.dp))

        ImageCard(imageUri = imageUri)

        Spacer(modifier = Modifier.height(24.dp))

        Column(modifier = Modifier.padding(horizontal = 16.dp)) {

            CheckItem(
                text = "Upload image to server",
                state = when {
                    step >= 1 -> StepState.Done
                    step == 0 -> StepState.Loading
                    else -> StepState.Pending
                }
            )
            Spacer(modifier = Modifier.height(14.dp))
            CheckItem(
                text = "Processing image on server",
                state = when {
                    step >= 2 -> StepState.Done
                    step == 1 -> StepState.Loading
                    else -> StepState.Pending
                }
            )
            Spacer(modifier = Modifier.height(14.dp))
            CheckItem(
                text = "Received result",
                state = when {
                    step >= 3 -> StepState.Done
                    step == 2 -> StepState.Loading
                    else -> StepState.Pending
                }
            )

            if (apiResult != null) {
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = apiResult ?: "",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFF333333)
                )
            }
        }
    }
}

@Composable
private fun TopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .padding(top = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Back", tint = GreenSurface)
        }
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "Disease Diagnosis",
            style = MaterialTheme.typography.TitleLarge1,
            color = GreenSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ImageCard(imageUri: Uri?) {
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
                val translateY = if (containerHeightPx > 0f) (containerHeightPx - barHeightPx) * offsetYFraction else 0f

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
                        .graphicsLayer { translationY = translateY + (barHeightPx / 2f) - with(density) { 1.dp.toPx() } }
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

private enum class StepState { Pending, Loading, Done }

@Composable
private fun CheckItem(text: String, state: StepState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        if (state == StepState.Loading) {
            CircularProgressIndicator(
                color = Caption,
                strokeWidth = 2.dp,
                modifier = Modifier.size(22.dp)
            )
        } else if (state == StepState.Done) {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = Color(0xFF2ECC71),
                modifier = Modifier.size(26.dp)
            )
        } else {
            Icon(
                imageVector = Icons.Filled.Lens,
                contentDescription = null,
                tint = Color(0xFF9E9E9E),
                modifier = Modifier.size(10.dp)
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Normal),
            color = if (state == StepState.Done) Color(0xFF616161) else Subtitle
        )
    }
}

@Preview
@Composable
private fun ProcessImagePreview() {
    ProcessImageScreen(imageUri = null, onBack = {})
}