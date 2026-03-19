package com.baothanhbin.feature.myplants

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
import kotlinx.coroutines.launch
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.Data
import androidx.work.WorkManager
import androidx.work.ExistingWorkPolicy
import androidx.work.ExistingPeriodicWorkPolicy
import com.baothanhbin.core.data.repository.PlantRepository
import com.baothanhbin.core.database.model.PlantEntity
import com.baothanhbin.core.worker.WateringWorker
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class MyPlantsViewModel @Inject constructor(
    application: Application,
    private val plantRepository: PlantRepository
) : AndroidViewModel(application) {

    private val _showLocationDialog = MutableStateFlow(false)
    val showLocationDialog: StateFlow<Boolean> = _showLocationDialog.asStateFlow()

    private val _locationError = MutableSharedFlow<String>()
    val locationError: SharedFlow<String> = _locationError.asSharedFlow()

    private val _myPlants = MutableStateFlow<List<PlantEntity>>(emptyList())
    val myPlants: StateFlow<List<PlantEntity>> = _myPlants.asStateFlow()

    init {
        loadPlants()
    }

    private fun loadPlants() {
        viewModelScope.launch {
            _myPlants.value = plantRepository.getAllPlants()
        }
    }

    fun addPlant(plantName: String, imageUri: String?, location: String?) {
        viewModelScope.launch {
            val plant = PlantEntity(
                plantName = plantName,
                imageUri = imageUri,
                location = location
            )
            plantRepository.insertPlant(plant)
            loadPlants() // Refresh the list
        }
    }

    fun scheduleWateringReminder(context: Context, plantName: String, targetTimestamp: Long) {
        val delay = targetTimestamp - System.currentTimeMillis()
        if (delay <= 0) return

        val inputData = Data.Builder()
            .putString("plantName", plantName)
            .build()
            
        val workRequest = OneTimeWorkRequestBuilder<WateringWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(inputData)
            .build()
            
        WorkManager.getInstance(context).enqueueUniqueWork(
            "Watering_${plantName.hashCode()}",
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
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
                Log.d("MyPlantsViewModel", "Vị trí: Latitude=${location.latitude}, Longitude=${location.longitude}")
                
                // Lấy địa chỉ từ tọa độ
                viewModelScope.launch {
                    val address = LocationHelper.getAddressFromLocation(
                        context = context,
                        latitude = location.latitude,
                        longitude = location.longitude
                    )
                    locationStateHolder.updateAddress(address)
                    Log.d("MyPlantsViewModel", "Địa chỉ: $address")
                }
            },
            onError = { exception ->
                Log.e("MyPlantsViewModel", "Lỗi lấy vị trí: ${exception.message}", exception)
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

