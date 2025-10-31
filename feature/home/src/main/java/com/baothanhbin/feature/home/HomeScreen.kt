package com.baothanhbin.feature.home

import android.net.Uri
import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

@Composable
fun HomeRoute() {
    HomeScreen()
}

@Composable
fun HomeScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(White)
    ) {
        Image(
            painter = painterResource(id = R.drawable.bg_home),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            contentScale = ContentScale.FillWidth
        )
        HomeTopBar()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 140.dp, bottom = 24.dp)
        ) {
            QuickActionsSection()
            Spacer(modifier = Modifier.height(20.dp))
            TodaysCareSection()
            Spacer(modifier = Modifier.height(20.dp))
            MeasurementToolsSection()
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun QuickActionsSection() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        HomeFeatureCard(
            title = "Identify Plant",
            subtitle = "Scan to identify",
            color = Color(0xFFE8F5E9),
            icon = R.drawable.ic_camera,
            backgroundRes = R.drawable.bg_home_identify,
            modifier = Modifier.weight(1f)
        )
        HomeFeatureCard(
            title = "Diagnose",
            subtitle = "Check your\nplant's heath",
            color = Color(0xFFF1F8E9),
            icon = R.drawable.ic_selected_diagnose,
            backgroundRes = R.drawable.bg_home_diagnose,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun TodaysCareSection() {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            "Today's Care",
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
                Text(
                    "0 Tasks",
                    color = GreenSurface,
                    style = MaterialTheme.typography.TitleLarge3
                )
                Spacer(modifier = Modifier.height(5.dp))
                Text(
                    "View All",
                    color = BlueDefault,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    textDecoration = TextDecoration.Underline
                )
            }
        }
    }
}

@Composable
private fun MeasurementToolsSection() {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text("Measurement Tools", style = MaterialTheme.typography.TitleLarge3)
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MeasurementToolCard(
                title = "Water Meter",
                description = "Optimize watering for your plant",
                icon = R.drawable.ic_water,
                background = Blue1,
                modifier = Modifier.weight(1f)
            )
            MeasurementToolCard(
                title = "Light Meter",
                description = "Measure light intensity",
                icon = R.drawable.ic_light,
                background = Yellow1,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        ChatCard(
            title = "Need Plant Help? Ask AI",
            description = "Get instant advice now!",
            background = Color.White,
            animatedRaw = R.raw.ic_tinh_linh
        )
    }
}

@Composable
fun HomeTopBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp)
            .padding(top = 25.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.height(64.dp)) {
            Icon(
                painter = painterResource(R.drawable.ic_location),
                contentDescription = null,
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(50.dp)
            )
            Spacer(modifier = Modifier.width(15.dp))
            Text(
                "Allow location tracking", style = MaterialTheme.typography.Body1
            )
        }
        Box(modifier = Modifier.clickable { }) {
            IconButton(onClick = { }, modifier = Modifier.size(40.dp)) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = GreenSurface
                )
            }
        }
    }
}

@Composable
fun HomeFeatureCard(
    title: String,
    subtitle: String,
    color: Color,
    icon: Int,
    backgroundRes: Int? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(110.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            backgroundRes?.let {
                Image(
                    painter = painterResource(id = it),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            ) {
                Icon(
                    painter = painterResource(id = icon),
                    contentDescription = null,
                    tint = Color(0xFF2E7D32),
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.height(5.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, style = MaterialTheme.typography.Body1)
                    Text(subtitle, style = MaterialTheme.typography.Label2, color = Subtitle)
                }
            }
        }
    }
}

@Composable
fun MeasurementToolCard(
    title: String,
    description: String,
    icon: Int,
    background: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(190.dp)
            .height(110.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            // ✅ Dòng đầu tiên: icon bên trái, badge bên phải
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Image(
                    painter = painterResource(id = icon),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                if (title == "Water Meter") {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_caculator_blue),
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_caculator_yellow),
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            // ✅ Tiêu đề
            Text(
                text = title,
                style = MaterialTheme.typography.Body4
            )
            Spacer(modifier = Modifier.height(2.dp))
            // ✅ Mô tả
            Text(
                text = description,
                style = MaterialTheme.typography.Label5,
                color = Subtitle
            )
        }
    }
}

@Composable
fun ChatCard(
    title: String, description: String, background: Color, animatedRaw: Int? = null
) {
    val shape = RoundedCornerShape(16.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .shadow(6.dp, shape = shape, clip = false)
            .border(
                width = 2.dp,
                brush = Brush.linearGradient(
                    listOf(
                        Color(0xFF00E676), // green
                        Color(0xFF00B0FF)  // blue
                    )
                ),
                shape = shape
            )
            .clip(shape)
    ) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .padding(1.5.dp),
            shape = shape,
            colors = CardDefaults.cardColors(containerColor = background),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, style = MaterialTheme.typography.Button1)
                    Text(description, style = MaterialTheme.typography.Label4, color = Subtitle)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {},
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4285F4)),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text("Chat Now", fontSize = 12.sp, color = Color.White)
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                animatedRaw?.let { GifImage(animatedRaw = it) }
            }
        }
    }
}

@Composable
private fun GifImage(animatedRaw: Int) {
    val context = LocalContext.current
    val imageLoader = remember {
        ImageLoader.Builder(context).components {
            if (Build.VERSION.SDK_INT >= 28) {
                add(ImageDecoderDecoder.Factory())
            } else {
                add(GifDecoder.Factory())
            }
        }.build()
    }

    val resourceEntryName = try {
        context.resources.getResourceEntryName(animatedRaw)
    } catch (e: Exception) {
        "ic_tinh_linh"
    }
    val gifUri = Uri.parse("android.resource://${context.packageName}/raw/$resourceEntryName")

    AsyncImage(
        model = ImageRequest.Builder(context).data(gifUri).crossfade(false).allowRgb565(false)
            .build(),
        contentDescription = null,
        modifier = Modifier.size(100.dp),
        contentScale = ContentScale.Fit,
        imageLoader = imageLoader
    )
}

@Preview
@Composable
fun HomeScreenPreview() {
    HomeScreen()
}
