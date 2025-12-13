package com.baothanhbin.feature.diagnoseresult

import android.net.Uri
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.baothanhbin.core.model.ApiType
import com.baothanhbin.core.model.ClassifyData
import com.baothanhbin.core.ui.util.LocationStateHolder
import com.baothanhbin.agridoctorai.resources.R
import com.baothanhbin.core.model.RecoveryItem
import com.baothanhbin.core.model.TreatmentItem
import com.baothanhbin.core.theme.BlueDefault
import com.baothanhbin.core.theme.Body1
import com.baothanhbin.core.theme.GreenSurface
import com.baothanhbin.core.theme.Label1
import com.baothanhbin.core.theme.Label4
import com.baothanhbin.core.theme.Red1
import com.baothanhbin.core.theme.RedErr
import com.baothanhbin.core.theme.Subtitle
import com.baothanhbin.core.theme.TitleLarge1
import com.baothanhbin.core.theme.TitleLarge3
import com.baothanhbin.core.theme.White

@Composable
fun DiagnoseResultRoute(
    navController: NavHostController? = null,
    imageUri: Uri? = null,
    diseaseName: String = "Unknown Disease",
    possibleProblems: List<String> = emptyList(),
    symptoms: String = "",
    causes: String = "",
    treatment: List<TreatmentItem> = emptyList(),
    recoveryCare: List<RecoveryItem> = emptyList(),
    location: String? = null,
    locationStateHolder: LocationStateHolder = LocationStateHolder(),
    apiType: ApiType = ApiType.DETECT,
    classifyData: ClassifyData? = null,
    viewModel: DiagnoseResultViewModel = hiltViewModel()
) {
    val loadedResult by viewModel.loadedResult.collectAsState()

    LaunchedEffect(Unit) {
        // Only load from database if it's DETECT API
        if (apiType == ApiType.DETECT) {
            viewModel.loadLatestResult()
        }
    }

    // Use loaded data if available, otherwise use default parameters
    // For CLASSIFY API, use classifyData
    val finalData = if (apiType == ApiType.CLASSIFY && classifyData != null) {
        LoadedResult(
            diseaseName = classifyData.plantNameVN ?: classifyData.plantName,
            possibleProblems = classifyData.commonDiseases ?: emptyList(),
            symptoms = "",
            causes = "",
            treatment = emptyList(),
            recoveryCare = emptyList(),
            location = location
        )
    } else {
        loadedResult ?: LoadedResult(
            diseaseName = diseaseName,
            possibleProblems = possibleProblems,
            symptoms = symptoms,
            causes = causes,
            treatment = treatment,
            recoveryCare = recoveryCare,
            location = location
        )
    }
    val displayLocation = locationStateHolder.currentAddress ?: finalData.location

    LaunchedEffect(displayLocation) {
        if (!displayLocation.isNullOrBlank() && locationStateHolder.currentAddress != displayLocation) {
            locationStateHolder.updateAddress(displayLocation)
        }
    }

    DiagnoseResultScreen(
        imageUri = imageUri,
        diseaseName = finalData.diseaseName,
        possibleProblems = finalData.possibleProblems,
        symptoms = finalData.symptoms,
        causes = finalData.causes,
        treatment = finalData.treatment,
        recoveryCare = finalData.recoveryCare,
        location = displayLocation,
        apiType = apiType,
        classifyData = classifyData,
        onBack = { navController?.popBackStack() }
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
    onBack: () -> Unit = {}
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
                location = location
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
                apiType = apiType
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

        // simple bounding box
        /*Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(110.dp, 70.dp)
                .border(2.dp, Color(0xFFE53935))
        )*/
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
    apiType: ApiType
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
        
        Spacer(modifier = Modifier.height(24.dp))

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
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun ClassifyContentSection(
    imageUri: Uri?,
    onBack: () -> Unit,
    classifyData: ClassifyData,
    location: String?
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
        
        // Scientific Name and Family (if available)
        if (!scientificName.isNullOrBlank() || !family.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (!scientificName.isNullOrBlank()) {
                    Text(
                        text = scientificName,
                        style = MaterialTheme.typography.bodySmall,
                        color = Subtitle,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }
                if (!family.isNullOrBlank()) {
                    Text(
                        text = "Họ: $family",
                        style = MaterialTheme.typography.bodySmall,
                        color = Subtitle
                    )
                }
            }
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
        
        // Info Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Status
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF2ECC71),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = classifyData.classificationStatus ?: stringResource(R.string.status_identified),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Subtitle
                    )
                }
                
                // Confidence
                if (confidence != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "📊",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        // Xử lý confidence: nếu > 1.0 thì là phần trăm, nếu <= 1.0 thì nhân 100
                        val confidencePercent = if (confidence > 1.0) confidence else confidence * 100
                        Text(
                            text = "${stringResource(R.string.confidence)}: ${String.format("%.2f", confidencePercent)}%",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Subtitle
                        )
                    }
                }
                
                // Description
                if (!description.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "📝 ${stringResource(R.string.description)}:",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = GreenSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Subtitle,
                        lineHeight = 20.sp
                    )
                }
            }
        }
        // Common Diseases Section
        if (!commonDiseases.isNullOrEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))
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
                    commonDiseases.forEach { ProblemChip(it) }
                }
            }
        }
        
        // Care Tips Section
        if (!careTips.isNullOrEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "💡 Mẹo chăm sóc",
                    style = MaterialTheme.typography.TitleLarge3,
                    color = GreenSurface
                )
                Spacer(modifier = Modifier.height(10.dp))
                careTips.forEachIndexed { index, tip ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = if (index != careTips.lastIndex) 15.dp else 0.dp),
                        shape = RoundedCornerShape(10.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
                    ) {
                        Text(
                            text = "• $tip",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Subtitle,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
        }
        
    }
}

