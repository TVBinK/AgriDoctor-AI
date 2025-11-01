package com.baothanhbin.core.database.converter

import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.serializer

class StringListConverter {
    private val json = Json { ignoreUnknownKeys = true }
    private val stringListSerializer = ListSerializer(kotlinx.serialization.serializer<String>())
    
    @TypeConverter
    fun fromString(value: String?): List<String> {
        if (value == null || value.isEmpty()) return emptyList()
        return try {
            json.decodeFromString(stringListSerializer, value)
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    @TypeConverter
    fun toString(list: List<String>): String {
        return json.encodeToString(stringListSerializer, list)
    }
}

