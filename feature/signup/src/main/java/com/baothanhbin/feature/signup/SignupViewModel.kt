package com.baothanhbin.feature.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.baothanhbin.core.data.repository.AuthRepository
import com.baothanhbin.core.model.SignupRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SignupUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSignupSuccess: Boolean = false,
    val identifier: String = "",
    val name: String = "",
    val password: String = ""
)

@HiltViewModel
class SignupViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SignupUiState())
    val uiState: StateFlow<SignupUiState> = _uiState.asStateFlow()

    fun signup(name: String, email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null,
                name = name,
                password = password
            )

            val result = authRepository.signup(
                SignupRequest(
                    name = name,
                    email = email,
                    password = password
                )
            )

            result
                .onSuccess { authResponse ->
                    val isSuccess = authResponse.isRegister ||
                        (
                            !authResponse.message.isNullOrEmpty() &&
                                (
                                    authResponse.message!!.contains("OTP", ignoreCase = true) ||
                                        authResponse.message!!.contains("successfully", ignoreCase = true)
                                    )
                            )

                    if (isSuccess) {
                        authRepository.logout()
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            isSignupSuccess = true,
                            identifier = email,
                            name = name,
                            password = password
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = authResponse.message ?: "Dang ky that bai"
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Dang ky that bai"
                    )
                }
        }
    }

    fun resetState() {
        _uiState.value = SignupUiState()
    }
}
