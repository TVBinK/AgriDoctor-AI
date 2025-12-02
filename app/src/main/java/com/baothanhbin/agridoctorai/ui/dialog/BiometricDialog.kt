package com.baothanhbin.agridoctorai.ui.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.baothanhbin.agridoctorai.resources.R
import com.baothanhbin.core.theme.GreenSurface
import com.baothanhbin.core.theme.White

@Composable
fun BiometricLockScreen(
    message: String?,
    onRetry: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xCC000000)),
        color = Color(0x99000000)
    ) {
        val cardShape = MaterialTheme.shapes.large
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .graphicsLayer {
                        shadowElevation = 8f
                        shape = cardShape
                        clip = true
                    }
                    .background(White)
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Fingerprint,
                    contentDescription = null,
                    tint = GreenSurface,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Text(
                    text = stringResource(id = R.string.biometric_lock_required_title),
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    text = message ?: stringResource(id = R.string.biometric_lock_required_description),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = Color(0xFF6D727E)
                )
                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GreenSurface,
                        contentColor = Color.White
                    )
                ) {
                    Text(text = stringResource(id = R.string.biometric_retry))
                }
            }
        }
    }
}
