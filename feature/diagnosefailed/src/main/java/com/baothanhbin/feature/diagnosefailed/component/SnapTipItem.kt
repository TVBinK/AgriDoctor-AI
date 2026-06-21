package com.baothanhbin.feature.diagnosefailed.component

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
import com.baothanhbin.core.model.ApiType
import com.baothanhbin.core.theme.GreenSurface
import com.baothanhbin.core.theme.TitleLarge1
import com.baothanhbin.core.theme.White
import com.baothanhbin.feature.diagnosefailed.*
import com.baothanhbin.feature.diagnosefailed.*
import com.baothanhbin.feature.diagnosefailed.component.*

@Composable
internal fun SnapTipItem(
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
