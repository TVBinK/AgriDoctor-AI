package com.baothanhbin.feature.diagnose

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.baothanhbin.agridoctorai.resources.R
import com.baothanhbin.core.database.model.DiagnoseResultEntity
import com.baothanhbin.core.theme.Body1
import com.baothanhbin.core.theme.GreenSurface
import com.baothanhbin.core.theme.Label4
import com.baothanhbin.core.theme.Subtitle
import com.baothanhbin.core.theme.TitleLarge3
import com.baothanhbin.core.theme.White
import android.net.Uri
import android.util.Log
import androidx.compose.ui.platform.LocalContext
import coil.request.ImageRequest
import com.baothanhbin.feature.diagnoseresult.navigation.navigateToDiagnoseResult
import coil.compose.AsyncImage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DiagnoseRoute(
    navController: NavHostController? = null,
    viewModel: DiagnoseViewModel = hiltViewModel()
) {
    // Handle navigation events
    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { event ->
            when (event) {
                is DiagnoseNavigationEvent.NavigateToResult -> {
                    navController?.navigateToDiagnoseResult(
                        imageUri = event.imageUri
                    )
                }
            }
        }
    }
    
    DiagnoseScreen(
        viewModel = viewModel
    )
}

@Composable
fun DiagnoseScreen(
    viewModel: DiagnoseViewModel = hiltViewModel()
) {
    // Reload history khi screen được hiển thị lại
    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.loadHistory()
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(White)
    ) {
        Image(
            painter = painterResource(id = R.drawable.bg_diagnose),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            contentScale = ContentScale.FillWidth
        )
        DiagnoseTopBar()
        Spacer(modifier = Modifier.height(12.dp))
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 140.dp, bottom = 24.dp)
        ) {
            DiagnoseCard()
            Spacer(modifier = Modifier.height(16.dp))
            CommonProblemsSection()
            Spacer(modifier = Modifier.height(24.dp))
            HistorySection(
                viewModel = viewModel
            )
        }
    }
}

@Composable
private fun DiagnoseTopBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp)
            .padding(top = 25.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(R.drawable.ic_location),
                contentDescription = null,
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(50.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(stringResource(R.string.allow_location_tracking), style = MaterialTheme.typography.Body1)
        }
        Box(modifier = Modifier.clickable { }) {
            IconButton(onClick = { }, modifier = Modifier.size(40.dp)) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = stringResource(R.string.settings),
                    tint = GreenSurface
                )
            }
        }
    }
}

@Composable
private fun DiagnoseCard() {
    val shape = RoundedCornerShape(16.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(240.dp)
    ) {
        // Card body
        Card(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(top = 30.dp),
            shape = shape,
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(40.dp))
                Text(stringResource(R.string.plant_health), style = MaterialTheme.typography.TitleLarge3)
                Text(
                    stringResource(R.string.help_plants_get_health),
                    style = MaterialTheme.typography.Label4,
                    color = Subtitle
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {},
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_camera),
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.auto_diagnose), color = Color.White, fontSize = 12.sp)
                }
            }
        }

        // Floating flower layered above the card (not clipped)
        Image(
            painter = painterResource(id = R.drawable.ic_flower),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .size(110.dp)
                .offset(y = (-18).dp),
            contentScale = ContentScale.Fit
        )
    }
}

private data class Problem(val title: String, val imageRes: Int)

@Composable
private fun CommonProblemsSection() {
    val problems = listOf(
        Problem(stringResource(R.string.botrytis), R.drawable.img_ca_chua),
        Problem(stringResource(R.string.late_blight), R.drawable.img_late_blight),
        Problem(stringResource(R.string.leaf_miner), R.drawable.img_leaf_miner)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(stringResource(R.string.common_problems), style = MaterialTheme.typography.TitleLarge3)
        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = Subtitle)
    }

    Spacer(modifier = Modifier.height(10.dp))

    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(problems) { problem ->
            ProblemCard(problem)
        }
    }
}

@Composable
private fun ProblemCard(problem: Problem) {
    val shape = RoundedCornerShape(12.dp)
    Card(
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.width(120.dp)) {
            Image(
                painter = painterResource(id = problem.imageRes),
                contentDescription = problem.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(shape),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = problem.title,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun HistorySection(
    viewModel: DiagnoseViewModel
) {
    val historyItems by viewModel.historyItems.collectAsStateWithLifecycle()

    if (historyItems.isEmpty()) {
        return
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = stringResource(R.string.health_check_history),
            style = MaterialTheme.typography.TitleLarge3,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp), // Giới hạn chiều cao để scroll được
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(
                items = historyItems,
                key = { it.id } // Sử dụng id làm key để đảm bảo recomposition đúng
            ) { item ->
                HistoryItem(
                    entity = item,
                    onClick = { viewModel.onHistoryItemClick(item) }
                )
            }
        }
    }
}

@Composable
private fun HistoryItem(
    entity: DiagnoseResultEntity,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Image - sử dụng imageUri từ entity nếu có
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF5F5F5)),
                contentAlignment = Alignment.Center
            ) {
                entity.imageUri?.let { uriString ->
                    // Parse URI trước khi vào Composable
                    val uri = try {
                        val parsed = Uri.parse(uriString)
                        Log.d("HistoryItem", "Loading image from URI: $uriString")
                        parsed
                    } catch (e: Exception) {
                        Log.e("HistoryItem", "Error parsing URI: $uriString", e)
                        null
                    }
                    
                    if (uri != null) {
                        // Sử dụng ImageRequest.Builder với Context để đảm bảo Coil có thể đọc file local
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(uri)
                                .crossfade(true)
                                .listener(
                                    onError = { _, result ->
                                        Log.e("HistoryItem", "Error loading image from URI: $uriString", result.throwable)
                                    },
                                    onSuccess = { _, _ ->
                                        Log.d("HistoryItem", "Successfully loaded image from URI: $uriString")
                                    }
                                )
                                .build(),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop,
                            error = painterResource(id = R.drawable.img_ca_chua),
                            placeholder = painterResource(id = R.drawable.img_ca_chua)
                        )
                    } else {
                        // Fallback nếu parse URI lỗi
                        Image(
                            painter = painterResource(id = R.drawable.img_ca_chua),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                } ?: Image(
                    painter = painterResource(id = R.drawable.img_ca_chua),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Plant name - có thể extract từ diseaseName hoặc dùng default
                Text(
                    text = extractPlantName(entity.diseaseName),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Subtitle
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Disease name
                Text(
                    text = entity.diseaseName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFFF9800) // Orange color như trong hình
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Date
                Text(
                    text = formatDate(entity.timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = Subtitle
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Subtitle,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

private fun extractPlantName(diseaseName: String): String {
    // Simple extraction - có thể improve sau
    // Ví dụ: "Early blight" -> "Potato" (cần logic tốt hơn)
    // Tạm thời return default
    return "Potato" // Có thể map từ diseaseName hoặc lưu trong database
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

@Preview
@Composable
fun DiagnosePreview() {
    // Preview không cần ViewModel, chỉ hiển thị UI
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(White)
    ) {
        Image(
            painter = painterResource(id = R.drawable.bg_diagnose),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            contentScale = ContentScale.FillWidth
        )
        DiagnoseTopBar()
        Spacer(modifier = Modifier.height(12.dp))
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 140.dp, bottom = 24.dp)
        ) {
            DiagnoseCard()
            Spacer(modifier = Modifier.height(16.dp))
            CommonProblemsSection()
        }
    }
}