package com.baothanhbin.core.model

object LatestDiagnoseResultCache {
    private var latestImageUri: String? = null
    private var latestDiagnoseData: DiagnoseData? = null

    fun store(imageUri: String?, diagnoseData: DiagnoseData) {
        latestImageUri = imageUri
        latestDiagnoseData = diagnoseData
    }

    fun get(imageUri: String?): DiagnoseData? {
        val cached = latestDiagnoseData ?: return null
        return if (latestImageUri == null || latestImageUri == imageUri) cached else null
    }

    fun clear() {
        latestImageUri = null
        latestDiagnoseData = null
    }
}
