package com.baothanhbin.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.baothanhbin.core.data.repository.AuthRepository
import com.baothanhbin.core.model.ChangePasswordRequest
import com.baothanhbin.core.model.UpdateProfileRequest
import com.baothanhbin.core.model.UserProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SettingsUiState(
    val isLoading: Boolean = true,
    val isSubmitting: Boolean = false,
    val profile: UserProfile? = null,
    val error: String? = null,
    val message: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            authRepository.getProfile()
                .onSuccess { profile ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        profile = profile,
                        error = null
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Khong tai duoc thong tin tai khoan."
                    )
                }
        }
    }

    fun updateProfile(name: String, phone: String, address: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmitting = true, error = null, message = null)
            authRepository.updateProfile(
                UpdateProfileRequest(
                    name = name,
                    phone = phone,
                    address = address
                )
            ).onSuccess { response ->
                _uiState.value = _uiState.value.copy(
                    isSubmitting = false,
                    profile = response.user,
                    message = response.message
                )
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isSubmitting = false,
                    error = error.message ?: "Cap nhat thong tin that bai."
                )
            }
        }
    }

    fun changePassword(currentPassword: String, newPassword: String, confirmPassword: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmitting = true, error = null, message = null)
            authRepository.changePassword(
                ChangePasswordRequest(
                    currentPassword = currentPassword,
                    newPassword = newPassword,
                    confirmPassword = confirmPassword
                )
            ).onSuccess { response ->
                _uiState.value = _uiState.value.copy(
                    isSubmitting = false,
                    message = response.message
                )
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isSubmitting = false,
                    error = error.message ?: "Doi mat khau that bai."
                )
            }
        }
    }

    fun clearFeedback() {
        _uiState.value = _uiState.value.copy(error = null, message = null)
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }
}
