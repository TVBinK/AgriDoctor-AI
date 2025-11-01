package com.baothanhbin.feature.diagnose

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.baothanhbin.core.data.repository.DiagnoseResultRepository
import com.baothanhbin.core.database.model.DiagnoseResultEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class DiagnoseNavigationEvent {
    data class NavigateToResult(val imageUri: Uri?) : DiagnoseNavigationEvent()
}

@HiltViewModel
class DiagnoseViewModel @Inject constructor(
    application: Application,
    private val diagnoseResultRepository: DiagnoseResultRepository
) : AndroidViewModel(application) {

    private val _historyItems = MutableStateFlow<List<DiagnoseResultEntity>>(emptyList())
    val historyItems: StateFlow<List<DiagnoseResultEntity>> = _historyItems.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<DiagnoseNavigationEvent>()
    val navigationEvent: SharedFlow<DiagnoseNavigationEvent> = _navigationEvent.asSharedFlow()

    init {
        loadHistory()
    }

    fun loadHistory() {
        viewModelScope.launch {
            val results = diagnoseResultRepository.getAllDiagnoseResults()
            _historyItems.value = results
        }
    }

    fun onHistoryItemClick(entity: DiagnoseResultEntity) {
        viewModelScope.launch {
            val uri = entity.imageUri?.let { Uri.parse(it) }
            _navigationEvent.emit(DiagnoseNavigationEvent.NavigateToResult(uri))
        }
    }
}

