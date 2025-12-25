package com.baothanhbin.feature.signup

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.net.Uri
import android.os.Build
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import com.baothanhbin.agridoctorai.resources.R
import com.baothanhbin.core.theme.BlueDefault
import com.baothanhbin.core.theme.Body4
import com.baothanhbin.core.theme.Subtitle
import com.baothanhbin.core.theme.Title
import com.baothanhbin.core.theme.White

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import android.widget.Toast

@Composable
fun SignupRoute(
    onSignupSuccess: (identifier: String) -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: SignupViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    androidx.compose.runtime.LaunchedEffect(uiState.isSignupSuccess) {
        if (uiState.isSignupSuccess) {
            onSignupSuccess(uiState.identifier)
            viewModel.resetState()
        }
    }
    
    androidx.compose.runtime.LaunchedEffect(uiState.error) {
        if (uiState.error != null) {
            Toast.makeText(context, uiState.error, Toast.LENGTH_LONG).show()
        }
    }

    SignupScreen(
        uiState = uiState,
        onSignupClick = { name, identifier, password ->
            viewModel.signup(name, identifier, password)
        },
        onNavigateToLogin = onNavigateToLogin,
    )
}

@Composable
fun SignupScreen(
    uiState: SignupUiState = SignupUiState(),
    onSignupClick: (name: String, identifier: String, password: String) -> Unit,
    onNavigateToLogin: () -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var identifier by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    val isFormValid = name.isNotBlank() &&
        identifier.isNotBlank() &&
        password.length >= 8 &&
        password == confirmPassword

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.bg_home),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            contentScale = ContentScale.FillWidth
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 150.dp, bottom = 24.dp, start = 16.dp, end = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            
            // Show Loading Indicator if needed
             if (uiState.isLoading) {
                 androidx.compose.material3.CircularProgressIndicator(
                     modifier = Modifier.padding(bottom = 16.dp)
                 )
             }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                
                Text(
                    text = "Đăng ký",
                    style = MaterialTheme.typography.titleLarge,
                    color = Title,
                    textAlign = TextAlign.Center,
                )

                // ===== Form đăng ký với style giống LoginScreen =====
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(animationSpec = tween(600, delayMillis = 600)),
                    exit = fadeOut(animationSpec = tween(600))
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.Start
                    ) {
                        // Name label
                        Text(
                            text = "Họ và tên",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF232B35),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            placeholder = {
                                Text(
                                    text = "Nhập họ và tên của bạn",
                                    fontSize = 15.sp,
                                    color = Color(0xFFB0B4BA)
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF34A853),
                                unfocusedBorderColor = Color(0xFFE8E8E8),
                                focusedContainerColor = Color(0xFFF8F9FA),
                                unfocusedContainerColor = Color(0xFFF8F9FA)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Next
                            ),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Email label
                        Text(
                            text = "Email",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF232B35),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        OutlinedTextField(
                            value = identifier,
                            onValueChange = { identifier = it },
                            placeholder = {
                                Text(
                                    text = "tvbink@example.com",
                                    fontSize = 15.sp,
                                    color = Color(0xFFB0B4BA)
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF34A853),
                                unfocusedBorderColor = Color(0xFFE8E8E8),
                                focusedContainerColor = Color(0xFFF8F9FA),
                                unfocusedContainerColor = Color(0xFFF8F9FA)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            ),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Password label
                        Text(
                            text = "Mật khẩu",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF232B35),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            placeholder = {
                                Text(
                                    text = "Tối thiểu 8 ký tự",
                                    fontSize = 15.sp,
                                    color = Color(0xFFB0B4BA)
                                )
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = if (passwordVisible) "Ẩn mật khẩu" else "Hiển thị mật khẩu",
                                        tint = Color(0xFFB0B4BA)
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF34A853),
                                unfocusedBorderColor = Color(0xFFE8E8E8),
                                focusedContainerColor = Color(0xFFF8F9FA),
                                unfocusedContainerColor = Color(0xFFF8F9FA)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Next
                            ),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Confirm Password label
                        Text(
                            text = "Xác nhận mật khẩu",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF232B35),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            placeholder = {
                                Text(
                                    text = "Nhập lại mật khẩu",
                                    fontSize = 15.sp,
                                    color = Color(0xFFB0B4BA)
                                )
                            },
                            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                    Icon(
                                        imageVector = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = if (confirmPasswordVisible) "Ẩn mật khẩu" else "Hiển thị mật khẩu",
                                        tint = Color(0xFFB0B4BA)
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF34A853),
                                unfocusedBorderColor = Color(0xFFE8E8E8),
                                focusedContainerColor = Color(0xFFF8F9FA),
                                unfocusedContainerColor = Color(0xFFF8F9FA)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            singleLine = true,
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    if (isFormValid) {
                                        onSignupClick(name.trim(), identifier.trim(), password)
                                    }
                                }
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(animationSpec = tween(600, delayMillis = 800)),
                    exit = fadeOut(animationSpec = tween(600))
                ) {
                    val buttonScale = if (isFormValid) 1f else 0.98f
                    Button(
                        onClick = {
                            if (isFormValid) {
                                onSignupClick(name.trim(), identifier.trim(), password)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .scale(buttonScale),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isFormValid) Color(0xFFFF7A3C) else Color(
                                0xFFD3D3D3
                            )
                        ),
                        shape = RoundedCornerShape(12.dp),
                        enabled = isFormValid
                    ) {
                        Text(
                            text = "Đăng ký",
                            color = if (isFormValid) Color.White else Color(0xFF9E9E9E),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Hoặc",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Subtitle,
                    textAlign = TextAlign.Center
                )

                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onNavigateToLogin),
                    text = "Đã có tài khoản? Đăng nhập",
                    textAlign = TextAlign.Center,
                    color = BlueDefault,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SignupScreenPreview() {
    SignupScreen(
        onSignupClick = { _, _, _ -> },
        onNavigateToLogin = {},
    )
}
