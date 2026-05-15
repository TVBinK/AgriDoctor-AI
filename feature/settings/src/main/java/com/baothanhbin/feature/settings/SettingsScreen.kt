package com.baothanhbin.feature.settings

import androidx.compose.foundation.Image
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

@Composable
fun SettingsRoute(
    onBackClick: () -> Unit,
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

    SettingsScreen(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onBackClick = onBackClick,
        onRefresh = viewModel::loadProfile,
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
    onUpdateProfile: (String, String, String) -> Unit,
    onChangePassword: (String, String, String) -> Unit,
    onLogoutClick: () -> Unit
) {
    var showEditDialog by rememberSaveable { mutableStateOf(false) }
    var showChangePasswordDialog by rememberSaveable { mutableStateOf(false) }

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
                        text = "Cài đặt",
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
                                title = "Không tải được thông tin tài khoản",
                                value = "Nhấn để thử lại",
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
                        title = "Ngôn ngữ",
                        value = "Tiếng Việt",
                        onClick = {}
                    )
                    DividerSpacer()
                    SettingRow(
                        leadingIcon = Icons.Default.Delete,
                        title = "Xóa bộ nhớ đệm",
                        value = "(0B)",
                        onClick = {}
                    )
                }

                SettingsGroupCard {
                    SettingRow(
                        leadingIcon = Icons.AutoMirrored.Filled.Message,
                        title = "Góp ý",
                        value = null,
                        onClick = {}
                    )
                    DividerSpacer()
                    SettingRow(
                        leadingIcon = Icons.Default.PrivacyTip,
                        title = "Chính sách quyền riêng tư",
                        value = null,
                        onClick = {}
                    )
                    DividerSpacer()
                    SettingRow(
                        leadingIcon = Icons.Default.Settings,
                        title = "Liên hệ",
                        value = null,
                        onClick = {}
                    )
                    DividerSpacer()
                    SettingRow(
                        leadingIcon = Icons.Default.RateReview,
                        title = "Đánh giá ứng dụng",
                        value = null,
                        onClick = {}
                    )
                }

                SettingsGroupCard {
                    SettingRow(
                        leadingIcon = Icons.AutoMirrored.Filled.ExitToApp,
                        title = "Đăng xuất",
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
                        text = "Khám phá thêm tiện ích!",
                        style = MaterialTheme.typography.TitleLarge3,
                        color = Title
                    )
                    Text(
                        text = "Mang đến trải nghiệm tốt hơn",
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
            title = "Thông tin tài khoản",
            value = profile.name,
            onClick = {}
        )
        DividerSpacer()
        SettingRow(
            leadingIcon = Icons.AutoMirrored.Filled.Message,
            title = "Email",
            value = profile.email,
            onClick = {}
        )
        DividerSpacer()
        SettingRow(
            leadingIcon = Icons.Default.Phone,
            title = "Số điện thoại",
            value = profile.phone.ifBlank { "Chưa cập nhật" },
            onClick = {}
        )
        DividerSpacer()
        SettingRow(
            leadingIcon = Icons.Default.Home,
            title = "Địa chỉ",
            value = profile.address.ifBlank { "Chưa cập nhật" },
            onClick = {}
        )
        DividerSpacer()
        SettingRow(
            leadingIcon = Icons.Default.Edit,
            title = "Cập nhật thông tin",
            value = "Chỉnh sửa hồ sơ cá nhân",
            onClick = onEditClick
        )
        DividerSpacer()
        SettingRow(
            leadingIcon = Icons.Default.Lock,
            title = "Đổi mật khẩu",
            value = "Thay đổi mật khẩu đăng nhập",
            onClick = onChangePasswordClick
        )
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

    SettingsFormDialog(
        onDismissRequest = onDismiss,
        title = "Cập nhật thông tin",
        subtitle = "Vui lòng điền thông tin mới của bạn bên dưới.",
        isSubmitting = isSubmitting,
        confirmText = "Lưu",
        onConfirm = {
            when {
                name.isBlank() -> localError = "Họ tên là trường bắt buộc."
                phone.isNotBlank() && !Regex("^[0-9+\\-\\s]{9,15}$").matches(phone.trim()) ->
                    localError = "Số điện thoại không đúng định dạng."
                else -> {
                    localError = null
                    onSubmit(name.trim(), phone.trim(), address.trim())
                }
            }
        }
    ) {
        DialogTextField(
            label = "HỌ TÊN",
            value = name,
            onValueChange = {
                name = it
                localError = null
            }
        )
        DialogTextField(
            label = "SỐ ĐIỆN THOẠI",
            value = phone,
            onValueChange = {
                phone = it
                localError = null
            },
            keyboardType = KeyboardType.Phone
        )
        DialogTextField(
            label = "ĐỊA CHỈ",
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

    SettingsFormDialog(
        onDismissRequest = onDismiss,
        title = "Đổi mật khẩu",
        subtitle = "Vui lòng nhập đầy đủ thông tin để cập nhật mật khẩu mới.",
        isSubmitting = isSubmitting,
        confirmText = "Xác nhận",
        onConfirm = {
            when {
                currentPassword.isBlank() || newPassword.isBlank() || confirmPassword.isBlank() ->
                    localError = "Vui lòng nhập đầy đủ thông tin."
                newPassword != confirmPassword ->
                    localError = "Mật khẩu xác nhận không khớp."
                else -> {
                    localError = null
                    onSubmit(currentPassword, newPassword, confirmPassword)
                }
            }
        }
    ) {
        DialogTextField(
            label = "MẬT KHẨU CŨ",
            value = currentPassword,
            onValueChange = {
                currentPassword = it
                localError = null
            },
            visualTransformation = PasswordVisualTransformation()
        )
        DialogTextField(
            label = "MẬT KHẨU MỚI",
            value = newPassword,
            onValueChange = {
                newPassword = it
                localError = null
            },
            visualTransformation = PasswordVisualTransformation()
        )
        DialogTextField(
            label = "XÁC NHẬN MẬT KHẨU",
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
                            text = "Hủy",
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
            profile = UserProfile(
                userId = "1",
                name = "Trịnh Văn Bình",
                email = "binh@example.com",
                phone = "0912345678",
                address = "Hà Nội"
            )
        ),
        snackbarHostState = SnackbarHostState(),
        onBackClick = {},
        onRefresh = {},
        onUpdateProfile = { _, _, _ -> },
        onChangePassword = { _, _, _ -> },
        onLogoutClick = {}
    )
}
