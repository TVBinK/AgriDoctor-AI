package com.baothanhbin.feature.verificationotp

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.baothanhbin.agridoctorai.resources.R
import com.baothanhbin.core.theme.Disabled
import com.baothanhbin.core.theme.GreenSurface
import com.baothanhbin.core.theme.Subtitle
import com.baothanhbin.core.theme.Title
import com.baothanhbin.core.theme.White
import com.baothanhbin.core.ui.feedback.LoadingOverlay
import com.baothanhbin.feature.verificationotp.component.*

@Composable
fun VerifycationOTPScreen(
    modifier: Modifier = Modifier,
    viewModel: VerificationOTPViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {},
    onVerifySuccess: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var pin by remember { mutableStateOf("") }
    val isPinComplete = pin.length == 6
    val resendCooldownText = remember(uiState.resendCooldownSeconds) {
        formatResendCountdown(uiState.resendCooldownSeconds)
    }
    val canResend = !uiState.isVerifying && !uiState.isResending && uiState.resendCooldownSeconds == 0

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onVerifySuccess()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            containerColor = White,
            topBar = {
                TopBar(onBackClick)
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(20.dp))

                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF0F3FF)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Security,
                        contentDescription = null,
                        tint = GreenSurface,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = stringResource(id = R.string.enter_verification_code),
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    ),
                    color = Title,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = buildAnnotatedString {
                        append(stringResource(id = R.string.verification_code_sent))
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = Title)) {
                            append(uiState.email)
                        }
                    },
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Subtitle,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(32.dp))

                OtpInputField(
                    otpText = pin,
                    onOtpTextChange = { if (it.length <= 6) pin = it }
                )

                uiState.error?.let { message ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = stringResource(id = R.string.didnt_receive_code),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Subtitle
                )

                Spacer(modifier = Modifier.height(8.dp))

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    TextButton(
                        onClick = {
                            viewModel.resendOtp()
                        },
                        enabled = canResend,
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(
                            horizontal = 12.dp,
                            vertical = 4.dp
                        )
                    ) {
                        Text(
                            text = if (uiState.isResending) {
                                stringResource(id = R.string.sending)
                            } else {
                                stringResource(id = R.string.resend_code)
                            },
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = if (canResend) GreenSurface else Subtitle,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    if (uiState.resendCooldownSeconds > 0) {
                        Text(
                            text = stringResource(id = R.string.resend_in_timer, resendCooldownText),
                            style = MaterialTheme.typography.bodySmall,
                            color = Disabled
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { viewModel.verifyOtp(pin) },
                    enabled = isPinComplete && !uiState.isVerifying,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GreenSurface,
                        disabledContainerColor = Color(0xFFE0E0E0),
                        contentColor = White,
                        disabledContentColor = Subtitle
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    elevation = ButtonDefaults.buttonElevation(0.dp)
                ) {
                    if (uiState.isVerifying) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = White
                        )
                    } else {
                        Text(
                            text = stringResource(id = R.string.verify_code),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (isPinComplete) White else Subtitle
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
                SecurityTipCard()
            }
        }

        LoadingOverlay(isVisible = uiState.isVerifying)
    }
}
