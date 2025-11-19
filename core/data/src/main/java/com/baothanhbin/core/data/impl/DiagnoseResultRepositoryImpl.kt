package com.baothanhbin.core.data.impl

import android.content.Context
import android.util.Log
import com.baothanhbin.core.data.repository.DiagnoseResultRepository
import com.baothanhbin.core.database.AgriDoctorDatabase
import com.baothanhbin.core.database.model.DiagnoseResultEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class DiagnoseResultRepositoryImpl @Inject constructor(
    private val database: AgriDoctorDatabase,
    @ApplicationContext private val context: Context
) : DiagnoseResultRepository {

    companion object {
        private const val TAG = "DiagnoseResultRepository"
        private const val DATABASE_NAME = "agridoctor_database"
    }
    
    /**
     * Đọc raw bytes từ database file để xem dữ liệu đã mã hóa
     */
    private fun readEncryptedDataFromDatabase(): String? {
        return try {
            val dbFile = context.getDatabasePath(DATABASE_NAME)
            if (dbFile.exists()) {
                val fileBytes = dbFile.readBytes()
                // Chỉ hiển thị 512 bytes đầu tiên để không làm log quá dài
                val previewBytes = fileBytes.take(512)
                val hexString = previewBytes.joinToString("") { "%02x".format(it) }
                val base64String = android.util.Base64.encodeToString(
                    previewBytes.toByteArray(),
                    android.util.Base64.NO_WRAP
                )
                "Hex (first 512 bytes): $hexString\nBase64 (first 512 bytes): $base64String\nTotal file size: ${fileBytes.size} bytes"
            } else {
                "Database file not found"
            }
        } catch (e: Exception) {
            "Error reading database file: ${e.message}"
        }
    }

    override suspend fun insertDiagnoseResult(diagnoseResult: DiagnoseResultEntity): Long {
        // Log dữ liệu TRƯỚC KHI MÃ HÓA (plaintext - dữ liệu gốc)
        Log.d(TAG, "========== TRƯỚC KHI MÃ HÓA (PLAINTEXT) ==========")
        Log.d(TAG, "ID: ${diagnoseResult.id}")
        Log.d(TAG, "Disease Name: ${diagnoseResult.diseaseName}")
        Log.d(TAG, "Timestamp: ${diagnoseResult.timestamp}")
        Log.d(TAG, "Image URI: ${diagnoseResult.imageUri}")
        Log.d(TAG, "Location: ${diagnoseResult.location}")
        Log.d(TAG, "Possible Problems: ${diagnoseResult.possibleProblems}")
        Log.d(TAG, "Symptoms: ${diagnoseResult.symptoms}")
        Log.d(TAG, "Causes: ${diagnoseResult.causes}")
        Log.d(TAG, "Treatment count: ${diagnoseResult.treatment.size}")
        diagnoseResult.treatment.forEachIndexed { index, treatment ->
            Log.d(TAG, "  Treatment[$index]: ${treatment}")
        }
        Log.d(TAG, "Recovery Care count: ${diagnoseResult.recoveryCare.size}")
        diagnoseResult.recoveryCare.forEachIndexed { index, recovery ->
            Log.d(TAG, "  Recovery Care[$index]: ${recovery}")
        }
        Log.d(TAG, "==================================================")

        // Lưu vào database (SQLCipher sẽ tự động mã hóa ở mức database)
        val resultId = database.diagnoseResultDao().insertDiagnoseResult(diagnoseResult)
        
        // Đọc raw bytes từ database file để xem dữ liệu ĐÃ MÃ HÓA
        Log.d(TAG, "========== DỮ LIỆU ĐÃ MÃ HÓA (RAW BYTES TỪ DATABASE FILE) ==========")
        val encryptedData = readEncryptedDataFromDatabase()
        Log.d(TAG, encryptedData ?: "Không thể đọc dữ liệu đã mã hóa")
        Log.d(TAG, "⚠️ Lưu ý: Đây là raw bytes từ database file đã được SQLCipher mã hóa")
        Log.d(TAG, "⚠️ Dữ liệu trong file không thể đọc được bằng SQLite thông thường")
        Log.d(TAG, "================================================================")
        
        // Đọc lại từ database để xác nhận dữ liệu SAU KHI GIẢI MÃ (để verify mã hóa/giải mã hoạt động đúng)
        val savedEntity = database.diagnoseResultDao().getDiagnoseResultById(resultId)
        
        if (savedEntity != null) {
            Log.d(TAG, "========== SAU KHI GIẢI MÃ (TỪ DATABASE) ==========")
            Log.d(TAG, "ID: ${savedEntity.id}")
            Log.d(TAG, "Disease Name: ${savedEntity.diseaseName}")
            Log.d(TAG, "Timestamp: ${savedEntity.timestamp}")
            Log.d(TAG, "Image URI: ${savedEntity.imageUri}")
            Log.d(TAG, "Location: ${savedEntity.location}")
            Log.d(TAG, "Possible Problems: ${savedEntity.possibleProblems}")
            Log.d(TAG, "Symptoms: ${savedEntity.symptoms}")
            Log.d(TAG, "Causes: ${savedEntity.causes}")
            Log.d(TAG, "Treatment count: ${savedEntity.treatment.size}")
            savedEntity.treatment.forEachIndexed { index, treatment ->
                Log.d(TAG, "  Treatment[$index]: ${treatment}")
            }
            Log.d(TAG, "Recovery Care count: ${savedEntity.recoveryCare.size}")
            savedEntity.recoveryCare.forEachIndexed { index, recovery ->
                Log.d(TAG, "  Recovery Care[$index]: ${recovery}")
            }
            Log.d(TAG, "===================================================")
            
            // So sánh để xác nhận dữ liệu giống nhau
            val isDataMatch = diagnoseResult.diseaseName == savedEntity.diseaseName &&
                    diagnoseResult.location == savedEntity.location &&
                    diagnoseResult.imageUri == savedEntity.imageUri
            Log.d(TAG, "✅ Dữ liệu sau giải mã khớp với dữ liệu gốc: $isDataMatch")
        } else {
            Log.w(TAG, "⚠️ Không thể đọc lại dữ liệu sau khi lưu")
        }
        
        return resultId
    }

    override suspend fun getDiagnoseResultById(id: Long): DiagnoseResultEntity? {
        return database.diagnoseResultDao().getDiagnoseResultById(id)
    }

    override suspend fun getLatestDiagnoseResult(): DiagnoseResultEntity? {
        return database.diagnoseResultDao().getLatestDiagnoseResult()
    }

    override suspend fun getAllDiagnoseResults(): List<DiagnoseResultEntity> {
        return database.diagnoseResultDao().getAllDiagnoseResults()
    }

    override suspend fun deleteDiagnoseResult(id: Long) {
        database.diagnoseResultDao().deleteDiagnoseResult(id)
    }

    override suspend fun deleteAllDiagnoseResults() {
        database.diagnoseResultDao().deleteAllDiagnoseResults()
    }
}
