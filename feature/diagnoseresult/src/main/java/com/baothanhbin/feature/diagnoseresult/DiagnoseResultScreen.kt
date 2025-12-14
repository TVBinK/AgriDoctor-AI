package com.baothanhbin.feature.diagnoseresult

import android.net.Uri
import android.os.Build
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import com.baothanhbin.core.model.ApiType
import com.baothanhbin.core.model.ClassifyData
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

@Composable
fun DiagnoseResultRoute(
    navController: NavHostController? = null,
    imageUri: Uri? = null,
    locationStateHolder: LocationStateHolder,
    viewModel: DiagnoseResultViewModel = hiltViewModel(),
    apiType: ApiType = ApiType.DETECT,
    classifyData: ClassifyData? = null,
    onChatWithAi: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // If apiType is CLASSIFY and we have classifyData, we don't need to fetch
    val effectiveUiState = if (apiType == ApiType.CLASSIFY && classifyData != null) {
        DiagnoseResultUiState(
            isLoading = false,
            // We don't have detection fields in classifyData, so we use defaults or empty
             diseaseName = "",
             possibleProblems = emptyList(),
             symptoms = "",
             causes = "",
             treatment = emptyList(),
             recoveryCare = emptyList()
        )
    } else {
        uiState
    }

    // Only fetch if DETECT type
    LaunchedEffect(Unit) {
        if (apiType == ApiType.DETECT) {
            viewModel.loadDiagnoseResult(imageUri)
        }
    }

    // Get current location
    val context = LocalContext.current
    val displayLocation = locationStateHolder.currentAddress ?: "Unknown Location"

    DiagnoseResultScreen(
        imageUri = imageUri,
        diseaseName = effectiveUiState.diseaseName,
        possibleProblems = effectiveUiState.possibleProblems,
        symptoms = effectiveUiState.symptoms,
        causes = effectiveUiState.causes,
        treatment = effectiveUiState.treatment,
        recoveryCare = effectiveUiState.recoveryCare,
        location = displayLocation,
        apiType = apiType,
        classifyData = classifyData,
        onBack = { navController?.popBackStack() },
        onChatWithAi = onChatWithAi
    )
}

@Composable
fun DiagnoseResultScreen(
    imageUri: Uri? = null,
    diseaseName: String,
    possibleProblems: List<String>,
    symptoms: String,
    causes: String,
    treatment: List<TreatmentItem>,
    recoveryCare: List<RecoveryItem>,
    location: String? = null,
    apiType: ApiType = ApiType.DETECT,
    classifyData: ClassifyData? = null,
    onBack: () -> Unit = {},
    onChatWithAi: (String) -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(White)
            .padding(bottom = 16.dp)
    ) {
        if (apiType == ApiType.CLASSIFY && classifyData != null) {
            ClassifyContentSection(
                imageUri = imageUri,
                onBack = onBack,
                classifyData = classifyData,
                location = location,
                onChatWithAi = onChatWithAi
            )
        } else {
            ContentSection(
                imageUri = imageUri,
                onBack = onBack,
                diseaseName = diseaseName,
                possibleProblems = possibleProblems,
                symptoms = symptoms,
                causes = causes,
                treatment = treatment,
                recoveryCare = recoveryCare,
                location = location,
                apiType = apiType,
                onChatWithAi = onChatWithAi
            )
        }
    }
}

@Composable
private fun HeaderImage(
    imageUri: Uri?,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(270.dp)
    ) {
        IconButton(
            onClick = onBack, modifier = Modifier
                .zIndex(1f)
                .padding(top = 20.dp)
        ) {
        Icon(
            painter = painterResource(R.drawable.ic_back),
            contentDescription = stringResource(R.string.back),
            tint = Color.Unspecified,
            modifier = Modifier.size(50.dp)
        )
        }
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
    }
}

@Composable
private fun ContentSection(
    imageUri: Uri?,
    onBack: () -> Unit,
    diseaseName: String,
    possibleProblems: List<String>,
    symptoms: String,
    causes: String,
    treatment: List<TreatmentItem>,
    recoveryCare: List<RecoveryItem>,
    location: String?,
    apiType: ApiType,
    onChatWithAi: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Header Image
        HeaderImage(imageUri = imageUri, onBack = onBack)
        
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
                    onChatWithAi("Cách điều trị bệnh $diseaseName")
                }
            )
        }
    }
}

@Composable
private fun ClassifyContentSection(
    imageUri: Uri?,
    onBack: () -> Unit,
    classifyData: ClassifyData,
    location: String?,
    onChatWithAi: (String) -> Unit
) {
    val plantName = classifyData.plantNameVN ?: classifyData.plantName
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
        HeaderImage(imageUri = imageUri, onBack = onBack)
        
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
                    onChatWithAi("Cách chăm sóc cây $plantName")
                }
            )
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Subtitle,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.width(200.dp),
            textAlign = TextAlign.End
        )
    }
}


