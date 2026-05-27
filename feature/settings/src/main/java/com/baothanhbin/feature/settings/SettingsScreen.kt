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

@Composable
private fun PremiumBanner() {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
                modifier = Modifier.fillMaxWidth(),
                contentScale = ContentScale.Crop
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.bg_follower),
                    contentDescription = null,
                    modifier = Modifier.size(90.dp),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(R.string.settings_premium_title),
                        style = MaterialTheme.typography.TitleLarge3,
                        color = Title
                    )
                    Text(
                        text = stringResource(R.string.settings_premium_subtitle),
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
private fun AccountGroupCard(
    profile: UserProfile,
    onEditClick: () -> Unit,
    onChangePasswordClick: () -> Unit
) {
    SettingsGroupCard {
        SettingRow(
            leadingIcon = Icons.Default.Person,
            title = stringResource(R.string.settings_account_information),
            value = profile.name,
            showChevron = false
        )
        DividerSpacer()
        SettingRow(
            leadingIcon = Icons.AutoMirrored.Filled.Message,
            title = stringResource(R.string.settings_email),
            value = profile.email,
            showChevron = false
        )
        DividerSpacer()
        SettingRow(
            leadingIcon = Icons.Default.Phone,
            title = stringResource(R.string.settings_phone),
            value = profile.phone.ifBlank { stringResource(R.string.settings_not_updated) },
            showChevron = false
        )
        DividerSpacer()
        SettingRow(
            leadingIcon = Icons.Default.Home,
            title = stringResource(R.string.settings_address),
            value = profile.address.ifBlank { stringResource(R.string.settings_not_updated) },
            showChevron = false
        )
        DividerSpacer()
        SettingRow(
            leadingIcon = Icons.Default.Edit,
            title = stringResource(R.string.settings_update_profile),
            value = stringResource(R.string.settings_update_profile_description),
            onClick = onEditClick
        )
        DividerSpacer()
        SettingRow(
            leadingIcon = Icons.Default.Lock,
            title = stringResource(R.string.settings_change_password),
            value = stringResource(R.string.settings_change_password_description),
            onClick = onChangePasswordClick
        )
    }
}

@Composable
private fun LanguageSelectionDialog(
    currentLanguageTag: String,
    onDismiss: () -> Unit,
    onSelectLanguage: (String) -> Unit
) {
    val languages = listOf(
        "vi" to stringResource(R.string.settings_language_vietnamese),
        "en" to stringResource(R.string.settings_language_english)
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(30.dp),
            colors = CardDefaults.cardColors(containerColor = White),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp, vertical = 22.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.settings_language_dialog_title),
                            style = MaterialTheme.typography.TitleLarge2,
                            color = Title
                        )
                        Text(
                            text = stringResource(R.string.settings_language_dialog_description),
                            style = MaterialTheme.typography.Body3,
                            color = Subtitle
                        )
                    }
                    TextButton(
                        onClick = onDismiss,
                        colors = ButtonDefaults.textButtonColors(contentColor = Subtitle)
                    ) {
                        Text(
                            text = stringResource(R.string.cancel),
                            style = MaterialTheme.typography.Button1
                        )
                    }
                }
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    languages.forEach { (tag, label) ->
                        val isSelected = currentLanguageTag == tag
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectLanguage(tag) },
                            shape = RoundedCornerShape(22.dp),
                            border = BorderStroke(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) {
                                    GreenSurface.copy(alpha = 0.42f)
                                } else {
                                    GreenSurface.copy(alpha = 0.12f)
                                }
                            ),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) Green1 else White
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.Body2,
                                        color = Title
                                    )
                                    Text(
                                        text = tag.uppercase(),
                                        style = MaterialTheme.typography.Label2,
                                        color = if (isSelected) GreenSurface else Subtitle
                                    )
                                }
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { onSelectLanguage(tag) },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = GreenSurface,
                                        unselectedColor = Subtitle.copy(alpha = 0.6f)
                                    )
                                )
                            }
                        }
                    }
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
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(24.dp)
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
            .background(GreenSurface.copy(alpha = 0.16f))
    )
}

