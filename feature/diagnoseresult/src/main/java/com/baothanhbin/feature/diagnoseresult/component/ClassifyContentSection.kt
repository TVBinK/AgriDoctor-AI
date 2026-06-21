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
internal fun ClassifyContentSection(
    imageUri: Uri?,
    onBack: () -> Unit,
    classifyData: ClassifyData,
    location: String?,
    onChatWithAi: (String) -> Unit
) {
    val plantName = classifyData.plantNameVN ?: classifyData.plantName
    val chatPromptCare = stringResource(R.string.chat_prompt_care, plantName)
    val confidence = classifyData.confidence
    val description = classifyData.description
    val icon = classifyData.icon
    val scientificName = classifyData.scientificName
    val family = classifyData.family
    val careTips = classifyData.careTips
    val commonDiseases = classifyData.commonDiseases
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Header Image
        HeaderImage(
            imageUri = imageUri,
            detections = emptyList(),
            onBack = onBack
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Plant Name with Icon
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = icon ?: "🌱",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = plantName,
                style = MaterialTheme.typography.TitleLarge1,
                color = GreenSurface
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
                    tint = Subtitle,
                    modifier = Modifier.size(16.dp)
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

        Spacer(modifier = Modifier.height(24.dp))
        
        // Plant Info Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = stringResource(R.string.plant_info),
                style = MaterialTheme.typography.TitleLarge3,
                color = GreenSurface
            )
            Spacer(modifier = Modifier.height(10.dp))
            
            // Scientific Name
            InfoRow(label = stringResource(R.string.scientific_name), value = scientificName ?: "N/A")
            Divider(color = Color(0xFFF1F1F1), thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))
            
            // Family
            InfoRow(label = stringResource(R.string.family), value = family ?: "N/A")
            Divider(color = Color(0xFFF1F1F1), thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))
            
            // Confidence
            val confidencePercent = (confidence ?: 0.0) * 100
            InfoRow(label = stringResource(R.string.confidence), value = "${confidencePercent.toInt()}%")
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Description Section
        if (!description.isNullOrBlank()) {
            TextSectionCard(title = stringResource(R.string.description), text = description)
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Care Tips Section
        careTips?.let { tips ->
            if (tips.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.care_tips),
                        style = MaterialTheme.typography.TitleLarge3,
                        color = GreenSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8E9)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            tips.forEachIndexed { index, tip ->
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    Text(
                                        text = "• ",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color(0xFF33691E)
                                    )
                                    Text(
                                        text = tip,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color(0xFF33691E),
                                        lineHeight = 22.sp,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                if (index < tips.lastIndex) Spacer(modifier = Modifier.height(4.dp))
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
        
        // Common Diseases Section
        commonDiseases?.let { diseases ->
            if (diseases.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.common_diseases),
                        style = MaterialTheme.typography.TitleLarge3,
                        color = GreenSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8E9)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            diseases.forEachIndexed { index, disease ->
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    Text(
                                        text = "• ",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color(0xFF33691E)
                                    )
                                    Text(
                                        text = disease,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color(0xFF33691E),
                                        lineHeight = 22.sp,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                if (index < diseases.lastIndex) Spacer(modifier = Modifier.height(4.dp))
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // Chat Button Card
        Box(modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 20.dp)) {
            ChatCard(
                title = stringResource(R.string.need_plant_help),
                description = stringResource(R.string.get_instant_advice),
                background = Color.White,
                animatedRaw = R.raw.ic_tinh_linh,
                onChatNowClick = {
                    onChatWithAi(chatPromptCare)
                }
            )
        }
    }
}
