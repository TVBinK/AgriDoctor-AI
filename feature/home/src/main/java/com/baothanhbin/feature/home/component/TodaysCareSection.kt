package com.baothanhbin.feature.home.component

import android.Manifest
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import com.baothanhbin.agridoctorai.resources.R
import com.baothanhbin.core.theme.Blue1
import com.baothanhbin.core.theme.BlueDefault
import com.baothanhbin.core.theme.Body1
import com.baothanhbin.core.theme.Body4
import com.baothanhbin.core.theme.Button1
import com.baothanhbin.core.theme.GreenSurface
import com.baothanhbin.core.theme.Label2
import com.baothanhbin.core.theme.Label4
import com.baothanhbin.core.theme.Label5
import com.baothanhbin.core.theme.Subtitle
import com.baothanhbin.core.theme.TitleLarge3
import com.baothanhbin.core.theme.White
import com.baothanhbin.core.theme.Yellow1
import com.baothanhbin.core.database.model.PlantEntity
import com.baothanhbin.core.database.model.ReminderEntity
import com.baothanhbin.core.database.model.displayName
import com.baothanhbin.core.database.model.imageModel
import com.baothanhbin.core.ui.dialog.LocationDialog
import com.baothanhbin.core.ui.util.LocationStateHolder
import com.baothanhbin.feature.camera.navigation.navigateToCamera
import com.baothanhbin.feature.myplants.navigation.navigateToMyplants
import com.baothanhbin.feature.chatbot.navigation.navigateToChatbot
import com.baothanhbin.feature.lightmeter.navigation.navigateToLightMeter
import com.baothanhbin.feature.settings.navigation.navigateToSettings
import kotlinx.coroutines.launch
import com.baothanhbin.feature.home.*
import com.baothanhbin.feature.home.*
import com.baothanhbin.feature.home.component.*

@Composable
internal fun TodaysCareSection(
    reminders: List<ReminderEntity>,
    plants: List<PlantEntity>,
    onToggleReminder: (Long, Boolean) -> Unit,
    onAddTaskClick: () -> Unit = {},
    onViewAllClick: () -> Unit = {}
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            stringResource(R.string.todays_care),
            style = MaterialTheme.typography.TitleLarge3
        )
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            ) {
                if (reminders.isNotEmpty()) {
                    Text(
                        "${reminders.size} ${stringResource(R.string.task)}",
                        color = GreenSurface,
                        style = MaterialTheme.typography.TitleLarge3
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    reminders.take(2).forEachIndexed { index, reminder ->
                        val plant = reminder.plantId?.let { plantId ->
                            plants.find { it.id == plantId }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = reminder.isCompleted,
                                onCheckedChange = { onToggleReminder(reminder.id, it) },
                                colors = CheckboxDefaults.colors(checkedColor = GreenSurface)
                            )
                            Spacer(Modifier.width(8.dp))
                            if (plant?.imageModel() != null) {
                                AsyncImage(
                                    model = plant.imageModel(),
                                    contentDescription = plant.displayName(),
                                    modifier = Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Image(
                                    painter = painterResource(id = R.drawable.img_lavender),
                                    contentDescription = reminder.plantName,
                                    modifier = Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(plant?.displayName() ?: reminder.plantName, style = MaterialTheme.typography.Body1)
                                Text(reminder.actionName, color = Subtitle, style = MaterialTheme.typography.Label4)
                            }
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = GreenSurface,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        if (index < reminders.take(2).size - 1) {
                            Divider(color = Color(0xFFEEEEEE))
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        stringResource(R.string.view_all),
                        color = BlueDefault,
                        modifier = Modifier.align(Alignment.CenterHorizontally).clickable { onViewAllClick() },
                        textDecoration = TextDecoration.Underline,
                        fontSize = 14.sp
                    )
                } else {
                    Text(
                        text = stringResource(R.string.tasks_count),
                        color = GreenSurface,
                        style = MaterialTheme.typography.TitleLarge3
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        stringResource(R.string.home_tasks_empty_title),
                        color = GreenSurface,
                        style = MaterialTheme.typography.TitleLarge3
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        stringResource(R.string.home_tasks_empty_description),
                        color = Subtitle,
                        style = MaterialTheme.typography.Body4
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.home_tasks_empty_suggestion_label),
                        color = Subtitle,
                        style = MaterialTheme.typography.Label4
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    ReminderSuggestions()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.home_tasks_empty_cta),
                        color = BlueDefault,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .clickable { onAddTaskClick() },
                        textDecoration = TextDecoration.Underline,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
