package com.baothanhbin.feature.diagnose

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.location.Location
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.baothanhbin.core.data.repository.DiagnoseResultRepository
import com.baothanhbin.core.database.model.DiagnoseResultEntity
import com.baothanhbin.core.database.model.PlantEntity
import com.baothanhbin.core.database.model.toClassifyData
import com.baothanhbin.core.model.ApiType
import com.baothanhbin.core.model.ClassifyData
import com.baothanhbin.core.ui.util.LocationHelper
import com.baothanhbin.core.ui.util.LocationStateHolder
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
    data class NavigateToResult(
        val imageUri: Uri?,
        val apiType: ApiType = ApiType.DETECT,
        val classifyData: ClassifyData? = null
    ) : DiagnoseNavigationEvent()

    data object NavigateToCamera : DiagnoseNavigationEvent()
}

@HiltViewModel
class DiagnoseViewModel @Inject constructor(
    application: Application,
    private val diagnoseResultRepository: DiagnoseResultRepository,
    private val plantRepository: com.baothanhbin.core.data.repository.PlantRepository
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
        loadHistory()
    }

    fun loadHistory() {
        viewModelScope.launch {
            val results = diagnoseResultRepository.getAllDiagnoseResults()
            _historyItems.value = results
            
            // Load plant history
            val plants = plantRepository.getAllPlants()
            _plantHistoryItems.value = plants
        }
    }

    fun onHistoryItemClick(entity: DiagnoseResultEntity) {
        viewModelScope.launch {
            val uri = entity.imageUri?.let { Uri.parse(it) }
            _navigationEvent.emit(DiagnoseNavigationEvent.NavigateToResult(uri))
        }
    }

    fun onPlantHistoryItemClick(entity: PlantEntity) {
        viewModelScope.launch {
            val uri = entity.imageUri?.let { Uri.parse(it) }
            val classifyData = entity.toClassifyData()
            _navigationEvent.emit(DiagnoseNavigationEvent.NavigateToResult(
                imageUri = uri,
                apiType = ApiType.CLASSIFY,
                classifyData = classifyData
            ))
        }
    }

    fun onAutoDiagnoseClick() {
        viewModelScope.launch {
            _navigationEvent.emit(DiagnoseNavigationEvent.NavigateToCamera)
        }
    }

    fun checkAndGetLocation(context: Context, locationStateHolder: LocationStateHolder) {
        if (!LocationHelper.hasLocationPermission(context)) {
            // Chưa có permission, hiển thị dialog yêu cầu permission
            _showLocationDialog.value = true
            return
        }

        // Đã có permission, lấy location
        getCurrentLocation(context, locationStateHolder)
    }

    @SuppressLint("MissingPermission")
    fun getCurrentLocation(context: Context, locationStateHolder: LocationStateHolder) {
        LocationHelper.getCurrentLocation(
            context = context,
            onLocationReceived = { location ->
                Log.d("DiagnoseViewModel", "Vị trí: Latitude=${location.latitude}, Longitude=${location.longitude}")
                
                // Lấy địa chỉ từ tọa độ
                viewModelScope.launch {
                    val address = LocationHelper.getAddressFromLocation(
                        context = context,
                        latitude = location.latitude,
                        longitude = location.longitude
                    )
                    locationStateHolder.updateAddress(address)
                    Log.d("DiagnoseViewModel", "Địa chỉ: $address")
                }
            },
            onError = { exception ->
                Log.e("DiagnoseViewModel", "Lỗi lấy vị trí: ${exception.message}", exception)
                viewModelScope.launch {
                    _locationError.emit(exception.message ?: "Không thể lấy vị trí")
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

