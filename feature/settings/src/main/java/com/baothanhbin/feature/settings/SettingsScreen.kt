package com.baothanhbin.feature.settings

import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.baothanhbin.agridoctorai.resources.R
import com.baothanhbin.core.theme.Blue1
import com.baothanhbin.core.theme.GreenSurface
import com.baothanhbin.core.theme.Subtitle
import com.baothanhbin.core.theme.Title
import com.baothanhbin.core.theme.TitleLarge3
import com.baothanhbin.core.theme.White
import com.baothanhbin.core.theme.Body4
import com.baothanhbin.core.theme.Label2
import androidx.compose.material.icons.automirrored.filled.ExitToApp

@Composable
fun SettingsRoute(
    onBackClick: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    SettingsScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onToggleBiometric = { enabled ->
            viewModel.setBiometricLockEnabled(enabled)
        },
        onLogoutClick = {
            viewModel.logout()
        }
    )
}


data class SettingsUiState(
    val biometricLockEnabled: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    onBackClick: () -> Unit,
    onToggleBiometric: (Boolean) -> Unit,
    onLogoutClick: () -> Unit
) {
    val context = LocalContext.current
    val isPreview = LocalInspectionMode.current
    val biometricManager = if (!isPreview) BiometricManager.from(context) else null
    val verticalPadding = 24.dp

    val notEnrolledMessage = stringResource(id = R.string.biometric_not_enrolled)
    val notSupportedMessage = stringResource(id = R.string.biometric_not_supported)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Blue1)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Blue1)
                .padding(horizontal = 16.dp, vertical = verticalPadding)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            /* Image(
                 painter = painterResource(id = R.drawable.bg_home),
                 contentDescription = null,
                 modifier = Modifier
                     .fillMaxWidth()
                     .wrapContentHeight(),
                 contentScale = ContentScale.FillWidth
             )*/
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null,
                        tint = GreenSurface
                    )
                }
                Text(
                    text = stringResource(id = R.string.settings),
                    style = MaterialTheme.typography.TitleLarge3,
                    color = GreenSurface
                )
            }
            PremiumBanner()

            SettingsGroupCard {
                SettingRow(
                    leadingIcon = Icons.Default.Language,
                    title = stringResource(id = R.string.settings_language),
                    value = stringResource(id = R.string.settings_language_value),
                    onClick = { /* TODO: open language screen */ }
                )
                DividerSpacer()
                SettingRow(
                    leadingIcon = Icons.Default.Delete,
                    title = stringResource(id = R.string.settings_clear_caches),
                    value = stringResource(id = R.string.settings_clear_caches_value),
                    onClick = { /* TODO: clear cache */ }
                )
                DividerSpacer()
                // Fingerprint / biometric row
                SettingRow(
                    leadingIcon = Icons.Default.Fingerprint,
                    title = stringResource(id = R.string.biometric_lock_title),
                    value = null,
                    showChevron = false,
                    trailing = {
                        Switch(
                            checked = uiState.biometricLockEnabled,
                            onCheckedChange = { isChecked ->
                                if (isChecked) {
                                    if (biometricManager == null) {
                                        // Preview mode: chỉ bật/tắt UI, không gọi hệ thống sinh trắc
                                        onToggleBiometric(true)
                                    } else {
                                        when (
                                            biometricManager.canAuthenticate(
                                                BiometricManager.Authenticators.BIOMETRIC_STRONG or
                                                        BiometricManager.Authenticators.BIOMETRIC_WEAK
                                            )
                                        ) {
                                            BiometricManager.BIOMETRIC_SUCCESS -> onToggleBiometric(
                                                true
                                            )

                                            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                                                Toast.makeText(
                                                    context,
                                                    notEnrolledMessage,
                                                    Toast.LENGTH_LONG
                                                ).show()
                                                onToggleBiometric(false)
                                            }

                                            else -> {
                                                Toast.makeText(
                                                    context,
                                                    notSupportedMessage,
                                                    Toast.LENGTH_LONG
                                                ).show()
                                                onToggleBiometric(false)
                                            }
                                        }
                                    }
                                } else {
                                    onToggleBiometric(false)
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = White,
                                checkedTrackColor = GreenSurface
                            )
                        )
                    }
                )
            }

            SettingsGroupCard {
                SettingRow(
                    leadingIcon = Icons.Default.Message,
                    title = stringResource(id = R.string.settings_feedback),
                    value = null,
                    onClick = { /* TODO */ }
                )
                DividerSpacer()
                SettingRow(
                    leadingIcon = Icons.Default.PrivacyTip,
                    title = stringResource(id = R.string.settings_privacy_policy),
                    value = null,
                    onClick = { /* TODO */ }
                )
                DividerSpacer()
                SettingRow(
                    leadingIcon = Icons.Default.Settings,
                    title = stringResource(id = R.string.settings_contact),
                    value = null,
                    onClick = { /* TODO */ }
                )
                DividerSpacer()
                SettingRow(
                    leadingIcon = Icons.Default.RateReview,
                    title = stringResource(id = R.string.settings_rate_app),
                    value = null,
                    onClick = { /* TODO */ }
                )
            }
            
            SettingsGroupCard {
                SettingRow(
                    leadingIcon = androidx.compose.material.icons.Icons.AutoMirrored.Filled.ExitToApp,
                    title = stringResource(id = R.string.settings_logout),
                    value = null,
                    onClick = onLogoutClick,
                    showChevron = false
                )
            }
        }
    }
}

@Composable
private fun PremiumBanner() {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.bg_setting),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth(),
                contentScale = ContentScale.Crop
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.bg_follower),
                    contentDescription = stringResource(id = R.string.settings_premium_illustration_cd),
                    modifier = Modifier.size(90.dp),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(id = R.string.settings_premium_title),
                        style = MaterialTheme.typography.TitleLarge3,
                        color = Title
                    )
                    Text(
                        text = stringResource(id = R.string.settings_premium_subtitle),
                        style = MaterialTheme.typography.Body4,
                        color = Subtitle,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsGroupCard(
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            content = content
        )
    }
}

@Composable
private fun DividerSpacer() {
    Spacer(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(Blue1.copy(alpha = 0.4f))
    )
}

@Composable
private fun SettingRow(
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String? = null,
    showChevron: Boolean = true,
    trailing: (@Composable () -> Unit)? = null,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
            .clickable(enabled = trailing == null, onClick = onClick),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        Icon(
            imageVector = leadingIcon,
            contentDescription = null,
            tint = GreenSurface
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.Body4,
                color = Title
            )
            value?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.Label2,
                    color = Subtitle
                )
            }
        }
        when {
            trailing != null -> trailing()
            showChevron -> {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = Subtitle
                )
            }
        }
    }
}

@Preview
@Composable
fun SettingsScreenPreview() {
    SettingsScreen(
        uiState = SettingsUiState(
            biometricLockEnabled = true
        ),
        onBackClick = {},
        onToggleBiometric = {},
        onLogoutClick = {}
    )
}

