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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.Data
import androidx.work.WorkManager
import androidx.work.ExistingWorkPolicy
import com.baothanhbin.core.data.repository.PlantRepository
import com.baothanhbin.core.database.model.PlantEntity
import com.baothanhbin.core.worker.ReminderWorker
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import com.baothanhbin.core.database.model.displayName
import com.baothanhbin.core.database.model.ReminderEntity

@HiltViewModel
class MyPlantsViewModel @Inject constructor(
    application: Application,
    private val plantRepository: PlantRepository
) : AndroidViewModel(application) {

    private val _showLocationDialog = MutableStateFlow(false)
    val showLocationDialog: StateFlow<Boolean> = _showLocationDialog.asStateFlow()

    private val _locationError = MutableSharedFlow<String>()
    val locationError: SharedFlow<String> = _locationError.asSharedFlow()

    val myPlants: StateFlow<List<PlantEntity>> = plantRepository.getAllPlants()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val reminders: StateFlow<List<ReminderEntity>> = plantRepository.getAllReminders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addPlant(plantName: String, imageUri: String?, location: String?) {
        viewModelScope.launch {
            val plant = PlantEntity(
                plantName = plantName,
                imageUri = imageUri,
                location = location
            )
            plantRepository.insertPlant(plant)
        }
    }

    fun deletePlant(plant: PlantEntity) {
        viewModelScope.launch {
            plantRepository.deletePlant(plant.id)
        }
    }

    fun scheduleCareReminder(context: Context, plant: PlantEntity, actionName: String, targetTimestamp: Long) {
        val plantName = plant.displayName()
        val delay = targetTimestamp - System.currentTimeMillis()
        if (delay <= 0) {
            Log.e("ReminderWorker", "Loi: Thoi gian hen ($targetTimestamp) phai lon hon hien tai (${System.currentTimeMillis()})")
            viewModelScope.launch {
                _locationError.emit("Thời gian hẹn phải lớn hơn thời gian hiện tại")
            }
            return
        }

        val minutes = TimeUnit.MILLISECONDS.toMinutes(delay)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(delay) % 60
        Log.d("ReminderWorker", "Dang len lich nhac nho cho '$plantName' sau: $minutes phut $seconds giay (Tong: $delay ms)")

        viewModelScope.launch {
            val reminder = ReminderEntity(
                plantId = plant.id,
                plantName = plantName,
                actionName = actionName,
                targetTimestamp = targetTimestamp,
                isCompleted = false
            )
            val reminderId = plantRepository.insertReminder(reminder)
            val workerInputData = Data.Builder()
                .putString(ReminderWorker.KEY_PLANT_NAME, plantName)
                .putString(ReminderWorker.KEY_ACTION_NAME, actionName)
                .putLong(ReminderWorker.KEY_REMINDER_ID, reminderId)
                .build()
            val workRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(workerInputData)
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                ReminderWorker.uniqueWorkName(reminderId),
                ExistingWorkPolicy.REPLACE,
                workRequest
            )

            Log.d("ReminderWorker", "enqueueUniqueWork thanh cong cho reminderId=$reminderId")
        }
    }
    
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

