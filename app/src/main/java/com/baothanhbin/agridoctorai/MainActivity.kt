package com.baothanhbin.agridoctorai

import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.baothanhbin.agridoctorai.navigation.rememberAppState
import com.baothanhbin.agridoctorai.security.SecurityGateViewModel
import com.baothanhbin.agridoctorai.security.SecurityManager
import com.baothanhbin.agridoctorai.ui.dialog.BiometricLockScreen
import com.baothanhbin.agridoctorai.ui.MainApp
import com.baothanhbin.agridoctorai.ui.SecurityWarningDialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import com.baothanhbin.agridoctorai.resources.R

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    private val securityGateViewModel: SecurityGateViewModel by viewModels()
    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var securityCheckResult by remember { mutableStateOf<com.baothanhbin.agridoctorai.security.SecurityCheckResult?>(null) }
            var showSecurityWarning by remember { mutableStateOf(false) }

            // Thực hiện kiểm tra bảo mật khi app khởi động
            LaunchedEffect(Unit) {
                val result = SecurityManager.performSecurityCheck(this@MainActivity)
                securityCheckResult = result

                // Hiển thị cảnh báo nếu có vấn đề nghiêm trọng
                if (!result.isSecure && result.hasCriticalIssues()) {
                    showSecurityWarning = true
                }
            }

            // Hiển thị dialog cảnh báo bảo mật
            if (showSecurityWarning && securityCheckResult != null) {
                SecurityWarningDialog(
                    result = securityCheckResult!!,
                    onExit = {
                        // Đóng ứng dụng
                        finish()
                    }
                )
            }

            val gateState by securityGateViewModel.gateState.collectAsStateWithLifecycle()
            val isLoggedIn by mainViewModel.isLoggedIn.collectAsStateWithLifecycle()

            LaunchedEffect(gateState.shouldPromptBiometric) {
                if (gateState.shouldPromptBiometric) {
                    securityGateViewModel.onPromptLaunched()
                    showBiometricPrompt(
                        onSuccess = { securityGateViewModel.onBiometricAuthenticated() },
                        onError = { errorMessage ->
                            securityGateViewModel.onBiometricError(errorMessage)
                        }
                    )
                }
            }

            val appState = rememberAppState() // Tạo navigation state

            LaunchedEffect(isLoggedIn) {
                if (!isLoggedIn) {
                    appState.navController.navigate(com.baothanhbin.feature.login.navigation.LOGIN_ROUTE) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                MainApp(
                    modifier = Modifier.fillMaxSize(),
                    appState = appState,
                    startDestination = if (isLoggedIn) com.baothanhbin.feature.home.navigation.HOME_ROUTE else com.baothanhbin.feature.login.navigation.LOGIN_ROUTE
                )

                if (gateState.shouldLockApp) {
                    BiometricLockScreen(
                        message = gateState.errorMessage,
                        onRetry = {
                            securityGateViewModel.requestRetry()
                        }
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (securityGateViewModel.gateState.value.biometricsEnabled) {
            securityGateViewModel.resetAuthentication()
        }
    }

    private fun showBiometricPrompt(
        onSuccess: () -> Unit,
        onError: (String?) -> Unit
    ) {
        val executor = ContextCompat.getMainExecutor(this)
        val prompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                onError(errString.toString())
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                onError(getString(R.string.biometric_prompt_failed))
            }
        })

        val promptBuilder = BiometricPrompt.PromptInfo.Builder()
            .setTitle(getString(R.string.biometric_prompt_title))
            .setSubtitle(getString(R.string.biometric_prompt_subtitle))
            .setNegativeButtonText(getString(R.string.biometric_prompt_cancel))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            promptBuilder.setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or
                    BiometricManager.Authenticators.BIOMETRIC_WEAK
            )
                .setConfirmationRequired(false)
        }

        prompt.authenticate(promptBuilder.build())
    }
}