package com.baothanhbin.core.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings

object ReminderAlarmScheduler {

    fun schedule(
        context: Context,
        reminderId: Long,
        plantName: String,
        actionName: String,
        targetTimestamp: Long
    ): Boolean {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        val pendingIntent = pendingIntent(context, reminderId, plantName, actionName)
        val canScheduleExact = Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
            alarmManager.canScheduleExactAlarms()

        if (canScheduleExact) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                targetTimestamp,
                pendingIntent
            )
        } else {
            // Keep the reminder functional until the user grants exact-alarm access.
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                targetTimestamp,
                pendingIntent
            )
        }
        return canScheduleExact
    }

    fun cancel(context: Context, reminderId: Long) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        alarmManager.cancel(pendingIntent(context, reminderId))
    }

    fun requestExactAlarmAccess(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        if (alarmManager.canScheduleExactAlarms()) return

        context.startActivity(
            Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                data = Uri.parse("package:${context.packageName}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        )
    }

    private fun pendingIntent(
        context: Context,
        reminderId: Long,
        plantName: String? = null,
        actionName: String? = null
    ): PendingIntent {
        val intent = Intent(context, ReminderAlarmReceiver::class.java).apply {
            action = ACTION_REMINDER
            putExtra(EXTRA_REMINDER_ID, reminderId)
            plantName?.let { putExtra(EXTRA_PLANT_NAME, it) }
            actionName?.let { putExtra(EXTRA_ACTION_NAME, it) }
        }
        return PendingIntent.getBroadcast(
            context,
            reminderId.hashCode(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    const val EXTRA_REMINDER_ID = "reminderId"
    const val EXTRA_PLANT_NAME = "plantName"
    const val EXTRA_ACTION_NAME = "actionName"
    private const val ACTION_REMINDER = "com.baothanhbin.agridoctorai.CARE_REMINDER"
}
