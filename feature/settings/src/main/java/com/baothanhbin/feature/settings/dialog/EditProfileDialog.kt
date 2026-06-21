package com.baothanhbin.feature.settings.dialog

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
import com.baothanhbin.feature.settings.*
import com.baothanhbin.feature.settings.component.*

@Composable
internal fun EditProfileDialog(
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
