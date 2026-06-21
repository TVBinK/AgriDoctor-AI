package com.baothanhbin.feature.myplants.dialog

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.baothanhbin.agridoctorai.resources.R
import com.baothanhbin.core.database.model.PlantEntity
import com.baothanhbin.core.database.model.displayName
import com.baothanhbin.core.database.model.displaySubtitle
import com.baothanhbin.core.database.model.imageModel
import com.baothanhbin.core.theme.GreenSurface
import com.baothanhbin.core.theme.Subtitle
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID
import com.baothanhbin.feature.myplants.*
import com.baothanhbin.feature.myplants.component.*
import com.baothanhbin.feature.myplants.dialog.*
import com.baothanhbin.feature.myplants.sheet.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetReminderDialog(
    initialPlantName: String,
    availablePlants: List<PlantEntity>,
    onDismissRequest: () -> Unit,
    onSetReminder: (PlantEntity, String, Long) -> Unit
) {
    val context = LocalContext.current
    var selectedTimestamp by remember { mutableStateOf<Long?>(null) }
    var dateTimeText by remember { mutableStateOf("") }
    var actionName by remember { mutableStateOf(context.getString(R.string.reminder_action_watering)) }
    var expandedActionMenu by remember { mutableStateOf(false) }
    
    // Default to the initial plant name, except if it's "Tất cả cây" then we try to pick the first one
    var selectedPlant by remember {
        mutableStateOf(
            availablePlants.firstOrNull { it.plantName == initialPlantName }
                ?: availablePlants.firstOrNull { it.displayName() == initialPlantName }
                ?: availablePlants.firstOrNull()
        )
    }
    var showPlantPicker by remember { mutableStateOf(false) }
    
    val calendar = Calendar.getInstance()
    
    val datePickerDialog = DatePickerDialog(
        context,
        com.baothanhbin.agridoctorai.resources.R.style.CustomDateTimePickerTheme,
        { _, year, month, dayOfMonth ->
            TimePickerDialog(
                context,
                com.baothanhbin.agridoctorai.resources.R.style.CustomDateTimePickerTheme,
                { _, hourOfDay, minute ->
                    val cal = Calendar.getInstance()
                    cal.set(year, month, dayOfMonth, hourOfDay, minute, 0)
                    selectedTimestamp = cal.timeInMillis
                    dateTimeText = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(cal.time)
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            ).show()
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )
    
    datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000

    AlertDialog(
        onDismissRequest = onDismissRequest,
        containerColor = Color.White,
        title = { Text(stringResource(R.string.set_care_reminder)) },
        text = {
            Column {
                if (availablePlants.isNotEmpty()) {
                    OutlinedTextField(
                        value = selectedPlant?.displayName().orEmpty(),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.plant_name)) },
                        trailingIcon = {
                            Icon(
                                imageVector = androidx.compose.material.icons.Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = Subtitle,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showPlantPicker = true },
                        enabled = false,
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledBorderColor = GreenSurface,
                            disabledLabelColor = GreenSurface
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                } else {
                    Text(
                        stringResource(
                            R.string.setup_care_reminder_for,
                            selectedPlant?.displayName().orEmpty()
                        ),
                        color = Subtitle
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                // Action Type Dropdown/TextField
                androidx.compose.material3.OutlinedTextField(
                    value = actionName,
                    onValueChange = { actionName = it },
                    label = { Text(stringResource(R.string.reminder_type_label)) },
                    trailingIcon = {
                        androidx.compose.material3.IconButton(onClick = { expandedActionMenu = !expandedActionMenu }) {
                            androidx.compose.material3.Icon(
                                imageVector = androidx.compose.material.icons.Icons.Default.ArrowDropDown,
                                contentDescription = null
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GreenSurface,
                        focusedLabelColor = GreenSurface,
                        cursorColor = GreenSurface
                    )
                )
                androidx.compose.material3.DropdownMenu(
                    expanded = expandedActionMenu,
                    onDismissRequest = { expandedActionMenu = false },
                    modifier = Modifier.fillMaxWidth(0.7f).background(Color.White)
                ) {
                    listOf(
                        stringResource(R.string.reminder_action_watering),
                        stringResource(R.string.reminder_action_fertilizing),
                        stringResource(R.string.reminder_action_pruning),
                        stringResource(R.string.reminder_action_weeding),
                        stringResource(R.string.reminder_action_spraying)
                    ).forEach { action ->
                        androidx.compose.material3.DropdownMenuItem(
                            text = { Text(action, color = GreenSurface) },
                            onClick = {
                                actionName = action
                                expandedActionMenu = false
                            }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = dateTimeText,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.reminder_datetime)) },
                    placeholder = { Text(stringResource(R.string.select_time)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { datePickerDialog.show() },
                    enabled = false,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = GreenSurface,
                        disabledLabelColor = GreenSurface,
                        disabledPlaceholderColor = Subtitle
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    selectedPlant?.let { plant ->
                        selectedTimestamp?.let { timestamp ->
                            onSetReminder(
                                plant,
                                actionName.ifBlank { context.getString(R.string.care) },
                                timestamp
                            )
                        }
                    }
                },
                enabled = selectedTimestamp != null && selectedPlant != null,
                colors = ButtonDefaults.buttonColors(containerColor = GreenSurface)
            ) {
                Text(stringResource(R.string.action_set))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismissRequest,
                colors = ButtonDefaults.textButtonColors(contentColor = Subtitle)
            ) {
                Text(stringResource(R.string.cancel))
            }
        }
    )

    if (showPlantPicker) {
        Dialog(onDismissRequest = { showPlantPicker = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.plant_name),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = GreenSurface,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    androidx.compose.foundation.lazy.LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(availablePlants.size) { index ->
                            val plant = availablePlants[index]
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedPlant = plant
                                        showPlantPicker = false
                                    }
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (plant.imageModel() != null) {
                                    coil.compose.AsyncImage(
                                        model = plant.imageModel(),
                                        contentDescription = plant.displayName(),
                                        modifier = Modifier.size(50.dp).clip(RoundedCornerShape(8.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Image(
                                        painter = painterResource(id = R.drawable.img_lavender),
                                        contentDescription = plant.displayName(),
                                        modifier = Modifier.size(50.dp).clip(RoundedCornerShape(8.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                                Spacer(Modifier.width(16.dp))
                                Column {
                                    Text(plant.displayName(), fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = plant.displaySubtitle() ?: stringResource(R.string.no_location),
                                        fontSize = 14.sp,
                                        color = Subtitle
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