@Composable
private fun ProblemChip(text: String) {
    AssistChip(
        onClick = { },
        label = { Text(text) },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = Color(0xFFFFEBEE),
            labelColor = Color(0xFFD32F2F)
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = Color(0xFFFFCDD2)
        )
    )
}

@Composable
private fun TextSectionCard(title: String, text: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.TitleLarge3,
            color = GreenSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8E9)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(16.dp),
                color = Color(0xFF33691E),
                lineHeight = 22.sp
            )
        }
    }
}

@Composable
private fun TreatmentItemCard(item: TreatmentItem) {
    Card(
        colors = CardDefaults.cardColors(containerColor = White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = GreenSurface
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            val subtitle = item.subtitle
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Subtitle
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            item.steps.forEachIndexed { index, step ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "• ",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Subtitle
                    )
                    Text(
                        text = step,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Subtitle,
                        modifier = Modifier.weight(1f)
                    )
                }
                if (index < item.steps.lastIndex) Spacer(modifier = Modifier.height(4.dp))
            }
            val linkText = item.linkText
            if (!linkText.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Recommended: $linkText",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFF1976D2),
                    fontStyle = FontStyle.Italic
                )
            }
        }
    }
}

@Composable
private fun RecoveryItemCard(item: RecoveryItem) {
    Card(
        colors = CardDefaults.cardColors(containerColor = White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1976D2)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            item.steps.forEachIndexed { i, step ->
                Row(verticalAlignment = Alignment.Top) {
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Subtitle,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = step,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Subtitle,
                        modifier = Modifier.weight(1f)
                    )
                }
                if (i < item.steps.lastIndex) Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

// === NEW COMPONENTS FOR CHAT CARD ===

@Composable
fun ChatCard(
    title: String, 
    description: String, 
    background: Color, 
    animatedRaw: Int? = null,
    onChatNowClick: () -> Unit = {}
) {
    val shape = RoundedCornerShape(16.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .shadow(6.dp, shape = shape, clip = false)
            .border(
                width = 2.dp,
                brush = Brush.linearGradient(
                    listOf(
                        Color(0xFF00E676), // green
                        Color(0xFF00B0FF)  // blue
                    )
                ),
                shape = shape
            )
            .clip(shape)
    ) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .padding(1.5.dp),
            shape = shape,
            colors = CardDefaults.cardColors(containerColor = background),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, style = MaterialTheme.typography.Button1)
                    Text(description, style = MaterialTheme.typography.Label4, color = Subtitle)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = onChatNowClick,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4285F4)),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.height(36.dp) // Smaller button
                    ) {
                        Text(stringResource(R.string.chat_now), fontSize = 12.sp, color = Color.White)
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                animatedRaw?.let { GifImage(animatedRaw = it) }
            }
        }
    }
}

@Composable
private fun GifImage(animatedRaw: Int) {
    val context = LocalContext.current
    val imageLoader = remember {
        ImageLoader.Builder(context).components {
            if (Build.VERSION.SDK_INT >= 28) {
                add(ImageDecoderDecoder.Factory())
            } else {
                add(GifDecoder.Factory())
            }
        }.build()
    }

    val resourceEntryName = try {
        context.resources.getResourceEntryName(animatedRaw)
    } catch (e: Exception) {
        "ic_tinh_linh"
    }
    // Fallback if resource name lookup fails (rare but possible with obfuscation) or dynamic load issues
    // Note: Assuming R.raw.ic_tinh_linh exists as passed from HomeScreen
    
    val gifUri = Uri.parse("android.resource://${context.packageName}/raw/$resourceEntryName")

    AsyncImage(
        model = ImageRequest.Builder(context).data(gifUri).crossfade(false).allowRgb565(false)
            .build(),
        contentDescription = null,
        modifier = Modifier.size(80.dp), // Reduced size slightly for smaller card
        contentScale = ContentScale.Fit,
        imageLoader = imageLoader
    )
}

@Preview(showBackground = true)
@Composable
fun DiagnoseResultScreenPreview() {
    DiagnoseResultScreen(
        imageUri = null,
        diseaseName = "Leaf Rust",
        possibleProblems = listOf("Fungal Infection", "High Humidity"),
        symptoms = "Brown spots on leaves, yellowing",
        causes = "Excessive moisture, poor air circulation",
        treatment = listOf(
            TreatmentItem(
                title = "Remove infected leaves",
                subtitle = "Cut off all visible infected leaves",
                steps = emptyList(),
                linkText = null
            ),
            TreatmentItem(
                title = "Apply fungicide",
                subtitle = "Spray copper-based fungicide",
                steps = emptyList(),
                linkText = "FungiStop 500"
            )
        ),
        recoveryCare = listOf(
            RecoveryItem("Watering", listOf("Water at base only", "Avoid wetting leaves")),
            RecoveryItem("Fertilizing", listOf("Use balanced NPK", "Reduce Nitrogen"))
        ),
        location = "Ho Chi Minh City"
    )
}
