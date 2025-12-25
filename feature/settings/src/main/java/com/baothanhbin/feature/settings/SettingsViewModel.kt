package com.baothanhbin.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.baothanhbin.core.data.repository.SecuritySettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val securitySettingsRepository: SecuritySettingsRepository,
    private val authRepository: com.baothanhbin.core.data.repository.AuthRepository
) : ViewModel() {

    val uiState = securitySettingsRepository.observeBiometricEnabled()
        .map { enabled ->
            SettingsUiState(biometricLockEnabled = enabled)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SettingsUiState()
        )

    fun setBiometricLockEnabled(enabled: Boolean) {
        viewModelScope.launch {
            securitySettingsRepository.setBiometricEnabled(enabled)
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }
}

