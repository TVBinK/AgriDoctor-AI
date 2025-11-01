package com.baothanhbin.core.database.converter

import androidx.room.TypeConverter
import com.baothanhbin.core.model.RecoveryCareData
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.serializer

class RecoveryCareDataListConverter {
    private val json = Json { ignoreUnknownKeys = true }
    private val recoveryCareDataListSerializer = ListSerializer(kotlinx.serialization.serializer<RecoveryCareData>())
    
    @TypeConverter
    fun fromString(value: String?): List<RecoveryCareData> {
        if (value == null || value.isEmpty()) return emptyList()
        return try {
            json.decodeFromString(recoveryCareDataListSerializer, value)
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    @TypeConverter
    fun toString(list: List<RecoveryCareData>): String {
        return json.encodeToString(recoveryCareDataListSerializer, list)
    }
}

