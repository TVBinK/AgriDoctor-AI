package com.baothanhbin.feature.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.baothanhbin.agridoctorai.resources.R
import com.baothanhbin.core.model.UserProfile
import com.baothanhbin.core.theme.Blue1
import com.baothanhbin.core.theme.Body2
import com.baothanhbin.core.theme.Body3
import com.baothanhbin.core.theme.Body4
import com.baothanhbin.core.theme.Button1
import com.baothanhbin.core.theme.Green1
import com.baothanhbin.core.theme.GreenSurface
import com.baothanhbin.core.theme.Label2
import com.baothanhbin.core.theme.Subtitle
import com.baothanhbin.core.theme.Title
import com.baothanhbin.core.theme.TitleLarge2
import com.baothanhbin.core.theme.TitleLarge3
import com.baothanhbin.core.theme.White
import java.util.Locale
import com.baothanhbin.feature.settings.component.*
import com.baothanhbin.feature.settings.dialog.*

@Composable
fun SettingsRoute(
    onBackClick: () -> Unit,
    onSessionExpired: () -> Unit = onBackClick,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error, uiState.message) {
        uiState.error?.let { snackbarHostState.showSnackbar(it) }
        uiState.message?.let { snackbarHostState.showSnackbar(it) }
        if (uiState.error != null || uiState.message != null) {
            viewModel.clearFeedback()
        }
    }

    LaunchedEffect(uiState.shouldLogout) {
        if (uiState.shouldLogout) {
            viewModel.consumeLogoutRequired()
            onSessionExpired()
        }
    }

    SettingsScreen(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onBackClick = onBackClick,
        onRefresh = viewModel::loadProfile,
        onChangeLanguage = viewModel::changeLanguage,
        onClearCache = viewModel::clearCache,
        onUpdateProfile = viewModel::updateProfile,
        onChangePassword = viewModel::changePassword,
        onLogoutClick = viewModel::logout
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    snackbarHostState: SnackbarHostState,
    onBackClick: () -> Unit,
    onRefresh: () -> Unit,
    onChangeLanguage: (String) -> Unit,
    onClearCache: () -> Unit,
    onUpdateProfile: (String, String, String) -> Unit,
    onChangePassword: (String, String, String) -> Unit,
    onLogoutClick: () -> Unit
) {
    var showEditDialog by rememberSaveable { mutableStateOf(false) }
    var showChangePasswordDialog by rememberSaveable { mutableStateOf(false) }
    var showLanguageDialog by rememberSaveable { mutableStateOf(false) }
    Scaffold(
        containerColor = Green1,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Green1)
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Green1)
                    .padding(horizontal = 16.dp, vertical = 24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
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
                        text = stringResource(R.string.settings),
                        style = MaterialTheme.typography.TitleLarge3,
                        color = GreenSurface
                    )
                }
                PremiumBanner()
                when {
                    uiState.isLoading -> {
                        SettingsGroupCard {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = GreenSurface)
                            }
                        }
                    }
                    uiState.profile == null -> {
                        SettingsGroupCard {
                            SettingRow(
                                leadingIcon = Icons.Default.Person,
                                title = stringResource(R.string.settings_account_load_failed),
                                value = stringResource(R.string.settings_tap_to_retry),
                                onClick = onRefresh
                            )
                        }
                    }
                    else -> {
                        AccountGroupCard(
                            profile = uiState.profile,
                            onEditClick = { showEditDialog = true },
                            onChangePasswordClick = { showChangePasswordDialog = true }
                        )
                    }
                }
                SettingsGroupCard {
                    SettingRow(
                        leadingIcon = Icons.Default.Language,
                        title = stringResource(R.string.settings_language),
                        value = if (uiState.currentLanguageTag == "en") {
                            stringResource(R.string.settings_language_english)
                        } else {
                            stringResource(R.string.settings_language_vietnamese)
                        },
                        onClick = { showLanguageDialog = true }
                    )
                    DividerSpacer()
                    SettingRow(
                        leadingIcon = Icons.Default.Delete,
                        title = stringResource(R.string.settings_clear_caches),
                        value = formatCacheSize(uiState.cacheSizeBytes),
                        onClick = onClearCache
                    )
                }
                SettingsGroupCard {
                    SettingRow(
                        leadingIcon = Icons.AutoMirrored.Filled.Message,
                        title = stringResource(R.string.settings_feedback),
                        value = null,
                        onClick = {}
                    )
                    DividerSpacer()
                    SettingRow(
                        leadingIcon = Icons.Default.PrivacyTip,
                        title = stringResource(R.string.settings_privacy_policy),
                        value = null,
                        onClick = {}
                    )
                    DividerSpacer()
                    SettingRow(
                        leadingIcon = Icons.Default.Settings,
                        title = stringResource(R.string.settings_contact),
                        value = null,
                        onClick = {}
                    )
                    DividerSpacer()
                    SettingRow(
                        leadingIcon = Icons.Default.RateReview,
                        title = stringResource(R.string.settings_rate_app),
                        value = null,
                        onClick = {}
                    )
                }
                SettingsGroupCard {
                    SettingRow(
                        leadingIcon = Icons.AutoMirrored.Filled.ExitToApp,
                        title = stringResource(R.string.settings_logout),
                        value = null,
                        onClick = onLogoutClick,
                        showChevron = false
                    )
                }
            }
        }
    }
    if (showEditDialog && uiState.profile != null) {
        EditProfileDialog(
            profile = uiState.profile,
            isSubmitting = uiState.isSubmitting,
            onDismiss = { showEditDialog = false },
            onSubmit = { name, phone, address ->
                onUpdateProfile(name, phone, address)
                showEditDialog = false
            }
        )
    }
    if (showChangePasswordDialog) {
        ChangePasswordDialog(
            isSubmitting = uiState.isSubmitting,
            onDismiss = { showChangePasswordDialog = false },
            onSubmit = { currentPassword, newPassword, confirmPassword ->
                onChangePassword(currentPassword, newPassword, confirmPassword)
                showChangePasswordDialog = false
            }
        )
    }
    if (showLanguageDialog) {
        LanguageSelectionDialog(
            currentLanguageTag = uiState.currentLanguageTag,
            onDismiss = { showLanguageDialog = false },
            onSelectLanguage = { languageTag ->
                onChangeLanguage(languageTag)
                showLanguageDialog = false
            }
        )
    }
}
