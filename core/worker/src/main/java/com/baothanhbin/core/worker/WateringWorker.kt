package com.baothanhbin.core.worker

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import com.baothanhbin.agridoctorai.resources.R

@HiltWorker
class WateringWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val plantName = inputData.getString("plantName") ?: "Cây của bạn"
        val actionName = inputData.getString("actionName") ?: "Chăm sóc"
        
        android.util.Log.d("WateringWorker", "Bat dau thuc thi Worker cho cay: $plantName, hanh dong: $actionName")
        val notified = NotificationHelper.showCareNotification(context, plantName, actionName)
        if (notified) {
            android.util.Log.d("WateringWorker", "Da hien thi Notification cho $plantName")
        } else {
            android.util.Log.e("WateringWorker", "Khong the hien thi Notification do thieu quyen (POST_NOTIFICATIONS)")
        }
        
        return Result.success()
    }
}
