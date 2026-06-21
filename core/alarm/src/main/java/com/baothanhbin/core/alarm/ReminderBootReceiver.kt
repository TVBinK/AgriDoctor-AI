package com.baothanhbin.core.alarm

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.baothanhbin.core.data.repository.PlantRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ReminderBootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var plantRepository: PlantRepository

    override fun onReceive(context: Context, intent: Intent) {
        if (
            intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED
        ) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val now = System.currentTimeMillis()
                plantRepository.getAllReminders().first()
                    .filter { !it.isCompleted && it.targetTimestamp > now }
                    .forEach { reminder ->
                        ReminderAlarmScheduler.schedule(
                            context = context,
                            reminderId = reminder.id,
                            plantName = reminder.plantName,
                            actionName = reminder.actionName,
                            targetTimestamp = reminder.targetTimestamp
                        )
                    }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
