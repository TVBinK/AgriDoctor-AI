package com.baothanhbin.feature.myplants.components

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
import com.baothanhbin.core.theme.GreenSurface
import com.baothanhbin.core.theme.Subtitle
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPlantBottomSheet(
    onDismissRequest: () -> Unit,
    onAddPlant: (name: String, imageUri: String?) -> Unit
) {
    val context = LocalContext.current
    var plantName by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<String?>(null) }
    var bitmapState by remember { mutableStateOf<Bitmap?>(null) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            bitmapState = bitmap
            val tempFile = File(context.cacheDir, "plant_${UUID.randomUUID()}.jpg")
            val outputStream = FileOutputStream(tempFile)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            outputStream.flush()
            outputStream.close()
            imageUri = tempFile.absolutePath
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = Color(0xFFF9F9F4),
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp, bottom = 32.dp, start = 24.dp, end = 24.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onDismissRequest,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.Close,
                        contentDescription = stringResource(R.string.close),
                        tint = GreenSurface,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    text = stringResource(R.string.add_new_plant),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = GreenSurface
                )
                Text(
                    text = stringResource(R.string.save),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = if (plantName.isNotBlank()) GreenSurface else Subtitle,
                    modifier = Modifier
                        .clickable(enabled = plantName.isNotBlank()) { onAddPlant(plantName, imageUri) }
                        .padding(8.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Image Preview Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(4f/3f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFFE8E8E8))
            ) {
                if (bitmapState != null) {
                    androidx.compose.foundation.Image(
                        bitmap = bitmapState!!.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(stringResource(R.string.take_photo_of_your_plant), color = Subtitle, fontSize = 14.sp)
                    }
                }
                
                // Photo button overlay
                Surface(
                    color = Color.White.copy(alpha = 0.9f),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                        .clickable { takePictureLauncher.launch(null) }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(com.baothanhbin.agridoctorai.resources.R.drawable.ic_camera),
                            contentDescription = null,
                            tint = GreenSurface,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = if (bitmapState != null) stringResource(R.string.change_photo) else stringResource(R.string.add_photo),
                            color = GreenSurface,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Text field label
            Text(
                text = stringResource(R.string.plant_name),
                color = GreenSurface,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // TextField custom wrapper
            TextField(
                value = plantName,
                onValueChange = { plantName = it },
                placeholder = { Text(stringResource(R.string.your_plant_name), color = Subtitle) },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp)),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFEFEFEA),
                    unfocusedContainerColor = Color(0xFFEFEFEA),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = GreenSurface,
                    unfocusedTextColor = Subtitle
                ),
                trailingIcon = {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.Edit,
                        contentDescription = null,
                        tint = Subtitle.copy(alpha = 0.6f),
                        modifier = Modifier.size(20.dp)
                    )
                },
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Save button
            Button(
                onClick = { onAddPlant(plantName, imageUri) },
                enabled = plantName.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GreenSurface)
            ) {
                Text(
                    text = stringResource(R.string.add_plant),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.White
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Cancel button
            Box(
                modifier = Modifier.fillMaxWidth().clickable { onDismissRequest() }.padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.cancel),
                    fontWeight = FontWeight.Bold,
                    color = GreenSurface,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetReminderDialog(
    initialPlantName: String,
    availablePlants: List<PlantEntity>,
    onDismissRequest: () -> Unit,
    onSetReminder: (String, String, Long) -> Unit
) {
    val context = LocalContext.current
    var selectedTimestamp by remember { mutableStateOf<Long?>(null) }
    var dateTimeText by remember { mutableStateOf("") }
    var actionName by remember { mutableStateOf("Tưới nước") }
    var expandedActionMenu by remember { mutableStateOf(false) }
    
    // Default to the initial plant name, except if it's "Tất cả cây" then we try to pick the first one
    var selectedPlantName by remember { 
        mutableStateOf(
            if (initialPlantName == "Tất cả cây" && availablePlants.isNotEmpty()) availablePlants[0].plantName
            else initialPlantName
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
        title = { Text(stringResource(R.string.set_watering_reminder).replace("tưới nước", "chăm sóc")) },
        text = {
            Column {
                if (availablePlants.isNotEmpty()) {
                    OutlinedTextField(
                        value = selectedPlantName,
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
                    Text(stringResource(R.string.setup_watering_reminder_for, selectedPlantName), color = Subtitle)
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                // Action Type Dropdown/TextField
                androidx.compose.material3.OutlinedTextField(
                    value = actionName,
                    onValueChange = { actionName = it },
                    label = { Text("Loại nhắc nhở (Tưới nước, Bón phân...)") },
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
                    listOf("Tưới nước", "Bón phân", "Tỉa cành", "Làm cỏ", "Phun thuốc").forEach { action ->
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
                    selectedTimestamp?.let { onSetReminder(selectedPlantName, actionName.ifBlank { "Chăm sóc" }, it) }
                },
                enabled = selectedTimestamp != null,
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
                                        selectedPlantName = plant.plantName
                                        showPlantPicker = false
                                    }
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (plant.imageUri != null) {
                                    coil.compose.AsyncImage(
                                        model = File(plant.imageUri),
                                        contentDescription = plant.plantName,
                                        modifier = Modifier.size(50.dp).clip(RoundedCornerShape(8.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Image(
                                        painter = painterResource(id = R.drawable.img_lavender),
                                        contentDescription = plant.plantName,
                                        modifier = Modifier.size(50.dp).clip(RoundedCornerShape(8.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                                Spacer(Modifier.width(16.dp))
                                Column {
                                    Text(plant.plantName, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = plant.location ?: stringResource(R.string.no_location),
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPlantBottomSheet(
    plant: PlantEntity,
    onDismissRequest: () -> Unit,
    onSave: (name: String, imageUri: String?) -> Unit
) {
    val context = LocalContext.current
    var plantName by remember { mutableStateOf(plant.plantName) }
    var imageUri by remember { mutableStateOf(plant.imageUri) }
    var bitmapState by remember { mutableStateOf<Bitmap?>(null) }
    
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            bitmapState = bitmap
            val tempFile = File(context.cacheDir, "plant_${UUID.randomUUID()}.jpg")
            val outputStream = FileOutputStream(tempFile)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            outputStream.flush()
            outputStream.close()
            imageUri = tempFile.absolutePath
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = Color(0xFFF9F9F4),
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp, bottom = 32.dp, start = 24.dp, end = 24.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onDismissRequest,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.Close,
                        contentDescription = stringResource(R.string.close),
                        tint = GreenSurface,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    text = stringResource(R.string.edit_plant),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = GreenSurface
                )
                Text(
                    text = stringResource(R.string.save),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = GreenSurface,
                    modifier = Modifier.clickable { onSave(plantName, imageUri) }.padding(8.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Image Preview Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(4f/3f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFFE8E8E8))
            ) {
                if (bitmapState != null) {
                    androidx.compose.foundation.Image(
                        bitmap = bitmapState!!.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else if (!imageUri.isNullOrEmpty()) {
                    coil.compose.AsyncImage(
                        model = imageUri,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                
                // Edit Photo button overlay
                Surface(
                    color = Color.White.copy(alpha = 0.9f),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                        .clickable { takePictureLauncher.launch(null) }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(com.baothanhbin.agridoctorai.resources.R.drawable.ic_camera),
                            contentDescription = null,
                            tint = GreenSurface,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.change_photo),
                            color = GreenSurface,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Text field label
            Text(
                text = stringResource(R.string.plant_name),
                color = GreenSurface,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // TextField custom wrapper
            TextField(
                value = plantName,
                onValueChange = { plantName = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp)),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFEFEFEA),
                    unfocusedContainerColor = Color(0xFFEFEFEA),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = GreenSurface,
                    unfocusedTextColor = Subtitle
                ),
                trailingIcon = {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.Edit,
                        contentDescription = null,
                        tint = Subtitle.copy(alpha = 0.6f),
                        modifier = Modifier.size(20.dp)
                    )
                },
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Save button
            Button(
                onClick = { onSave(plantName, imageUri) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GreenSurface)
            ) {
                Text(
                    text = stringResource(R.string.save_changes),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Cancel button
            Box(
                modifier = Modifier.fillMaxWidth().clickable { onDismissRequest() }.padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.cancel),
                    fontWeight = FontWeight.Bold,
                    color = GreenSurface,
                    fontSize = 16.sp
                )
            }
        }
    }
}
