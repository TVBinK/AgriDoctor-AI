package com.baothanhbin.core.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.baothanhbin.core.data.repository.PlantRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ReminderAlarmReceiver : BroadcastReceiver() {

    @Inject
    lateinit var plantRepository: PlantRepository

    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getLongExtra(ReminderAlarmScheduler.EXTRA_REMINDER_ID, -1L)
        val plantName = intent.getStringExtra(ReminderAlarmScheduler.EXTRA_PLANT_NAME)
            ?: "Cay cua ban"
        val actionName = intent.getStringExtra(ReminderAlarmScheduler.EXTRA_ACTION_NAME)
            ?: "Cham soc"

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val notified = NotificationHelper.showCareNotification(
                    context,
                    plantName,
                    actionName,
                    reminderId
                )
                if (notified && reminderId >= 0) {
                    plantRepository.updateReminderStatus(reminderId, true)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
