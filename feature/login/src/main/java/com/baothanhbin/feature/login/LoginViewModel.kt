package com.baothanhbin.feature.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.baothanhbin.core.data.repository.AuthRepository
import com.baothanhbin.core.model.LoginRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val isLoading: Boolean = false,
    val isOtpSent: Boolean = false,
    val error: String? = null,
    val email: String = ""
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun login(email: String, password: String?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, email = email)
            
            val request = LoginRequest(email = email, password = password)
            
            authRepository.login(request)
                .onSuccess { authResponse ->
                    // Kiểm tra lỗi từ server:
                    // 1. Cờ needRegister = true (Tài khoản chưa tồn tại)
                    // 2. Có message lỗi (VD: "Mật khẩu không đúng", "Tài khoản không tồn tại")
                    // Lưu ý: Nếu server trả về message khi Thành công (VD: "OTP đã gửi"), logic này cần điều chỉnh lại.
                    val isSuccessMessage = !authResponse.message.isNullOrEmpty() && 
                        (authResponse.message!!.contains("successfully", ignoreCase = true) || 
                         authResponse.message!!.contains("OTP sent", ignoreCase = true))
                    
                    if (authResponse.needRegister || (!authResponse.message.isNullOrEmpty() && !isSuccessMessage)) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = authResponse.message ?: "Đăng nhập thất bại"
                        )
                    } else {
                        // Thành công, OTP đã được gửi
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            isOtpSent = true
                        )
                    }
                }
                .onFailure { e ->
                    val errorMessage = e.message ?: "Login failed"
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = errorMessage
                    )
                }
        }
    }
    
    fun resetState() {
        _uiState.value = LoginUiState()
    }
}