@Composable
private fun SettingRow(
    leadingIcon: ImageVector,
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
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = leadingIcon,
            contentDescription = null,
            tint = GreenSurface
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
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

@Composable
private fun EditProfileDialog(
    profile: UserProfile,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (String, String, String) -> Unit
) {
    var name by rememberSaveable { mutableStateOf(profile.name) }
    var phone by rememberSaveable { mutableStateOf(profile.phone) }
    var address by rememberSaveable { mutableStateOf(profile.address) }
    var localError by rememberSaveable { mutableStateOf<String?>(null) }
    val dialogTitle = stringResource(R.string.settings_edit_profile_dialog_title)
    val dialogSubtitle = stringResource(R.string.settings_edit_profile_dialog_description)
    val confirmText = stringResource(R.string.save)
    val requiredNameError = stringResource(R.string.settings_name_required)
    val invalidPhoneError = stringResource(R.string.settings_phone_invalid)
    val nameLabel = stringResource(R.string.settings_field_name)
    val phoneLabel = stringResource(R.string.settings_field_phone)
    val addressLabel = stringResource(R.string.settings_field_address)

    SettingsFormDialog(
        onDismissRequest = onDismiss,
        title = dialogTitle,
        subtitle = dialogSubtitle,
        isSubmitting = isSubmitting,
        confirmText = confirmText,
        onConfirm = {
            when {
                name.isBlank() -> localError = requiredNameError
                phone.isNotBlank() && !Regex("^[0-9+\\-\\s]{9,15}$").matches(phone.trim()) ->
                    localError = invalidPhoneError
                else -> {
                    localError = null
                    onSubmit(name.trim(), phone.trim(), address.trim())
                }
            }
        }
    ) {
        DialogTextField(
            label = nameLabel,
            value = name,
            onValueChange = {
                name = it
                localError = null
            }
        )
        DialogTextField(
            label = phoneLabel,
            value = phone,
            onValueChange = {
                phone = it
                localError = null
            },
            keyboardType = KeyboardType.Phone
        )
        DialogTextField(
            label = addressLabel,
            value = address,
            onValueChange = {
                address = it
                localError = null
            }
        )
        localError?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.Label2,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
private fun ChangePasswordDialog(
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (String, String, String) -> Unit
) {
    var currentPassword by rememberSaveable { mutableStateOf("") }
    var newPassword by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var localError by rememberSaveable { mutableStateOf<String?>(null) }
    val dialogTitle = stringResource(R.string.settings_change_password_dialog_title)
    val dialogSubtitle = stringResource(R.string.settings_change_password_dialog_description)
    val confirmText = stringResource(R.string.settings_confirm)
    val requiredPasswordError = stringResource(R.string.settings_password_required)
    val passwordMismatchError = stringResource(R.string.settings_password_mismatch)
    val currentPasswordLabel = stringResource(R.string.settings_field_current_password)
    val newPasswordLabel = stringResource(R.string.settings_field_new_password)
    val confirmPasswordLabel = stringResource(R.string.settings_field_confirm_password)

    SettingsFormDialog(
        onDismissRequest = onDismiss,
        title = dialogTitle,
        subtitle = dialogSubtitle,
        isSubmitting = isSubmitting,
        confirmText = confirmText,
        onConfirm = {
            when {
                currentPassword.isBlank() || newPassword.isBlank() || confirmPassword.isBlank() ->
                    localError = requiredPasswordError
                newPassword != confirmPassword ->
                    localError = passwordMismatchError
                else -> {
                    localError = null
                    onSubmit(currentPassword, newPassword, confirmPassword)
                }
            }
        }
    ) {
        DialogTextField(
            label = currentPasswordLabel,
            value = currentPassword,
            onValueChange = {
                currentPassword = it
                localError = null
            },
            visualTransformation = PasswordVisualTransformation()
        )
        DialogTextField(
            label = newPasswordLabel,
            value = newPassword,
            onValueChange = {
                newPassword = it
                localError = null
            },
            visualTransformation = PasswordVisualTransformation()
        )
        DialogTextField(
            label = confirmPasswordLabel,
            value = confirmPassword,
            onValueChange = {
                confirmPassword = it
                localError = null
            },
            visualTransformation = PasswordVisualTransformation()
        )
        localError?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.Label2,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
private fun SettingsFormDialog(
    onDismissRequest: () -> Unit,
    title: String,
    subtitle: String,
    isSubmitting: Boolean,
    confirmText: String,
    onConfirm: () -> Unit,
    content: @Composable () -> Unit
) {
    val focusManager = LocalFocusManager.current

    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(30.dp),
            colors = CardDefaults.cardColors(containerColor = White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 30.dp, vertical = 28.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.TitleLarge2,
                    color = Title
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.Body3,
                    color = Subtitle,
                    modifier = Modifier.padding(top = 8.dp, end = 12.dp)
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 22.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    content()
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 28.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(
                        enabled = !isSubmitting,
                        onClick = onDismissRequest
                    ) {
                        Text(
                            text = stringResource(R.string.cancel),
                            style = MaterialTheme.typography.Button1,
                            color = Subtitle
                        )
                    }
                    Button(
                        enabled = !isSubmitting,
                        onClick = {
                            focusManager.clearFocus()
                            onConfirm()
                        },
                        shape = RoundedCornerShape(999.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GreenSurface,
                            contentColor = White,
                            disabledContainerColor = GreenSurface.copy(alpha = 0.45f),
                            disabledContentColor = White.copy(alpha = 0.85f)
                        )
                    ) {
                        Text(
                            text = confirmText,
                            style = MaterialTheme.typography.Button1,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DialogTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text,
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.Label2,
            color = Subtitle,
            modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
        )
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(999.dp),
            textStyle = MaterialTheme.typography.Body2.copy(color = Title),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            visualTransformation = visualTransformation,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Green1,
                unfocusedContainerColor = Green1,
                disabledContainerColor = Green1,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                focusedTextColor = Title,
                unfocusedTextColor = Title,
                disabledTextColor = Title,
                cursorColor = GreenSurface
            )
        )
    }
}

@Preview
@Composable
fun SettingsScreenPreview() {
    SettingsScreen(
        uiState = SettingsUiState(
            isLoading = false,
            currentLanguageTag = "vi",
            profile = UserProfile(
                userId = "1",
                name = "Trinh Van Binh",
                email = "binh@example.com",
                phone = "0912345678",
                address = "Ha Noi"
            )
        ),
        snackbarHostState = SnackbarHostState(),
        onBackClick = {},
        onRefresh = {},
        onChangeLanguage = {},
        onClearCache = {},
        onUpdateProfile = { _, _, _ -> },
        onChangePassword = { _, _, _ -> },
        onLogoutClick = {}
    )
}

@Composable
private fun formatCacheSize(sizeBytes: Long): String {
    if (sizeBytes <= 0L) {
        return "0 ${stringResource(R.string.settings_cache_unit_b)}"
    }
    val units = listOf(
        stringResource(R.string.settings_cache_unit_b),
        stringResource(R.string.settings_cache_unit_kb),
        stringResource(R.string.settings_cache_unit_mb),
        stringResource(R.string.settings_cache_unit_gb)
    )
    var value = sizeBytes.toDouble()
    var unitIndex = 0
    while (value >= 1024 && unitIndex < units.lastIndex) {
        value /= 1024
        unitIndex++
    }
    return if (unitIndex == 0) {
        "${value.toLong()} ${units[unitIndex]}"
    } else {
        String.format(Locale.getDefault(), "%.1f %s", value, units[unitIndex])
    }
}


