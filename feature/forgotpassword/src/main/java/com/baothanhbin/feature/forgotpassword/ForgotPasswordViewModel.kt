package com.baothanhbin.feature.forgotpassword

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.baothanhbin.core.data.repository.AuthRepository
import com.baothanhbin.core.model.ForgotPasswordRequest
import com.baothanhbin.core.model.ResetPasswordRequest
import com.baothanhbin.core.model.VerifyForgotOtpRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class ForgotPasswordStep {
    RequestOtp,
    VerifyOtp,
    ResetPassword
}

data class ForgotPasswordUiState(
    val email: String = "",
    val step: ForgotPasswordStep = ForgotPasswordStep.RequestOtp,
    val isLoading: Boolean = false,
    val error: String? = null,
    val message: String? = null,
    val resetToken: String? = null,
    val isResetSuccessful: Boolean = false
)

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")
    private val passwordRegex = Regex("^(?=.*[A-Za-z])(?=.*\\d).{8,}$")

    private val _uiState = MutableStateFlow(ForgotPasswordUiState())
    val uiState: StateFlow<ForgotPasswordUiState> = _uiState.asStateFlow()

    fun sendOtp(email: String) {
        when {
            email.isBlank() -> {
                _uiState.value = _uiState.value.copy(error = "Vui lòng nhập email.")
                return
            }

            !emailRegex.matches(email.trim()) -> {
                _uiState.value = _uiState.value.copy(error = "Email không đúng định dạng.")
                return
            }
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, message = null)
            authRepository.forgotPassword(ForgotPasswordRequest(email.trim()))
                .onSuccess { response ->
                    _uiState.value = _uiState.value.copy(
                        email = email.trim(),
                        step = ForgotPasswordStep.VerifyOtp,
                        isLoading = false,
                        message = response.message,
                        resetToken = null,
                        error = null
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Không gửi được OTP."
                    )
                }
        }
    }

    fun verifyOtp(email: String, otp: String) {
        when {
            email.isBlank() -> {
                _uiState.value = _uiState.value.copy(error = "Vui lòng nhập email.")
                return
            }

            !emailRegex.matches(email.trim()) -> {
                _uiState.value = _uiState.value.copy(error = "Email không đúng định dạng.")
                return
            }

            otp.length != 6 -> {
                _uiState.value = _uiState.value.copy(error = "OTP phải gồm đúng 6 số.")
                return
            }
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, message = null)
            authRepository.verifyForgotOtp(VerifyForgotOtpRequest(email.trim(), otp))
                .onSuccess { response ->
                    _uiState.value = _uiState.value.copy(
                        step = ForgotPasswordStep.ResetPassword,
                        isLoading = false,
                        message = response.message,
                        resetToken = response.resetToken,
                        error = null
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "OTP không hợp lệ."
                    )
                }
        }
    }

    fun resetPassword(email: String, newPassword: String, confirmPassword: String) {
        when {
            email.isBlank() -> {
                _uiState.value = _uiState.value.copy(error = "Vui lòng nhập email.")
                return
            }

            !emailRegex.matches(email.trim()) -> {
                _uiState.value = _uiState.value.copy(error = "Email không đúng định dạng.")
                return
            }

            newPassword.isBlank() || confirmPassword.isBlank() -> {
                _uiState.value = _uiState.value.copy(error = "Vui lòng nhập đầy đủ mật khẩu mới.")
                return
            }

            !passwordRegex.matches(newPassword) -> {
                _uiState.value = _uiState.value.copy(
                    error = "Mật khẩu phải có ít nhất 8 ký tự, gồm chữ và số."
                )
                return
            }

            newPassword != confirmPassword -> {
                _uiState.value = _uiState.value.copy(error = "Mật khẩu xác nhận không khớp.")
                return
            }
        }

        val resetToken = _uiState.value.resetToken
        if (resetToken.isNullOrBlank()) {
            _uiState.value = _uiState.value.copy(
                error = "Phiên đặt lại mật khẩu không hợp lệ. Vui lòng xác thực OTP lại."
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, message = null)
            authRepository.resetPassword(
                ResetPasswordRequest(
                    email = email.trim(),
                    resetToken = resetToken,
                    newPassword = newPassword,
                    confirmPassword = confirmPassword
                )
            ).onSuccess { response ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isResetSuccessful = true,
                    message = response.message,
                    error = null
                )
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = error.message ?: "Đặt lại mật khẩu thất bại."
                )
            }
        }
    }

    fun consumeResetSuccess() {
        _uiState.value = _uiState.value.copy(isResetSuccessful = false)
    }
}
