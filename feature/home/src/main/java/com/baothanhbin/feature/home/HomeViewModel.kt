package com.baothanhbin.feature.home

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.location.Location
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.baothanhbin.core.ui.util.LocationHelper
import com.baothanhbin.core.ui.util.LocationStateHolder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import androidx.work.WorkManager
import com.baothanhbin.core.data.repository.PlantRepository
import com.baothanhbin.core.database.model.PlantEntity
import com.baothanhbin.core.database.model.ReminderEntity
import com.baothanhbin.core.worker.ReminderWorker
import javax.inject.Inject

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
            // Chưa có permission, hiển thị dialog yêu cầu permission
            _showLocationDialog.value = true
            return
        }

        // Đã có permission, lấy location
        getCurrentLocation(context, locationStateHolder)
    }

    @SuppressLint("MissingPermission")
    fun getCurrentLocation(context: Context, locationStateHolder: LocationStateHolder) {
        try {
            LocationHelper.getCurrentLocation(
                context = context,
                onLocationReceived = { location ->
                    Log.d("HomeViewModel", "Vị trí: Latitude=${location.latitude}, Longitude=${location.longitude}")
                    
                    // Lấy địa chỉ từ tọa độ
                    viewModelScope.launch {
                        val address = LocationHelper.getAddressFromLocation(
                            context = context,
                            latitude = location.latitude,
                            longitude = location.longitude
                        )
                        locationStateHolder.updateAddress(address)
                        Log.d("HomeViewModel", "Địa chỉ: $address")
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
            // Xử lý SecurityException nếu permission bị từ chối
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

    /**
     * Khởi tạo và load location khi screen start
     * Nên gọi từ LaunchedEffect trong Composable
     */
    fun initializeLocation(context: Context, locationStateHolder: LocationStateHolder) {
        if (shouldLoadLocationOnStart(context, locationStateHolder)) {
            getCurrentLocation(context, locationStateHolder)
        }
    }
}

