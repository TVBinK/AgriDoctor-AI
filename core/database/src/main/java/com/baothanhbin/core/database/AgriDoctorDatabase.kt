package com.baothanhbin.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.baothanhbin.core.database.converter.ChatMessageListConverter
import com.baothanhbin.core.database.converter.RecoveryCareDataListConverter
import com.baothanhbin.core.database.converter.StringListConverter
import com.baothanhbin.core.database.converter.TreatmentDataListConverter
import com.baothanhbin.core.database.dao.ChatDao
import com.baothanhbin.core.database.dao.DiagnoseResultDao
import com.baothanhbin.core.database.model.ChatEntity
import com.baothanhbin.core.database.model.DiagnoseResultEntity

@Database(
    entities = [DiagnoseResultEntity::class, ChatEntity::class],
    version = 4, // Tăng version vì thêm ChatEntity
    exportSchema = false
)
@TypeConverters(
    StringListConverter::class,
    TreatmentDataListConverter::class,
    RecoveryCareDataListConverter::class,
    ChatMessageListConverter::class
)
abstract class AgriDoctorDatabase : RoomDatabase() {
    abstract fun diagnoseResultDao(): DiagnoseResultDao
    abstract fun chatDao(): ChatDao
}

