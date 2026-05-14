package com.baothanhbin.feature.home

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import com.baothanhbin.core.data.repository.PlantRepository
import com.baothanhbin.core.database.model.PlantEntity
import com.baothanhbin.core.database.model.ReminderEntity
import com.baothanhbin.core.ui.util.LocationHelper
import com.baothanhbin.core.ui.util.LocationStateHolder
import com.baothanhbin.core.worker.ReminderWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class HomeViewModel @Inject constructor(
    application: Application,
    private val plantRepository: PlantRepository
) : AndroidViewModel(application) {

    private val _showLocationDialog = MutableStateFlow(false)
    val showLocationDialog: StateFlow<Boolean> = _showLocationDialog.asStateFlow()

    private val _locationError = MutableSharedFlow<String>()
    val locationError: SharedFlow<String> = _locationError.asSharedFlow()

    val reminders: StateFlow<List<ReminderEntity>> = plantRepository.getAllReminders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val myPlants: StateFlow<List<PlantEntity>> = plantRepository.getAllPlants()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun toggleReminderStatus(id: Long, isCompleted: Boolean) {
        viewModelScope.launch {
            plantRepository.updateReminderStatus(id, isCompleted)
            if (isCompleted) {
                WorkManager.getInstance(getApplication()).cancelUniqueWork(
                    ReminderWorker.uniqueWorkName(id)
                )
            }
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
        try {
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
                    Log.e("HomeViewModel", "Lỗi lấy vị trí: ${exception.message}", exception)
                    viewModelScope.launch {
                        _locationError.emit(exception.message ?: "Không thể lấy vị trí")
                    }
                }
            )
        } catch (e: SecurityException) {
            Log.e("HomeViewModel", "SecurityException: ${e.message}", e)
            viewModelScope.launch {
                _locationError.emit(e.message ?: "Không có quyền truy cập vị trí")
            }
        }
    }

    fun onPermissionGranted(context: Context, locationStateHolder: LocationStateHolder) {
        _showLocationDialog.value = false
        getCurrentLocation(context, locationStateHolder)
    }

    fun onPermissionDenied() {
        _showLocationDialog.value = false
    }

    fun hideLocationDialog() {
        _showLocationDialog.value = false
    }

    fun shouldLoadLocationOnStart(context: Context, locationStateHolder: LocationStateHolder): Boolean {
        return LocationHelper.hasLocationPermission(context) &&
            locationStateHolder.currentAddress == null
    }

    fun initializeLocation(context: Context, locationStateHolder: LocationStateHolder) {
        if (shouldLoadLocationOnStart(context, locationStateHolder)) {
            getCurrentLocation(context, locationStateHolder)
        }
    }
}
