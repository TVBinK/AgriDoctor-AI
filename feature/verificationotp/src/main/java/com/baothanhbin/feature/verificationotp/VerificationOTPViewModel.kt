package com.baothanhbin.feature.verificationotp

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.baothanhbin.core.data.impl.HistorySyncRepository
import com.baothanhbin.core.data.repository.AuthRepository
import com.baothanhbin.core.model.OtpVerificationPurpose
import com.baothanhbin.core.model.ResendOtpRequest
import com.baothanhbin.core.model.VerifyOtpRequest
import com.baothanhbin.feature.verificationotp.navigation.PURPOSE_ARG
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.math.max
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val RESEND_COOLDOWN_SECONDS = 60

data class VerificationUiState(
    val isVerifying: Boolean = false,
    val isResending: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
    val email: String = "",
    val purpose: OtpVerificationPurpose = OtpVerificationPurpose.LOGIN,
    val resendCooldownSeconds: Int = RESEND_COOLDOWN_SECONDS
)

@HiltViewModel
class VerificationOTPViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val historySyncRepository: HistorySyncRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val waitSecondsRegex = Regex("(\\d+)\\s*giay", RegexOption.IGNORE_CASE)
    private val emailArg: String = savedStateHandle["email"] ?: ""
    private val purpose = runCatching {
        OtpVerificationPurpose.valueOf(savedStateHandle.get<String>(PURPOSE_ARG) ?: "")
    }.getOrDefault(OtpVerificationPurpose.LOGIN)
    private var cooldownJob: Job? = null

    private val _uiState = MutableStateFlow(
        VerificationUiState(
            email = emailArg,
            purpose = purpose
        )
    )
    val uiState: StateFlow<VerificationUiState> = _uiState.asStateFlow()

    init {
        startCooldown()
    }

    fun verifyOtp(otp: String) {
        if (otp.length < 6) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isVerifying = true, error = null)

            val request = VerifyOtpRequest(
                email = _uiState.value.email,
                otp = otp
            )

            authRepository.verifyOtp(request)
                .onSuccess {
                    historySyncRepository.syncFromServer()
                    _uiState.value = _uiState.value.copy(
                        isVerifying = false,
                        isSuccess = true
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isVerifying = false,
                        error = error.message ?: "Xac thuc that bai."
                    )
                }
        }
    }

    fun resendOtp() {
        val state = _uiState.value
        if (state.isVerifying || state.isResending || state.resendCooldownSeconds > 0) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isResending = true, error = null)

            val resendResult = authRepository.resendOtp(
                ResendOtpRequest(
                    email = state.email,
                    purpose = state.purpose.name
                )
            )

            resendResult
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isResending = false,
                        error = null
                    )
                    startCooldown()
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isResending = false,
                        error = error.message ?: "Gui lai OTP that bai."
                    )

                    val cooldownSeconds = extractCooldownSeconds(error.message)
                    if (cooldownSeconds > 0) {
                        startCooldown(cooldownSeconds)
                    }
                }
        }
    }

    private fun startCooldown(seconds: Int = RESEND_COOLDOWN_SECONDS) {
        cooldownJob?.cancel()
        val initialSeconds = max(0, seconds)
        _uiState.value = _uiState.value.copy(resendCooldownSeconds = initialSeconds)
        if (initialSeconds == 0) return

        cooldownJob = viewModelScope.launch {
            for (remaining in initialSeconds downTo 1) {
                _uiState.value = _uiState.value.copy(resendCooldownSeconds = remaining)
                delay(1_000)
            }
            _uiState.value = _uiState.value.copy(resendCooldownSeconds = 0)
        }
    }

    private fun extractCooldownSeconds(message: String?): Int {
        val matchedSeconds = message
            ?.let(waitSecondsRegex::find)
            ?.groupValues
            ?.getOrNull(1)
            ?.toIntOrNull()
            ?: return 0
        return max(0, matchedSeconds)
    }
}
