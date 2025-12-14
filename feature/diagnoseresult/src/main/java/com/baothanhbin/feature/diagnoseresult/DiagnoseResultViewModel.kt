package com.baothanhbin.feature.diagnoseresult

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.baothanhbin.core.data.repository.DiagnoseResultRepository
import com.baothanhbin.core.model.RecoveryItem
import com.baothanhbin.core.model.TreatmentItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DiagnoseResultUiState(
    val isLoading: Boolean = false,
    val diseaseName: String = "",
    val possibleProblems: List<String> = emptyList(),
    val symptoms: String = "",
    val causes: String = "",
    val treatment: List<TreatmentItem> = emptyList(),
    val recoveryCare: List<RecoveryItem> = emptyList(),
    val location: String? = null
)

@HiltViewModel
class DiagnoseResultViewModel @Inject constructor(
    application: Application,
    private val diagnoseResultRepository: DiagnoseResultRepository
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(DiagnoseResultUiState())
    val uiState: StateFlow<DiagnoseResultUiState> = _uiState.asStateFlow()

    fun loadDiagnoseResult(imageUri: Uri?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            // Mock result or fetch from repository actually
            // If repository has getLatestDiagnoseResult, use it.
            // The previous code used diagnoseResultRepository.getLatestDiagnoseResult()
            // But checking the previous file view, it seems `getLatestDiagnoseResult` returns a different model.
            
            val latestResult = diagnoseResultRepository.getLatestDiagnoseResult()
            
             if (latestResult != null) {
                 _uiState.value = _uiState.value.copy(
                    isLoading = false,
                     diseaseName = latestResult.diseaseName,
                     possibleProblems = latestResult.possibleProblems,
                     symptoms = latestResult.symptoms,
                     causes = latestResult.causes,
                     treatment = latestResult.treatment.map {
                         TreatmentItem(
                             title = it.title,
                             subtitle = it.subtitle,
                             steps = it.steps,
                             linkText = it.linkText
                         )
                     },
                     recoveryCare = latestResult.recoveryCare.map {
                         RecoveryItem(
                             title = it.title,
                             steps = it.steps
                         )
                     },
                     location = latestResult.location
                 )
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }
}
