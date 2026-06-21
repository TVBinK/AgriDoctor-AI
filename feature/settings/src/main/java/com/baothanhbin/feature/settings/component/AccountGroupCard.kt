package com.baothanhbin.feature.settings.component

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
import com.baothanhbin.feature.settings.*
import com.baothanhbin.feature.settings.*
import com.baothanhbin.feature.settings.component.*
import com.baothanhbin.feature.settings.dialog.*

@Composable
internal fun AccountGroupCard(
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
