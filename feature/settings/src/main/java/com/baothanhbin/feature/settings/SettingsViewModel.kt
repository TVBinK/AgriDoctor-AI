package com.baothanhbin.feature.settings

import android.content.Context
import com.baothanhbin.agridoctorai.resources.R
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.baothanhbin.core.data.repository.AuthRepository
import com.baothanhbin.core.model.ChangePasswordRequest
import com.baothanhbin.core.model.UpdateProfileRequest
import com.baothanhbin.core.model.UserProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class SettingsUiState(
    val isLoading: Boolean = true,
    val isSubmitting: Boolean = false,
    val profile: UserProfile? = null,
    val currentLanguageTag: String = "vi",
    val cacheSizeBytes: Long = 0L,
    val error: String? = null,
    val message: String? = null,
    val shouldLogout: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        refreshLocalSettings()
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
                        error = error.message ?: context.getString(R.string.settings_account_load_failed)
                    )
                }
        }
    }

    fun changeLanguage(languageTag: String) {
        val normalizedTag = if (languageTag == "en") "en" else "vi"
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(normalizedTag))
        _uiState.value = _uiState.value.copy(currentLanguageTag = normalizedTag)
    }

    fun clearCache() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmitting = true, error = null, message = null)
            val deletedBytes = withContext(Dispatchers.IO) {
                clearDirectory(context.cacheDir) + clearDirectory(context.externalCacheDir)
            }
            refreshLocalSettings()
            _uiState.value = _uiState.value.copy(
                isSubmitting = false,
                message = if (deletedBytes > 0L) {
                    context.getString(R.string.settings_cache_cleared)
                } else {
                    context.getString(R.string.settings_cache_already_empty)
                }
            )
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
                    error = error.message ?: context.getString(R.string.settings_update_profile_failed)
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
                authRepository.logout()
                _uiState.value = _uiState.value.copy(
                    isSubmitting = false,
                    message = response.message,
                    shouldLogout = true
                )
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isSubmitting = false,
                    error = error.message ?: context.getString(R.string.settings_change_password_failed)
                )
            }
        }
    }

    fun clearFeedback() {
        _uiState.value = _uiState.value.copy(error = null, message = null)
    }

    fun consumeLogoutRequired() {
        _uiState.value = _uiState.value.copy(shouldLogout = false)
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }

    private fun refreshLocalSettings() {
        val currentLanguageTag = AppCompatDelegate.getApplicationLocales()[0]?.toLanguageTag()
            ?.takeIf { it.isNotBlank() }
            ?.substringBefore('-')
            ?: "vi"
        val cacheSizeBytes = calculateCacheSize(context.cacheDir) + calculateCacheSize(context.externalCacheDir)
        _uiState.value = _uiState.value.copy(
            currentLanguageTag = currentLanguageTag,
            cacheSizeBytes = cacheSizeBytes
        )
    }

    private fun calculateCacheSize(directory: File?): Long {
        if (directory == null || !directory.exists()) return 0L
        return if (directory.isFile) {
            directory.length()
        } else {
            directory.listFiles()?.sumOf(::calculateCacheSize) ?: 0L
        }
    }

    private fun clearDirectory(directory: File?): Long {
        if (directory == null || !directory.exists()) return 0L
        var deletedBytes = 0L
        directory.listFiles()?.forEach { file ->
            deletedBytes += calculateCacheSize(file)
            if (file.isDirectory) {
                file.deleteRecursively()
            } else {
                file.delete()
            }
        }
        return deletedBytes
    }
}
