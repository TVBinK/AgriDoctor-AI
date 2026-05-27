package com.baothanhbin.core.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val ownerUserId: String = "",
    val plantId: Long? = null,
    val plantName: String,
    val targetTimestamp: Long,
    val isCompleted: Boolean = false,
    val actionName: String = "Tưới nước"
)
