package com.baothanhbin.feature.myplants.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.baothanhbin.agridoctorai.resources.R
import com.baothanhbin.core.database.model.PlantEntity
import com.baothanhbin.core.database.model.displayName
import com.baothanhbin.core.database.model.displaySubtitle
import com.baothanhbin.core.database.model.imageModel
import com.baothanhbin.core.theme.GreenSurface
import com.baothanhbin.core.theme.Subtitle
import com.baothanhbin.feature.myplants.*
import com.baothanhbin.feature.myplants.component.*
import com.baothanhbin.feature.myplants.dialog.*
import com.baothanhbin.feature.myplants.sheet.*

@Composable
fun MyPlantsContent(
    plants: List<PlantEntity>,
    onSetReminderClick: (PlantEntity) -> Unit,
    onEditClick: (PlantEntity) -> Unit,
    onDeleteClick: (PlantEntity) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (plants.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.no_plant),
                    color = Subtitle
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(plants) { plant ->
                    PlantItemCard(
                        plant = plant,
                        onSetReminderClick = { onSetReminderClick(plant) },
                        onEditClick = { onEditClick(plant) },
                        onDeleteClick = { onDeleteClick(plant) }
                    )
                }
            }
        }
    }
}
