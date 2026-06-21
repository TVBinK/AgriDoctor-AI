package com.baothanhbin.feature.myplants.sheet

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
