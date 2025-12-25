package com.baothanhbin.feature.verificationotp

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.baothanhbin.core.data.repository.AuthRepository
import com.baothanhbin.core.model.VerifyOtpRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class VerificationUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
    val email: String = ""
)

@HiltViewModel
class VerificationOTPViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Giả sử email được truyền qua SavedStateHandle
    private val emailArg: String = savedStateHandle["email"] ?: "tvbink@gmail.com"

    private val _uiState = MutableStateFlow(VerificationUiState(email = emailArg))
    val uiState: StateFlow<VerificationUiState> = _uiState.asStateFlow()

    fun verifyOtp(otp: String) {
        if (otp.length < 6) return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            val request = VerifyOtpRequest(
                email = _uiState.value.email,
                otp = otp
            )
            
            authRepository.verifyOtp(request)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSuccess = true
                    )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Xác thực thất bại"
                    )
                }
        }
    }

    fun resendOtp() {

    }
}
