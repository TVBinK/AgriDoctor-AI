package com.baothanhbin.core.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Lưu cache danh sách bệnh cây (JSON raw) lấy từ API /api/diseases.
 * Dùng để cung cấp ngữ cảnh cho Gemini khi tư vấn bệnh cây.
 */
@Entity(tableName = "diseases_cache")
data class DiseaseListEntity(
    @PrimaryKey val id: Int = 0, // Luôn chỉ có 1 bản ghi cache
    val json: String,
    val updatedAt: Long = System.currentTimeMillis()
)


