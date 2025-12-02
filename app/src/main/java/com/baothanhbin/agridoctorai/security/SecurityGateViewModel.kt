package com.baothanhbin.agridoctorai.security

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.baothanhbin.core.data.repository.SecuritySettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class BiometricGateState(
    val biometricsEnabled: Boolean = false,
    val hasAuthenticated: Boolean = false,
    val promptInProgress: Boolean = false,
    val errorMessage: String? = null,
    val autoPromptEnabled: Boolean = true
) {
    val shouldLockApp: Boolean
        get() = biometricsEnabled && !hasAuthenticated

    val shouldPromptBiometric: Boolean
        get() = biometricsEnabled && !hasAuthenticated && autoPromptEnabled && !promptInProgress
}

@HiltViewModel
class SecurityGateViewModel @Inject constructor(
    private val securitySettingsRepository: SecuritySettingsRepository
) : ViewModel() {

    private val authenticated = MutableStateFlow(false)
    private val promptInProgress = MutableStateFlow(false)
    private val errorMessage = MutableStateFlow<String?>(null)
    private val autoPromptEnabled = MutableStateFlow(true)

    val gateState: StateFlow<BiometricGateState> = combine(
        securitySettingsRepository.observeBiometricEnabled(),
        authenticated,
        promptInProgress,
        errorMessage,
        autoPromptEnabled
    ) { biometricEnabled, isAuthenticated, isPrompting, error, autoPrompt ->
        BiometricGateState(
            biometricsEnabled = biometricEnabled,
            hasAuthenticated = isAuthenticated,
            promptInProgress = isPrompting,
            errorMessage = error,
            autoPromptEnabled = autoPrompt
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = BiometricGateState()
    )

    fun onPromptLaunched() {
        promptInProgress.value = true
        errorMessage.value = null
        autoPromptEnabled.value = true
    }

    fun onBiometricAuthenticated() {
        authenticated.value = true
        promptInProgress.value = false
        errorMessage.value = null
        autoPromptEnabled.value = true
        viewModelScope.launch {
            securitySettingsRepository.updateLastUnlockTimestamp(System.currentTimeMillis())
        }
    }

    fun onBiometricError(message: String?) {
        promptInProgress.value = false
        errorMessage.value = message
        autoPromptEnabled.value = false
    }

    fun resetAuthentication() {
        authenticated.value = false
        autoPromptEnabled.value = true
        promptInProgress.value = false
        errorMessage.value = null
    }

    fun requestRetry() {
        autoPromptEnabled.value = true
    }
}

