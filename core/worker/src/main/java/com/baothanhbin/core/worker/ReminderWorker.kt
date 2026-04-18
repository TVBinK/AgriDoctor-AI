package com.baothanhbin.core.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.baothanhbin.core.data.repository.PlantRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class ReminderWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val plantRepository: PlantRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val plantName = inputData.getString(KEY_PLANT_NAME) ?: DEFAULT_PLANT_NAME
        val actionName = inputData.getString(KEY_ACTION_NAME) ?: DEFAULT_ACTION_NAME
        val reminderId = inputData.getLong(KEY_REMINDER_ID, INVALID_REMINDER_ID)

        android.util.Log.d(
            LOG_TAG,
            "Bat dau thuc thi Worker cho cay: $plantName, hanh dong: $actionName, reminderId=$reminderId"
        )

        val notified = NotificationHelper.showCareNotification(context, plantName, actionName)
        if (notified) {
            android.util.Log.d(LOG_TAG, "Da hien thi Notification cho $plantName")
        } else {
            android.util.Log.e(
                LOG_TAG,
                "Khong the hien thi Notification do thieu quyen (POST_NOTIFICATIONS)"
            )
        }

        if (reminderId != INVALID_REMINDER_ID) {
            plantRepository.updateReminderStatus(reminderId, true)
            android.util.Log.d(LOG_TAG, "Da auto-complete reminderId=$reminderId")
        }

        return Result.success()
    }

    companion object {
        const val KEY_PLANT_NAME = "plantName"
        const val KEY_ACTION_NAME = "actionName"
        const val KEY_REMINDER_ID = "reminderId"

        private const val INVALID_REMINDER_ID = -1L
        private const val DEFAULT_PLANT_NAME = "Cay cua ban"
        private const val DEFAULT_ACTION_NAME = "Cham soc"
        private const val LOG_TAG = "ReminderWorker"

        fun uniqueWorkName(reminderId: Long): String = "Reminder_$reminderId"
    }
}
