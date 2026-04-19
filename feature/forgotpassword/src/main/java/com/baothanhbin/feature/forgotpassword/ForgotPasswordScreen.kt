package com.baothanhbin.feature.forgotpassword

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Password
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.baothanhbin.agridoctorai.resources.R
import com.baothanhbin.core.theme.Blue1
import com.baothanhbin.core.theme.Body3
import com.baothanhbin.core.theme.Body4
import com.baothanhbin.core.theme.Button1
import com.baothanhbin.core.theme.GreenSurface
import com.baothanhbin.core.theme.Label2
import com.baothanhbin.core.theme.Subtitle
import com.baothanhbin.core.theme.Title
import com.baothanhbin.core.theme.Title1
import com.baothanhbin.core.theme.TitleLarge3
import com.baothanhbin.core.theme.White

@Composable
fun ForgotPasswordRoute(
    onBackClick: () -> Unit,
    onResetSuccess: () -> Unit,
    viewModel: ForgotPasswordViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.message) {
        uiState.message?.let { snackbarHostState.showSnackbar(it) }
    }

    LaunchedEffect(uiState.isResetSuccessful) {
        if (uiState.isResetSuccessful) {
            onResetSuccess()
            viewModel.consumeResetSuccess()
        }
    }

    ForgotPasswordScreen(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onBackClick = onBackClick,
        onSendOtp = viewModel::sendOtp,
        onVerifyOtp = viewModel::verifyOtp,
        onResetPassword = viewModel::resetPassword
    )
}

@Composable
fun ForgotPasswordScreen(
    uiState: ForgotPasswordUiState,
    snackbarHostState: SnackbarHostState,
    onBackClick: () -> Unit,
    onSendOtp: (String) -> Unit,
    onVerifyOtp: (String, String) -> Unit,
    onResetPassword: (String, String, String) -> Unit
) {
    var email by rememberSaveable(uiState.email) { mutableStateOf(uiState.email) }
    var otp by rememberSaveable { mutableStateOf("") }
    var newPassword by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }

    Scaffold(
        containerColor = Blue1,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Blue1)
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 24.dp),
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
                        text = "Quên mật khẩu",
                        style = MaterialTheme.typography.TitleLarge3,
                        color = GreenSurface
                    )
                }

                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(18.dp)
                    ) {
                        ForgotPasswordStepRow(currentStep = uiState.step)

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = stepTitle(uiState.step),
                                style = MaterialTheme.typography.Title1,
                                color = Title
                            )
                            Text(
                                text = stepDescription(uiState.step),
                                style = MaterialTheme.typography.Body3,
                                color = Subtitle
                            )
                        }

                        AppTextField(
                            label = "EMAIL",
                            value = email,
                            onValueChange = { email = it },
                            leadingIcon = Icons.Default.Email,
                            keyboardType = KeyboardType.Email,
                            enabled = uiState.step == ForgotPasswordStep.RequestOtp
                        )

                        if (uiState.step != ForgotPasswordStep.RequestOtp) {
                            AppTextField(
                                label = "MÃ OTP",
                                value = otp,
                                onValueChange = { otp = it.take(6) },
                                leadingIcon = Icons.Default.Password,
                                keyboardType = KeyboardType.Number,
                                enabled = uiState.step == ForgotPasswordStep.VerifyOtp
                            )
                        }

                        if (uiState.step == ForgotPasswordStep.ResetPassword) {
                            AppTextField(
                                label = "MẬT KHẨU MỚI",
                                value = newPassword,
                                onValueChange = { newPassword = it },
                                leadingIcon = Icons.Default.Lock,
                                visualTransformation = PasswordVisualTransformation()
                            )
                            AppTextField(
                                label = "XÁC NHẬN MẬT KHẨU",
                                value = confirmPassword,
                                onValueChange = { confirmPassword = it },
                                leadingIcon = Icons.Default.Lock,
                                visualTransformation = PasswordVisualTransformation()
                            )
                        }

                        uiState.error?.let {
                            Text(
                                text = it,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.Body3
                            )
                        }

                        Button(
                            onClick = {
                                when (uiState.step) {
                                    ForgotPasswordStep.RequestOtp -> onSendOtp(email.trim())
                                    ForgotPasswordStep.VerifyOtp -> onVerifyOtp(email.trim(), otp)
                                    ForgotPasswordStep.ResetPassword -> onResetPassword(
                                        email.trim(),
                                        newPassword,
                                        confirmPassword
                                    )
                                }
                            },
                            enabled = !uiState.isLoading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GreenSurface,
                                contentColor = White,
                                disabledContainerColor = GreenSurface.copy(alpha = 0.45f),
                                disabledContentColor = White.copy(alpha = 0.85f)
                            )
                        ) {
                            if (uiState.isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(22.dp),
                                    color = White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = stepButton(uiState.step),
                                    style = MaterialTheme.typography.Button1
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
private fun ForgotPasswordStepRow(currentStep: ForgotPasswordStep) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        ForgotPasswordStep.entries.forEachIndexed { index, step ->
            val isActive = step == currentStep
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isActive) GreenSurface else Blue1.copy(alpha = 0.6f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${index + 1}",
                        style = MaterialTheme.typography.Label2,
                        color = if (isActive) White else GreenSurface
                    )
                    Text(
                        text = shortStepName(step),
                        style = MaterialTheme.typography.Label2,
                        color = if (isActive) White else Subtitle
                    )
                }
            }
        }
    }
}

