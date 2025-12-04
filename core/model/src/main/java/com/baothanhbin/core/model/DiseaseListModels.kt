package com.baothanhbin.core.model

import kotlinx.serialization.Serializable

/**
 * Model dữ liệu cho API /api/diseases
 * Chỉ giữ những field cần thiết để tóm tắt, tránh phụ thuộc toàn bộ schema.
 */
@Serializable
data class DiseaseListResponse(
    val success: Boolean,
    val count: Int,
    val data: List<DiseaseItem>
)

@Serializable
data class DiseaseItem(
    val code: String,
    val diseaseName: String,
    val symptoms: String
)


