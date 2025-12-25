package com.baothanhbin.feature.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.baothanhbin.core.data.repository.AuthRepository
import com.baothanhbin.core.model.SignupRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SignupUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSignupSuccess: Boolean = false,
    val identifier: String = ""
)

@HiltViewModel
class SignupViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SignupUiState())
    val uiState: StateFlow<SignupUiState> = _uiState.asStateFlow()

    fun signup(name: String, email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            val request = SignupRequest(name = name, email = email, password = password)
            
            authRepository.signup(request)
                .onSuccess { authResponse ->
                    // Kiểm tra response từ server
                    // Nếu isRegister = true hoặc message chứa từ khóa "OTP" -> Thành công
                    val isSuccess = authResponse.isRegister || 
                                    (!authResponse.message.isNullOrEmpty() && 
                                     (authResponse.message!!.contains("OTP", ignoreCase = true) || 
                                      authResponse.message!!.contains("successfully", ignoreCase = true)))

                    if (isSuccess) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            isSignupSuccess = true,
                            identifier = email
                        )
                    } else {
                        // Trường hợp khác coi là lỗi (hoặc message lỗi)
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = authResponse.message ?: "Đăng ký thất bại"
                        )
                    }
                }
                .onFailure { e ->
                    // Network error hoặc exception khác
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Đăng ký thất bại"
                    )
                }
        }
    }

    fun resetState() {
        _uiState.value = SignupUiState()
    }
}
