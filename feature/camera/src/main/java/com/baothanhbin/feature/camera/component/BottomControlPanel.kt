package com.baothanhbin.feature.camera.component

import android.Manifest
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.baothanhbin.agridoctorai.resources.R
import com.baothanhbin.feature.processimage.ProcessImageViewModel
import com.baothanhbin.feature.processimage.navigation.navigateToProcessImage
import com.baothanhbin.feature.camera.*
import com.baothanhbin.feature.camera.*
import com.baothanhbin.feature.camera.component.*

@Composable
internal fun BottomControlPanel(
    modifier: Modifier = Modifier,
    selectedIndex: Int,
    onOptionSelected: (Int) -> Unit,
    onTakePhoto: ((android.net.Uri) -> Unit, (String) -> Unit) -> Unit,
    onSelectFromGallery: () -> Unit
) {
    val context = LocalContext.current
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color(0xFF4CAF50),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .padding(bottom = 15.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SlidingSegmentedToggle(
                selectedIndex = selectedIndex,
                onOptionSelected = onOptionSelected,
                modifier = Modifier.fillMaxWidth(0.6f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    border = BorderStroke(2.dp, Color.White),
                    modifier = Modifier
                        .size(55.dp)
                        .clickable { onSelectFromGallery() }
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_lavender),
                        contentDescription = stringResource(R.string.picture_plant_preview),
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(Modifier.width(50.dp))

                Box(
                    contentAlignment = Alignment.Center, 
                    modifier = Modifier
                        .size(78.dp)
                        .clickable {
                            onTakePhoto(
                                { uri ->
                                    Toast.makeText(
                                        context,
                                        context.getString(R.string.photo_saved, uri.toString()),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                { error ->
                                    Toast.makeText(
                                        context,
                                        error,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            )
                        }
                ) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(Color.White, CircleShape)
                            .border(BorderStroke(2.dp, Color.White), CircleShape)
                    )
                    Box(
                        modifier = Modifier
                            .size(58.dp)
                            .border(BorderStroke(5.dp, Color(0xFF2ECC71)), CircleShape)
                    )
                }

                Spacer(Modifier.width(50.dp))

                Surface(
                    color = Color.Transparent,
                    shape = CircleShape,
                    border = BorderStroke(2.dp, Color.White),
                    modifier = Modifier.size(55.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_help),
                            contentDescription = stringResource(R.string.help),
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}
