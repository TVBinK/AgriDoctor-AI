package com.baothanhbin.feature.diagnose.component

import android.Manifest
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.baothanhbin.agridoctorai.resources.R
import com.baothanhbin.core.database.model.DiagnoseResultEntity
import com.baothanhbin.core.database.model.PlantEntity
import com.baothanhbin.core.database.model.displayName
import com.baothanhbin.core.theme.Body1
import com.baothanhbin.core.theme.GreenSurface
import com.baothanhbin.core.theme.Label4
import com.baothanhbin.core.theme.Subtitle
import com.baothanhbin.core.theme.TitleLarge3
import com.baothanhbin.core.theme.White
import com.baothanhbin.core.ui.dialog.LocationDialog
import com.baothanhbin.feature.camera.navigation.navigateToCamera
import com.baothanhbin.feature.diagnoseresult.navigation.navigateToDiagnoseResult
import com.baothanhbin.feature.settings.navigation.navigateToSettings
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.baothanhbin.feature.diagnose.*
import com.baothanhbin.feature.diagnose.*
import com.baothanhbin.feature.diagnose.component.*

@Composable
internal fun HistorySection(
    viewModel: DiagnoseViewModel
) {
    val historyItems by viewModel.historyItems.collectAsStateWithLifecycle()
    val plantHistoryItems by viewModel.plantHistoryItems.collectAsStateWithLifecycle()

    // Tab state
    var selectedTab by remember { mutableStateOf(0) } // 0: Disease, 1: Plant

    if (historyItems.isEmpty() && plantHistoryItems.isEmpty()) {
        return
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.health_check_history),
                style = MaterialTheme.typography.TitleLarge3,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        // Custom Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .background(Color(0xFFF0F0F0), RoundedCornerShape(8.dp))
                .padding(4.dp)
        ) {
            TabButton(
                text = stringResource(R.string.list_disease),
                selected = selectedTab == 0,
                modifier = Modifier.weight(1f),
                onClick = { selectedTab = 0 }
            )
            TabButton(
                text = stringResource(R.string.list_plant),
                selected = selectedTab == 1,
                modifier = Modifier.weight(1f),
                onClick = { selectedTab = 1 }
            )
        }

        when (selectedTab) {
            0 -> {
                // Tab Disease - hiển thị từ diagnose_results
                if (historyItems.isEmpty()) {
                    Text(
                        text = stringResource(R.string.no_disease_history),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Subtitle,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(400.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = historyItems,
                            key = { it.id }
                        ) { item ->
                            SwipeToDeleteContainer(
                                onDelete = {
                                    viewModel.deleteHistoryItem(item)
                                }
                            ) {
                                HistoryItem(
                                    entity = item,
                                    onClick = { viewModel.onHistoryItemClick(item) }
                                )
                            }
                        }
                    }
                }
            }
            1 -> {
                // Tab Plant - hiển thị từ plants
                if (plantHistoryItems.isEmpty()) {
                    Text(
                        text = stringResource(R.string.no_plant_history),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Subtitle,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(400.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = plantHistoryItems,
                            key = { it.id }
                        ) { item ->
                            SwipeToDeleteContainer(
                                onDelete = {
                                    viewModel.deletePlantHistoryItem(item)
                                }
                            ) {
                                PlantHistoryItem(
                                    entity = item,
                                    onClick = { viewModel.onPlantHistoryItemClick(item) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
