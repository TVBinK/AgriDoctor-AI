package com.baothanhbin.feature.diagnose

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.baothanhbin.core.data.repository.DiagnoseResultRepository
import com.baothanhbin.core.data.repository.PlantRepository
import com.baothanhbin.core.database.model.DiagnoseResultEntity
import com.baothanhbin.core.database.model.PlantEntity
import com.baothanhbin.core.database.model.toClassifyData
import com.baothanhbin.core.database.model.toDiagnoseData
import com.baothanhbin.core.model.ApiType
import com.baothanhbin.core.model.ClassifyData
import com.baothanhbin.core.model.LatestDiagnoseResultCache
import com.baothanhbin.core.ui.util.LocationHelper
import com.baothanhbin.core.ui.util.LocationStateHolder
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

sealed class DiagnoseNavigationEvent {
    data class NavigateToResult(
        val imageUri: Uri?,
        val apiType: ApiType,
        val classifyData: ClassifyData? = null
    ) : DiagnoseNavigationEvent()

    data object NavigateToCamera : DiagnoseNavigationEvent()
}

@HiltViewModel
class DiagnoseViewModel @Inject constructor(
    application: Application,
    private val diagnoseResultRepository: DiagnoseResultRepository,
    private val plantRepository: PlantRepository
) : AndroidViewModel(application) {

    private val _historyItems = MutableStateFlow<List<DiagnoseResultEntity>>(emptyList())
    val historyItems: StateFlow<List<DiagnoseResultEntity>> = _historyItems.asStateFlow()

    private val _plantHistoryItems = MutableStateFlow<List<PlantEntity>>(emptyList())
    val plantHistoryItems: StateFlow<List<PlantEntity>> = _plantHistoryItems.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<DiagnoseNavigationEvent>()
    val navigationEvent: SharedFlow<DiagnoseNavigationEvent> = _navigationEvent.asSharedFlow()

    private val _showLocationDialog = MutableStateFlow(false)
    val showLocationDialog: StateFlow<Boolean> = _showLocationDialog.asStateFlow()

    private val _locationError = MutableSharedFlow<String>()
    val locationError: SharedFlow<String> = _locationError.asSharedFlow()

    init {
        observeDiagnoseHistory()
        observePlantHistory()
    }

    fun loadHistory() {
        // History is observed reactively from Room.
    }

    private fun observeDiagnoseHistory() {
        viewModelScope.launch {
            diagnoseResultRepository.observeAllDiagnoseResults().collect { diagnoseResults ->
                _historyItems.value = diagnoseResults
            }
        }
    }

    private fun observePlantHistory() {
        viewModelScope.launch {
            plantRepository.getRecognizedPlants().collect { plants ->
                _plantHistoryItems.value = plants
            }
        }
    }

    fun deleteHistoryItem(item: DiagnoseResultEntity) {
        viewModelScope.launch {
            diagnoseResultRepository.deleteDiagnoseResult(item.id)
        }
    }

    fun deletePlantHistoryItem(item: PlantEntity) {
        viewModelScope.launch {
            plantRepository.deletePlant(item.id)
        }
    }

    fun onHistoryItemClick(item: DiagnoseResultEntity) {
        viewModelScope.launch {
            LatestDiagnoseResultCache.store(item.imageUri, item.toDiagnoseData())
            _navigationEvent.emit(
                DiagnoseNavigationEvent.NavigateToResult(
                    imageUri = item.imageUri?.let(Uri::parse),
                    apiType = ApiType.DETECT
                )
            )
        }
    }

    fun onPlantHistoryItemClick(item: PlantEntity) {
        viewModelScope.launch {
            _navigationEvent.emit(
                DiagnoseNavigationEvent.NavigateToResult(
                    imageUri = item.imageUri?.let(Uri::parse),
                    apiType = ApiType.CLASSIFY,
                    classifyData = item.toClassifyData()
                )
            )
        }
    }

    fun onAutoDiagnoseClick() {
        viewModelScope.launch {
            _navigationEvent.emit(DiagnoseNavigationEvent.NavigateToCamera)
        }
    }

    fun checkAndGetLocation(context: Context, locationStateHolder: LocationStateHolder) {
        if (!LocationHelper.hasLocationPermission(context)) {
            _showLocationDialog.value = true
            return
        }

        getCurrentLocation(context, locationStateHolder)
    }

    @SuppressLint("MissingPermission")
    fun getCurrentLocation(context: Context, locationStateHolder: LocationStateHolder) {
        LocationHelper.getCurrentLocation(
            context = context,
            onLocationReceived = { location ->
                viewModelScope.launch {
                    val address = LocationHelper.getAddressFromLocation(
                        context = context,
                        latitude = location.latitude,
                        longitude = location.longitude
                    )
                    locationStateHolder.updateAddress(address)
                }
            },
            onError = { exception ->
                Log.e("DiagnoseViewModel", "Lá»—i láº¥y vá»‹ trÃ­: ${exception.message}", exception)
                viewModelScope.launch {
                    _locationError.emit(exception.message ?: "KhÃ´ng thá»ƒ láº¥y vá»‹ trÃ­")
                }
            }
        )
    }

    fun onPermissionGranted(context: Context, locationStateHolder: LocationStateHolder) {
        _showLocationDialog.value = false
        getCurrentLocation(context, locationStateHolder)
    }

    fun onPermissionDenied() {
        _showLocationDialog.value = false
    }

    fun showLocationDialog() {
        _showLocationDialog.value = true
    }

    fun hideLocationDialog() {
        _showLocationDialog.value = false
    }

    fun shouldLoadLocationOnStart(context: Context, locationStateHolder: LocationStateHolder): Boolean {
        return LocationHelper.hasLocationPermission(context) &&
            locationStateHolder.currentAddress == null
    }
}
