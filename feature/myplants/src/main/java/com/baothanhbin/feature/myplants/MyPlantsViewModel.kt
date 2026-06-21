package com.baothanhbin.feature.myplants

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.baothanhbin.core.data.repository.PlantRepository
import com.baothanhbin.core.database.model.PlantEntity
import com.baothanhbin.core.database.model.ReminderEntity
import com.baothanhbin.core.database.model.displayName
import com.baothanhbin.core.ui.util.LocationHelper
import com.baothanhbin.core.ui.util.LocationStateHolder
import com.baothanhbin.core.alarm.ReminderAlarmScheduler
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
class MyPlantsViewModel @Inject constructor(
    application: Application,
    private val plantRepository: PlantRepository
) : AndroidViewModel(application) {
    private var hasAttemptedInitialLocationLoad = false

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
            Log.e("ReminderAlarm", "Loi: Thoi gian hen ($targetTimestamp) phai lon hon hien tai (${System.currentTimeMillis()})")
            viewModelScope.launch {
                _locationError.emit("Thời gian hẹn phải lớn hơn thời gian hiện tại")
            }
            return
        }

        Log.d("ReminderAlarm", "Dang len lich nhac nho cho '$plantName' tai $targetTimestamp")

        viewModelScope.launch {
            val reminder = ReminderEntity(
                plantId = plant.id,
                plantName = plantName,
                actionName = actionName,
                targetTimestamp = targetTimestamp,
                isCompleted = false
            )
            val reminderId = plantRepository.insertReminder(reminder)
            val isExact = ReminderAlarmScheduler.schedule(
                context = context.applicationContext,
                reminderId = reminderId,
                plantName = plantName,
                actionName = actionName,
                targetTimestamp = targetTimestamp
            )
            if (!isExact) {
                ReminderAlarmScheduler.requestExactAlarmAccess(context.applicationContext)
            }
            Log.d("ReminderAlarm", "Da len lich reminderId=$reminderId, exact=$isExact")
        }
    }

    fun toggleReminderStatus(id: Long, isCompleted: Boolean) {
        viewModelScope.launch {
            plantRepository.updateReminderStatus(id, isCompleted)
            if (isCompleted) {
                ReminderAlarmScheduler.cancel(getApplication(), id)
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
        if (hasAttemptedInitialLocationLoad) {
            return false
        }

        val shouldLoad = LocationHelper.hasLocationPermission(context) &&
            locationStateHolder.currentAddress == null

        if (shouldLoad) {
            hasAttemptedInitialLocationLoad = true
        }

        return shouldLoad
    }
}