@Composable
private fun ProblemChip(text: String) {
    AssistChip(
        onClick = {},
        label = { Text(text = text, style = MaterialTheme.typography.Label1) },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = Red1,
            labelColor = RedErr
        )
    )
}

@Composable
private fun TextSectionCard(title: String, text: String) {
    if (text.isBlank()) return
    
    Column(modifier = Modifier.padding(horizontal = 14.dp)) {
        Text(title, style = MaterialTheme.typography.TitleLarge3)
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFDFD))
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = Subtitle,
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}



@Composable
private fun TreatmentItemCard(item: TreatmentItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                item.title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = GreenSurface
            )
            if (!item.subtitle.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    item.subtitle!!,
                    style = MaterialTheme.typography.Body1,
                    color = Color(0xFF2C3E50)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            item.steps.forEachIndexed { index, step ->
                Text(text = step, style = MaterialTheme.typography.Body1, color = Subtitle)
                if (index != item.steps.lastIndex) Spacer(modifier = Modifier.height(6.dp))
            }
        }
    }
}



@Composable
private fun RecoveryItemCard(item: RecoveryItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp),
        shape = RoundedCornerShape(10.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                item.title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = Color(0xFF1976D2)
            )
            Spacer(modifier = Modifier.height(6.dp))
            item.steps.forEachIndexed { index, s ->
                Text(s, style = MaterialTheme.typography.Body1, color = Subtitle)
                if (index != item.steps.lastIndex) Spacer(modifier = Modifier.height(6.dp))
            }
        }
    }
}

@Preview
@Composable
private fun PreviewDiagnoseResult() {
    DiagnoseResultScreen(
        diseaseName = "Tomato Early Blight",
        possibleProblems = listOf("Fungal Infection", "Nutrient Deficiency"),
        symptoms = "Dark spots on leaves\nYellowing of lower leaves\nStunted growth",
        causes = "Overwatering\nPoor air circulation\nInfected soil",
        treatment = listOf(
            TreatmentItem(
                title = "Fungicide Application",
                subtitle = "Use a copper-based fungicide",
                steps = listOf(
                    "Mix fungicide according to package instructions.",
                    "Apply to affected plants every 7-10 days."
                ),
                linkText = "Buy Fungicide"
            )
        ),
        recoveryCare = listOf(
            RecoveryItem(
                title = "Post-Treatment Care",
                steps = listOf(
                    "Monitor plants for new symptoms.",
                    "Ensure proper watering and spacing."
                ),
                linkText = "Learn More"
            )
        ),
        location = "Hà Đông, Hà Nội"
    )
}
