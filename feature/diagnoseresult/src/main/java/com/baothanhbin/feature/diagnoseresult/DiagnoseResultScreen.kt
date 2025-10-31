package com.baothanhbin.feature.diagnoseresult

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavHostController
import com.baothanhbin.agridoctorai.resources.R
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
    diseaseName: String = "Golden pothos",
    possibleProblems: List<String> = listOf("Irregular watering", "Nutrient deficiency"),
    symptoms: List<String> = listOf(
        "Stunted growth, dry dark spots, leaf drop, yellowing and wilting"
    ),
    causes: List<String> = listOf("Overwatering, Pathogen infection"),
    treatment: List<TreatmentItem> = listOf(
        TreatmentItem(
            title = "\uD83C\uDFE0 Use Home Remedies",
            subtitle = "Option 1: Baking Soda",
            steps = listOf(
                "Step 1: Mix the Solution",
                "In a container, combine:",
                "1 liter of clean water",
                "1 teaspoon of baking soda",
                "2–3 drops of dish soap (helps the solution stick to leaves)",
                "\uD83C\uDF31 Tip: Shake or stir well to make sure everything is dissolved."
            ),
            linkText = "More Option"
        ),
        TreatmentItem(
            title = "✂\uFE0F Remove Unhealthy Parts",
            subtitle = null,
            steps = listOf(
                "\uD83C\uDF31 Tip: Unhealthy parts can’t recover and may spread disease. Remove them promptly."
            ),
            linkText = "Detailed guide"
        )
    ),
    recoveryCare: List<RecoveryItem> = listOf(
        RecoveryItem(
            title = "Reduce Watering",
            steps = listOf(
                "Stop Watering: Allow the soil to partially dry out before resuming our recommended watering schedule",
                "2–3 time a week",
                "Loosen the Soil: Gently insert a blunt-ended wooden stick into the soil around the pot’s edge. Move the stick in circles to loosen compacted soil for better drainage. Do this every few weeks or after"
            ),
            linkText = null
        )
    )
) {
    DiagnoseResultScreen(
        diseaseName = diseaseName,
        possibleProblems = possibleProblems,
        symptoms = symptoms,
        causes = causes,
        treatment = treatment,
        recoveryCare = recoveryCare,
        onBack = { navController?.popBackStack() }
    )
}

@Composable
fun DiagnoseResultScreen(
    diseaseName: String,
    possibleProblems: List<String>,
    symptoms: List<String>,
    causes: List<String>,
    treatment: List<TreatmentItem>,
    recoveryCare: List<RecoveryItem>,
    onBack: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(White)
            .padding(bottom = 16.dp)
    ) {
        ContentSection(
            onBack = onBack,
            diseaseName = diseaseName,
            possibleProblems = possibleProblems,
            symptoms = symptoms,
            causes = causes,
            treatment = treatment,
            recoveryCare = recoveryCare
        )
    }
}

@Composable
private fun HeaderImage(onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(270.dp)
    ) {
        IconButton(
            onClick = onBack, modifier = Modifier
                .zIndex(1f)
                .padding(top = 10.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_back),
                contentDescription = "Back",
                tint = Color.Unspecified,
                modifier = Modifier.size(50.dp)
            )
        }
        Image(
            painter = painterResource(id = R.drawable.img_ca_chua),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // simple bounding box
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(110.dp, 70.dp)
                .border(2.dp, Color(0xFFE53935))
        )
    }
}

@Composable
private fun ContentSection(
    onBack: () -> Unit,
    diseaseName: String,
    possibleProblems: List<String>,
    symptoms: List<String>,
    causes: List<String>,
    treatment: List<TreatmentItem>,
    recoveryCare: List<RecoveryItem>
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        HeaderImage(onBack = {})
        Spacer(modifier = Modifier.height(8.dp))
        //Tên bệnh
        Text(
            text = diseaseName,
            style = MaterialTheme.typography.TitleLarge1,
            modifier = Modifier
                .padding(start = 16.dp)
                .align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = "Possible Problems",
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

        PillSectionCard(title = "Symptoms", items = symptoms)

        PillSectionCard(title = "Causes of Disease", items = causes)

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Treatment",
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
            text = "Recovery Care",
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
private fun PillSectionCard(title: String, items: List<String>) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(title, style = MaterialTheme.typography.TitleLarge3)
            Spacer(modifier = Modifier.height(8.dp))
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items.forEach { text ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
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
        }
}

data class TreatmentItem(
    val title: String,
    val subtitle: String? = null,
    val steps: List<String>,
    val linkText: String? = null
)

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

data class RecoveryItem(
    val title: String,
    val steps: List<String>,
    val linkText: String? = null
)

@Composable
private fun RecoveryItemCard(item: RecoveryItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 10.dp),
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
    DiagnoseResultRoute()
}