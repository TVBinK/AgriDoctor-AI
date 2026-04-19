package com.baothanhbin.feature.myplants.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.Image
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.baothanhbin.agridoctorai.resources.R
import com.baothanhbin.core.database.model.PlantEntity
import com.baothanhbin.core.database.model.ReminderEntity
import com.baothanhbin.core.database.model.displayName
import com.baothanhbin.core.database.model.imageModel
import com.baothanhbin.core.theme.GreenSurface
import com.baothanhbin.core.theme.Subtitle
import coil.compose.AsyncImage
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun ReminderContent(
    reminders: List<ReminderEntity>,
    plants: List<PlantEntity>,
    onAddReminderClick: () -> Unit,
    onToggleReminderStatus: (Long, Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(White)
    ) {
        // Top section: Add Reminder Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.End
        ) {
            Surface(
                color = Color(0xFFE8F5E9), // Light green background
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.clickable { onAddReminderClick() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(R.string.add_reminder),
                        tint = GreenSurface,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.add_reminder),
                        style = MaterialTheme.typography.bodyMedium,
                        color = GreenSurface,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Calendar Strip
        val todayStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().time) }
        var selectedDateStr by remember { mutableStateOf(todayStr) }

        val days = remember {
            val list = mutableListOf<Pair<String, Pair<Int, String>>>()
            val tempCal = Calendar.getInstance()
            tempCal.add(Calendar.DAY_OF_MONTH, -2) // Start from 2 days ago
            for (i in 0..30) { // Generate next 30 days
                val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(tempCal.time)
                val date = tempCal.get(Calendar.DAY_OF_MONTH)
                val dayOfWeek = SimpleDateFormat("EEE", Locale.getDefault()).format(tempCal.time)
                list.add(dateStr to Pair(date, dayOfWeek))
                tempCal.add(Calendar.DAY_OF_MONTH, 1)
            }
            list
        }
        
        val displayYear = remember(selectedDateStr) {
            try {
                val time = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(selectedDateStr)
                SimpleDateFormat("yyyy", Locale.getDefault()).format(time!!)
            } catch (e: Exception) { "2025" }
        }
        
        val displayMonth = remember(selectedDateStr) {
            try {
                val time = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(selectedDateStr)
                SimpleDateFormat("MMM", Locale.getDefault()).format(time!!)
            } catch (e: Exception) { "Jun" }
        }

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp)
                ) {
                    Text(displayYear, color = Subtitle.copy(alpha = 0.7f), fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp)) // Giống với khoảng cách ở cột ngày
                    Text(displayMonth, color = Subtitle.copy(alpha = 0.7f), fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(14.dp)) // Bù khoảng trống 14dp phía cuối cho bằng với Box dấu chấm
                }
            }

            items(days) { (dateStr, dateInfo) ->
                val (date, dayOfWeek) = dateInfo
                val isSelected = dateStr == selectedDateStr
                Column(
                    modifier = Modifier
                        .background(
                            color = if (isSelected) GreenSurface else Color.Transparent,
                            shape = RoundedCornerShape(24.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 12.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { selectedDateStr = dateStr },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = String.format(Locale.getDefault(), "%02d", date),
                        color = if (isSelected) White else Subtitle.copy(alpha = 0.8f),
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 15.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = dayOfWeek,
                        color = if (isSelected) Color.White else Subtitle.copy(alpha = 0.8f),
                        fontSize = 13.sp
                    )
                    if (isSelected) {
                        Spacer(Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(Color.White, CircleShape)
                        )
                    } else {
                        Spacer(Modifier.height(14.dp)) // To keep alignment consistent
                    }
                }
            }
        }

        val tasksForSelectedDate = reminders.filter { task ->
            try {
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(task.targetTimestamp) == selectedDateStr
            } catch (e: Exception) { false }
        }

        if (tasksForSelectedDate.isEmpty()) {
            Spacer(modifier = Modifier.weight(0.8f))

            // Empty state
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_selected_plant),
                    contentDescription = null,
                    tint = Subtitle.copy(alpha = 0.4f),
                    modifier = Modifier.size(100.dp)
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = stringResource(R.string.no_care_tasks_today),
                    style = MaterialTheme.typography.bodyLarge,
                    color = Subtitle.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Normal
                )
            }

            Spacer(modifier = Modifier.weight(1f))
        } else {
            // Task list
            androidx.compose.foundation.lazy.LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(tasksForSelectedDate) { task ->
                    val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(task.targetTimestamp)
                    val completedText = stringResource(R.string.completed_at, timeStr)
                    val waitingText = stringResource(R.string.waiting_at, timeStr)
                    val plant = task.plantId?.let { plantId ->
                        plants.find { it.id == plantId }
                    }
                    
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = !task.isCompleted) {
                                onToggleReminderStatus(task.id, true)
                            },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (task.isCompleted) Color(0xFFECEFF1) else Color(0xFFF1F8F1)
                        ),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(if (task.isCompleted) Subtitle.copy(alpha = 0.3f) else White, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                if (plant?.imageModel() != null) {
                                    AsyncImage(
                                        model = plant.imageModel(),
                                        contentDescription = plant.displayName(),
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    val actionIconRes = reminderActionIconRes(task.actionName)
                                    if (actionIconRes != null) {
                                        Icon(
                                            painter = painterResource(actionIconRes),
                                            contentDescription = null,
                                            tint = if (task.isCompleted) White else GreenSurface,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    } else {
                                        Icon(
                                            imageVector = reminderActionIconVector(task.actionName),
                                            contentDescription = null,
                                            tint = if (task.isCompleted) White else GreenSurface,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                            }
                            Spacer(Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${task.actionName} - ${plant?.displayName() ?: task.plantName}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = if (task.isCompleted) Subtitle else GreenSurface,
                                    textDecoration = if (task.isCompleted) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = if (task.isCompleted) completedText else waitingText,
                                    fontSize = 14.sp,
                                    color = Subtitle,
                                    fontWeight = if (task.isCompleted) FontWeight.Normal else FontWeight.Medium
                                )
                            }
                            Spacer(Modifier.width(16.dp))
                            Checkbox(
                                checked = task.isCompleted,
                                onCheckedChange = if (task.isCompleted) null else { isChecked ->
                                    onToggleReminderStatus(task.id, isChecked)
                                },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = GreenSurface
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun reminderActionIconRes(actionName: String): Int? {
    val normalizedAction = actionName.lowercase(Locale.ROOT)
    return if (
        normalizedAction.contains("tuoi") ||
        normalizedAction.contains("tưới") ||
        normalizedAction.contains("water")
    ) {
        R.drawable.ic_water
    } else {
        null
    }
}

private fun reminderActionIconVector(actionName: String): ImageVector {
    val normalizedAction = actionName.lowercase(Locale.ROOT)
    return when {
        normalizedAction.contains("bon phan") ||
            normalizedAction.contains("bón phân") ||
            normalizedAction.contains("fertiliz") -> Icons.Default.LocalFlorist
        else -> Icons.Default.LocalFlorist
    }
}
