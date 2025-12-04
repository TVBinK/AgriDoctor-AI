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
import com.baothanhbin.core.database.dao.DiseaseDao
import com.baothanhbin.core.database.model.ChatEntity
import com.baothanhbin.core.database.model.DiagnoseResultEntity
import com.baothanhbin.core.database.model.DiseaseListEntity

@Database(
    entities = [DiagnoseResultEntity::class, ChatEntity::class, DiseaseListEntity::class],
    version = 6, // Tăng version vì thêm bảng lưu cache danh sách bệnh
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
    abstract fun diseaseDao(): DiseaseDao
}

