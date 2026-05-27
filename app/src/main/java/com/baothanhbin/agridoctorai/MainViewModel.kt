package com.baothanhbin.agridoctorai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.baothanhbin.core.data.impl.HistorySyncRepository
import com.baothanhbin.core.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    authRepository: AuthRepository,
    private val historySyncRepository: HistorySyncRepository
) : ViewModel() {
    val isLoggedIn: StateFlow<Boolean> = authRepository.isLoggedIn
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = authRepository.isUserLoggedIn() 
        )

    init {
        viewModelScope.launch {
            isLoggedIn.collectLatest { loggedIn ->
                if (loggedIn) {
                    historySyncRepository.syncFromServer()
                }
            }
        }
    }
}
