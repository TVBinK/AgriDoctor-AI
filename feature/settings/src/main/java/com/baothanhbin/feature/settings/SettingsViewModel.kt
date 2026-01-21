package com.baothanhbin.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: com.baothanhbin.core.data.repository.AuthRepository
) : ViewModel() {

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }
}

