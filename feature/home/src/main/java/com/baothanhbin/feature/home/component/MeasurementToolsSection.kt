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
internal fun MeasurementToolsSection(
    navController: NavController? = null,
    onNavigateToChatbot: (() -> Unit)? = null
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        // Tạm ẩn tính năng máy đo nước và máy đo ánh sáng
        /*
        Text(stringResource(R.string.measurement_tools), style = MaterialTheme.typography.TitleLarge3)
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MeasurementToolCard(
                title = stringResource(R.string.water_meter),
                description = stringResource(R.string.optimize_watering),
                icon = R.drawable.ic_water,
                background = Blue1,
                modifier = Modifier.weight(1f),
                onClick = {

                }
            )
            MeasurementToolCard(
                title = stringResource(R.string.light_meter),
                description = stringResource(R.string.measure_light_intensity),
                icon = R.drawable.ic_light,
                background = Yellow1,
                modifier = Modifier.weight(1f),
                onClick = {
                    navController?.navigateToLightMeter()
                }
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        */
        ChatCard(
            title = stringResource(R.string.need_plant_help),
            description = stringResource(R.string.get_instant_advice),
            background = Color.White,
            animatedRaw = R.raw.ic_tinh_linh,
            onChatNowClick = {
                navController?.navigateToChatbot(
                    navOptions = androidx.navigation.navOptions {
                        popUpTo(com.baothanhbin.feature.home.navigation.HOME_ROUTE) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                ) ?: onNavigateToChatbot?.invoke()
            }
        )
    }
}
