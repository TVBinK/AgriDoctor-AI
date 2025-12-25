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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.baothanhbin.core.theme.Disabled
import com.baothanhbin.core.theme.GreenSurface
import com.baothanhbin.core.theme.Subtitle
import com.baothanhbin.core.theme.Title
import com.baothanhbin.core.theme.White

@Composable
fun VerifycationOTPScreen(
    modifier: Modifier = Modifier,
    viewModel: VerificationOTPViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {},
    onVerifySuccess: () -> Unit = {} // Callback to navigate to main app
) {
    val uiState by viewModel.uiState.collectAsState()
    var pin by remember { mutableStateOf("") }
    val isPinComplete = pin.length == 6

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onVerifySuccess()
        }
    }

    Scaffold(
        containerColor = White,
        topBar = {
            TopBar(onBackClick)
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Icon Shield
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF0F3FF)), // Light purple/blue
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

            // Title
            Text(
                text = "Enter verification code",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                ),
                color = Title,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Subtitle
            Text(
                text = buildAnnotatedString {
                    append("We've sent a 6-digit verification code to\n")
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

            // OTP Input
            OtpInputField(
                otpText = pin,
                onOtpTextChange = { if (it.length <= 6) pin = it }
            )
            
            if (uiState.error != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = uiState.error!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Resend Options
            Text(
                text = "Didn't receive the code?",
                style = MaterialTheme.typography.bodyMedium,
                color = Subtitle
            )

            Spacer(modifier = Modifier.height(8.dp))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (uiState.isLoading) "Sending..." else "Resend code",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = if (uiState.isLoading) Subtitle else GreenSurface,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.clickable(enabled = !uiState.isLoading) { 
                        viewModel.resendOtp() 
                    }
                )
                Spacer(modifier = Modifier.height(4.dp))
                // Static timer for now as text
                Text(
                    text = "Resend in 00:53",
                    style = MaterialTheme.typography.bodySmall,
                    color = Disabled
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Verify Button
            Button(
                onClick = { viewModel.verifyOtp(pin) },
                enabled = isPinComplete && !uiState.isLoading,
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
                if (uiState.isLoading) {
                    androidx.compose.material3.CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = White
                    )
                } else {
                    Text(
                        text = "Verify Code",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (isPinComplete) White else Subtitle
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Security Tip
            SecurityTipCard()
        }
    }
}

@Composable
private fun TopBar(onBackClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, start = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color(0xFFF7F7F7))
                .clickable { onBackClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Title,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun OtpInputField(
    otpText: String,
    onOtpTextChange: (String) -> Unit
) {
    BasicTextField(
        value = otpText,
        onValueChange = {
            if (it.all { char -> char.isDigit() }) {
                onOtpTextChange(it)
            }
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        decorationBox = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(6) { index ->
                    val char = when {
                        index >= otpText.length -> ""
                        else -> otpText[index].toString()
                    }
                    val isFocused = index == otpText.length
                    
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(0.85f)
                            .border(
                                width = if (isFocused) 1.5.dp else 1.dp,
                                color = if (isFocused) GreenSurface else Color(0xFFE0E0E0),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .background(White, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = char,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp
                            ),
                            color = Title,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    )
}

@Composable
fun SecurityTipCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF0F6FF)) // Light Blue
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Filled.Info,
                contentDescription = null,
                tint = GreenSurface,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Security tip",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = GreenSurface
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Never share your verification code with anyone. We'll never ask for it.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color(0xFF2E7D32), // Darker green for text readability
                        lineHeight = 16.sp
                    )
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewVerifycationOTPScreen() {
    VerifycationOTPScreen()
}
