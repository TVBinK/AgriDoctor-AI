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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
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
    viewModel: DiagnoseResultViewModel = hiltViewModel()
) {
    val loadedResult by viewModel.loadedResult.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadLatestResult()
    }

    // Use loaded data if available, otherwise use default parameters
    val finalData = loadedResult ?: LoadedResult(
        diseaseName = diseaseName,
        possibleProblems = possibleProblems,
        symptoms = symptoms,
        causes = causes,
        treatment = treatment,
        recoveryCare = recoveryCare,
        location = location
    )

    DiagnoseResultScreen(
        imageUri = imageUri,
        diseaseName = finalData.diseaseName,
        possibleProblems = finalData.possibleProblems,
        symptoms = finalData.symptoms,
        causes = finalData.causes,
        treatment = finalData.treatment,
        recoveryCare = finalData.recoveryCare,
        location = finalData.location,
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
    onBack: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(White)
            .padding(bottom = 16.dp)
    ) {
        ContentSection(
            imageUri = imageUri,
            onBack = onBack,
            diseaseName = diseaseName,
            possibleProblems = possibleProblems,
            symptoms = symptoms,
            causes = causes,
            treatment = treatment,
            recoveryCare = recoveryCare,
            location = location
        )
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
    location: String?
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        HeaderImage(imageUri = imageUri, onBack = onBack)
        Spacer(modifier = Modifier.height(8.dp))
        //Tên bệnh
        Text(
            text = diseaseName,
            style = MaterialTheme.typography.TitleLarge1,
            modifier = Modifier
                .padding(start = 16.dp)
                .align(Alignment.CenterHorizontally)
        )

        if (!location.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_location),
                    contentDescription = null,
                    tint = GreenSurface,
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = location,
                    style = MaterialTheme.typography.Label4.copy(fontSize = 15.sp),
                    color = Subtitle,
                    maxLines = 2
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = stringResource(R.string.possible_problems),
            style = MaterialTheme.typography.TitleLarge3,
            modifier = Modifier.padding(start = 16.dp),
            color = Color.Red
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .padding(start = 16.dp)
                .horizontalScroll(rememberScrollState())
        ) {
            possibleProblems.forEach { ProblemChip(it) }
        }

        Spacer(modifier = Modifier.height(14.dp))

        TextSectionCard(title = stringResource(R.string.symptoms), text = symptoms)

        Spacer(modifier = Modifier.height(14.dp))

        TextSectionCard(title = stringResource(R.string.causes_of_disease), text = causes)

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = stringResource(R.string.treatment),
            style = MaterialTheme.typography.TitleLarge3,
            modifier = Modifier.padding(start = 16.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        treatment.forEachIndexed { index, item ->
            TreatmentItemCard(item)
            if (index != treatment.lastIndex) Spacer(modifier = Modifier.height(10.dp))
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = stringResource(R.string.recovery_care),
            style = MaterialTheme.typography.TitleLarge3,
            modifier = Modifier.padding(start = 16.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        recoveryCare.forEachIndexed { index, item ->
            RecoveryItemCard(item)
            if (index != recoveryCare.lastIndex) Spacer(modifier = Modifier.height(10.dp))
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
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp),
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
            if (!item.linkText.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = item.linkText!! + " \u2193",
                    style = MaterialTheme.typography.Label4,
                    color = BlueDefault
                )
            }
        }
    }
}



@Composable
private fun RecoveryItemCard(item: RecoveryItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, bottom = 10.dp),
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
            if (!item.linkText.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = item.linkText!! + " \u2193",
                    style = MaterialTheme.typography.Body1.copy(fontWeight = FontWeight.SemiBold),
                    color = Color(0xFF1976D2)
                )
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
