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
internal fun ContentSection(
    imageUri: Uri?,
    onBack: () -> Unit,
    diseaseName: String,
    possibleProblems: List<String>,
    symptoms: String,
    causes: String,
    treatment: List<TreatmentItem>,
    recoveryCare: List<RecoveryItem>,
    detections: List<DetectionData>,
    location: String?,
    apiType: ApiType,
    onChatWithAi: (String) -> Unit
) {
    val chatPromptTreatment = stringResource(R.string.chat_prompt_treatment, diseaseName)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Header Image
        HeaderImage(
            imageUri = imageUri,
            detections = detections,
            onBack = onBack
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Disease Name with Icon
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "🦠",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = diseaseName,
                style = MaterialTheme.typography.TitleLarge1,
                color = Color(0xFFE53935)
            )
        }
        
        // Location and Time
        if (!location.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_location),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = location,
                    style = MaterialTheme.typography.bodySmall,
                    color = Subtitle
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "•",
                    style = MaterialTheme.typography.bodySmall,
                    color = Subtitle
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
                        .format(java.util.Date()),
                    style = MaterialTheme.typography.bodySmall,
                    color = Subtitle
                )
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        // Possible Problems Section
        if (possibleProblems.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = stringResource(R.string.possible_problems),
                    style = MaterialTheme.typography.TitleLarge3,
                    color = Color.Red
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.horizontalScroll(rememberScrollState())
                ) {
                    possibleProblems.forEach { ProblemChip(it) }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(15.dp))

        // Symptoms Section
        if (symptoms.isNotBlank()) {
            TextSectionCard(title = stringResource(R.string.symptoms), text = symptoms)
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Causes Section
        if (causes.isNotBlank()) {
            TextSectionCard(title = stringResource(R.string.causes_of_disease), text = causes)
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Treatment Section
        if (treatment.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = stringResource(R.string.treatment),
                    style = MaterialTheme.typography.TitleLarge3,
                    color = GreenSurface
                )
                Spacer(modifier = Modifier.height(12.dp))
                treatment.forEachIndexed { index, item ->
                    TreatmentItemCard(item)
                    if (index != treatment.lastIndex) Spacer(modifier = Modifier.height(12.dp))
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Recovery Care Section
        if (recoveryCare.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = stringResource(R.string.recovery_care),
                    style = MaterialTheme.typography.TitleLarge3,
                    color = Color(0xFF1976D2)
                )
                Spacer(modifier = Modifier.height(12.dp))
                recoveryCare.forEachIndexed { index, item ->
                    RecoveryItemCard(item)
                    if (index != recoveryCare.lastIndex) Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Chat Button Card
        Box(modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 20.dp)) {
            ChatCard(
                title = stringResource(R.string.need_plant_help),
                description = stringResource(R.string.get_instant_advice),
                background = Color.White,
                animatedRaw = R.raw.ic_tinh_linh,
                onChatNowClick = {
                    onChatWithAi(chatPromptTreatment)
                }
            )
        }
    }
}
