package com.baothanhbin.feature.myplants.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.baothanhbin.agridoctorai.resources.R
import com.baothanhbin.core.theme.Body1
import com.baothanhbin.feature.myplants.*
import com.baothanhbin.feature.myplants.component.*
import com.baothanhbin.feature.myplants.dialog.*
import com.baothanhbin.feature.myplants.sheet.*

@Composable
fun MyPlantsTopBar(
    currentAddress: String? = null,
    onLocationClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp)
            .padding(top = 25.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .height(64.dp)
                .weight(1f)
                .clickable(
                    onClick = onLocationClick,
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                )
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_location),
                contentDescription = null,
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(50.dp)
            )
            Spacer(modifier = Modifier.width(15.dp))
            Text(
                text = currentAddress ?: stringResource(R.string.allow_location_tracking),
                style = MaterialTheme.typography.Body1,
                maxLines = 2
            )
        }
        Box {
            IconButton(onClick = onSettingsClick, modifier = Modifier.size(40.dp)) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = stringResource(R.string.settings),
                    tint = com.baothanhbin.core.theme.GreenSurface
                )
            }
        }
    }
}