@Composable
private fun AppTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    leadingIcon: ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text,
    enabled: Boolean = true,
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.Label2,
            color = Subtitle
        )
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = enabled,
            shape = RoundedCornerShape(16.dp),
            textStyle = MaterialTheme.typography.Body4.copy(color = Title),
            leadingIcon = {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = GreenSurface
                )
            },
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            visualTransformation = visualTransformation,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Blue1.copy(alpha = 0.55f),
                unfocusedContainerColor = Blue1.copy(alpha = 0.55f),
                disabledContainerColor = Blue1.copy(alpha = 0.35f),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                focusedTextColor = Title,
                unfocusedTextColor = Title,
                disabledTextColor = Title,
                focusedLeadingIconColor = GreenSurface,
                unfocusedLeadingIconColor = GreenSurface,
                disabledLeadingIconColor = GreenSurface.copy(alpha = 0.65f),
                cursorColor = GreenSurface
            )
        )
    }
}

private fun stepTitle(step: ForgotPasswordStep): String = when (step) {
    ForgotPasswordStep.RequestOtp -> "Nhập email nhận OTP"
    ForgotPasswordStep.VerifyOtp -> "Xác thực mã OTP"
    ForgotPasswordStep.ResetPassword -> "Đặt lại mật khẩu"
}

private fun stepDescription(step: ForgotPasswordStep): String = when (step) {
    ForgotPasswordStep.RequestOtp -> "Hệ thống sẽ gửi mã xác thực đến email bạn đã đăng ký."
    ForgotPasswordStep.VerifyOtp -> "Nhập mã gồm 6 số vừa được gửi để tiếp tục."
    ForgotPasswordStep.ResetPassword -> "Tạo mật khẩu mới đủ mạnh để bảo vệ tài khoản của bạn."
}

private fun stepButton(step: ForgotPasswordStep): String = when (step) {
    ForgotPasswordStep.RequestOtp -> "Gửi OTP"
    ForgotPasswordStep.VerifyOtp -> "Xác thực OTP"
    ForgotPasswordStep.ResetPassword -> "Đặt lại mật khẩu"
}

private fun shortStepName(step: ForgotPasswordStep): String = when (step) {
    ForgotPasswordStep.RequestOtp -> "Email"
    ForgotPasswordStep.VerifyOtp -> "OTP"
    ForgotPasswordStep.ResetPassword -> "Mật khẩu"
}

@Preview(showBackground = true)
@Composable
private fun ForgotPasswordScreenPreview() {
    ForgotPasswordScreen(
        uiState = ForgotPasswordUiState(),
        snackbarHostState = SnackbarHostState(),
        onBackClick = {},
        onSendOtp = {},
        onVerifyOtp = { _, _ -> },
        onResetPassword = { _, _, _ -> }
    )
}
