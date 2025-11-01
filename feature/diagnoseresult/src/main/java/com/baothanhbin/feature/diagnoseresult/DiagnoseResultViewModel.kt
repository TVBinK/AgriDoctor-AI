package com.baothanhbin.feature.diagnoseresult

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.baothanhbin.core.data.repository.DiagnoseResultRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoadedResult(
    val diseaseName: String,
    val possibleProblems: List<String>,
    val symptoms: String,
    val causes: String,
    val treatment: List<com.baothanhbin.core.model.TreatmentItem>,
    val recoveryCare: List<com.baothanhbin.core.model.RecoveryItem>
)

@HiltViewModel
class DiagnoseResultViewModel @Inject constructor(
    application: Application,
    private val diagnoseResultRepository: DiagnoseResultRepository
) : AndroidViewModel(application) {

    private val _loadedResult = MutableStateFlow<LoadedResult?>(null)
    val loadedResult: StateFlow<LoadedResult?> = _loadedResult.asStateFlow()

    fun loadLatestResult() {
        viewModelScope.launch {
            val latestResult = diagnoseResultRepository.getLatestDiagnoseResult()
            
            if (latestResult != null) {
                _loadedResult.value = LoadedResult(
                    diseaseName = latestResult.diseaseName,
                    possibleProblems = latestResult.possibleProblems,
                    symptoms = latestResult.symptoms,
                    causes = latestResult.causes,
                    treatment = latestResult.treatment.map {
                        com.baothanhbin.core.model.TreatmentItem(
                            title = it.title,
                            subtitle = it.subtitle,
                            steps = it.steps,
                            linkText = it.linkText
                        )
                    },
                    recoveryCare = latestResult.recoveryCare.map {
                        com.baothanhbin.core.model.RecoveryItem(
                            title = it.title,
                            steps = it.steps,
                            linkText = it.linkText
                        )
                    }
                )
            }
        }
    }
}
