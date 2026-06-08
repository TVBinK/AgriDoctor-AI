package com.baothanhbin.feature.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.baothanhbin.core.data.repository.AuthRepository
import com.baothanhbin.core.model.LoginRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LoginUiState(
    val isLoading: Boolean = false,
    val isOtpSent: Boolean = false,
    val error: String? = null,
    val email: String = "",
    val password: String = ""
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun login(email: String, password: String?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null,
                email = email,
                password = password.orEmpty()
            )

            val result = authRepository.login(
                LoginRequest(
                    email = email,
                    password = password
                )
            )

            result
                .onSuccess { authResponse ->
                    val isSuccessMessage = !authResponse.message.isNullOrEmpty() &&
                        (
                            authResponse.message!!.contains("successfully", ignoreCase = true) ||
                                authResponse.message!!.contains("OTP sent", ignoreCase = true)
                            )

                    if (authResponse.needRegister || (!authResponse.message.isNullOrEmpty() && !isSuccessMessage)) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = authResponse.message ?: "Dang nhap that bai"
                        )
                    } else {
                        authRepository.logout()
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            isOtpSent = true
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Login failed"
                    )
                }
        }
    }

    fun resetState() {
        _uiState.value = LoginUiState()
    }
}
