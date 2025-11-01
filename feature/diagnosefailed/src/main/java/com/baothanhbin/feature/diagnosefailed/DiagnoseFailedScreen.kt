package com.baothanhbin.feature.diagnosefailed

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.baothanhbin.agridoctorai.resources.R
import com.baothanhbin.core.theme.GreenSurface
import com.baothanhbin.core.theme.TitleLarge1
import com.baothanhbin.core.theme.White

@Composable
fun DiagnoseFailedRoute(
    navController: NavHostController,
    imageUri: Uri? = null
) {
    DiagnoseFailedScreen(
        imageUri = imageUri,
        onBack = { navController.popBackStack() },
        onTryAgain = { navController.popBackStack() }
    )
}

@Composable
fun DiagnoseFailedScreen(
    imageUri: Uri? = null,
    onBack: () -> Unit = {},
    onTryAgain: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(White)
            .padding(top = 20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    painter = painterResource(R.drawable.ic_back),
                    modifier = Modifier.size(50.dp),
                    contentDescription = stringResource(R.string.back),
                    tint = Color.Unspecified
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = stringResource(R.string.diagnose_failed),
                style = MaterialTheme.typography.TitleLarge1,
                color = GreenSurface,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Plant Image Section
        Box(
            modifier = Modifier
                .width(200.dp)
                .height(200.dp)
                .align(Alignment.CenterHorizontally),
        ) {
            // Plant image
            if (imageUri != null) {
                AsyncImage(
                    model = imageUri,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.img_ca_chua),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Error Message
        Text(
            text = stringResource(R.string.sorry_couldnt_identify),
            style = MaterialTheme.typography.bodyLarge,
            color = Color(0xFFE53935),
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Tips Description
        Text(
            text = stringResource(R.string.try_snap_tips_description),
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF1A1A1A),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Snap Tips Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(20.dp)
                        .background(GreenSurface, RoundedCornerShape(2.dp))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.snap_tips),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1A1A1A)
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SnapTipItem(
                    imageResId = R.drawable.img_good,
                    isGood = true,
                    label = "",
                    modifier = Modifier.size(120.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tips Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterHorizontally)
            ) {
                // Too Close
                SnapTipItem(
                    imageResId = R.drawable.img_too_close,
                    isGood = false,
                    label = stringResource(R.string.too_close),
                    modifier = Modifier.size(70.dp)
                )

                SnapTipItem(
                    imageResId = R.drawable.img_too_far,
                    isGood = false,
                    label = stringResource(R.string.too_far),
                    modifier = Modifier.size(70.dp)
                )

                SnapTipItem(
                    imageResId = R.drawable.img_multi_species,
                    isGood = false,
                    label = stringResource(R.string.multi_species),
                    modifier = Modifier.size(70.dp)
                )

                SnapTipItem(
                    imageResId = R.drawable.img_too_blurry,
                    isGood = false,
                    label = stringResource(R.string.too_blurry),
                    modifier = Modifier.size(70.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Try Again Button
        Button(
            onClick = onTryAgain,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
                .height(50.dp),
            shape = RoundedCornerShape(25.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = GreenSurface
            )
        ) {
            Text(
                text = stringResource(R.string.try_again),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = White
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun SnapTipItem(
    imageResId: Int,
    isGood: Boolean,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = modifier
        ) {
            // Image - use the size from modifier parameter
            Image(
                painter = painterResource(id = imageResId),
                contentDescription = label,
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )

            // Icon overlay
            Icon(
                imageVector = if (isGood) Icons.Filled.CheckCircle else Icons.Filled.Close,
                contentDescription = null,
                tint = if (isGood) Color(0xFF4CAF50) else Color(0xFFE53935),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(if (isGood) 32.dp else 20.dp)
                    .background(
                        White,
                        CircleShape
                    )
                    .padding(if (isGood) 6.dp else 4.dp)
                    .zIndex(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            color = Color(0xFF1A1A1A),
            maxLines = 2
        )
    }
}

@Preview
@Composable
private fun DiagnoseFailedPreview() {
    DiagnoseFailedScreen()
}

