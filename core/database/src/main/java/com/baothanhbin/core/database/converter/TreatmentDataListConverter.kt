package com.baothanhbin.core.database.converter

import androidx.room.TypeConverter
import com.baothanhbin.core.model.TreatmentData
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.serializer

class TreatmentDataListConverter {
    private val json = Json { ignoreUnknownKeys = true }
    private val treatmentDataListSerializer = ListSerializer(kotlinx.serialization.serializer<TreatmentData>())
    
    @TypeConverter
    fun fromString(value: String?): List<TreatmentData> {
        if (value == null || value.isEmpty()) return emptyList()
        return try {
            json.decodeFromString(treatmentDataListSerializer, value)
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    @TypeConverter
    fun toString(list: List<TreatmentData>): String {
        return json.encodeToString(treatmentDataListSerializer, list)
    }
}

