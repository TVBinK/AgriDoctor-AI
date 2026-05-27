package com.baothanhbin.core.data.impl

import android.content.Context
import android.net.Uri
import android.util.Log
import com.baothanhbin.core.data.repository.AuthRepository
import com.baothanhbin.core.data.repository.DiagnoseResultRepository
import com.baothanhbin.core.data.repository.PlantRepository
import com.baothanhbin.core.database.model.DiagnoseResultEntity
import com.baothanhbin.core.database.model.PlantEntity
import com.baothanhbin.core.network.NetworkDataSource
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@Singleton
class HistorySyncRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val authRepository: AuthRepository,
    private val diagnoseResultRepository: DiagnoseResultRepository,
    private val plantRepository: PlantRepository
) {
    companion object {
        private const val SYNC_IMAGES_DIRECTORY = "history_sync_images"
        private const val TAG = "HistorySyncRepository"
    }

    private val syncMutex = Mutex()

    suspend fun syncFromServer() {
        syncMutex.withLock {
            val token = authRepository.getToken() ?: return
            ensureCurrentUserId() ?: return

            val historyResponse = NetworkDataSource.getUserHistory(token)
                ?.takeIf { it.success }
                ?: return

            val cachedDiagnoseIds = diagnoseResultRepository.getExistingServerHistoryIds().toMutableSet()
            val cachedPlantIds = plantRepository.getExistingServerHistoryIds().toMutableSet()

            historyResponse.data
                .sortedBy { it.timestamp }
                .forEach { historyItem ->
                    when (historyItem.type.lowercase(Locale.ROOT)) {
                        "detect" -> {
                            val resultType = historyItem.resultType?.lowercase(Locale.ROOT)
                            if (
                                historyItem.id in cachedDiagnoseIds ||
                                historyItem.diseaseName.isNullOrBlank() ||
                                resultType != "diagnosed"
                            ) {
                                return@forEach
                            }

                            val imageUri = downloadImageToAppStorage(historyItem.id, token)
                            diagnoseResultRepository.insertDiagnoseResult(
                                historyItem.toDiagnoseResultEntity(imageUri)
                            )
                            cachedDiagnoseIds += historyItem.id
                        }

                        "classify" -> {
                            val resultType = historyItem.resultType?.lowercase(Locale.ROOT)
                            if (
                                historyItem.id in cachedPlantIds ||
                                historyItem.plantName.isNullOrBlank() ||
                                historyItem.rejectedInput ||
                                resultType != "classified"
                            ) {
                                return@forEach
                            }

                            val imageUri = downloadImageToAppStorage(historyItem.id, token)
                            plantRepository.insertPlant(
                                historyItem.toRecognizedPlantEntity(imageUri)
                            )
                            cachedPlantIds += historyItem.id
                        }
                    }
                }
        }
    }

    suspend fun deleteDiagnoseHistoryItem(item: DiagnoseResultEntity): Result<Unit> {
        return deleteHistoryItem(
            serverHistoryId = item.serverHistoryId,
            imageUri = item.imageUri,
            deleteLocalRecord = { diagnoseResultRepository.deleteDiagnoseResult(item.id) }
        )
    }

    suspend fun deletePlantHistoryItem(item: PlantEntity): Result<Unit> {
        return deleteHistoryItem(
            serverHistoryId = item.serverHistoryId,
            imageUri = item.imageUri,
            deleteLocalRecord = { plantRepository.deletePlant(item.id) }
        )
    }

    private suspend fun ensureCurrentUserId(): String? {
        authRepository.getCurrentUserId()
            ?.takeIf { it.isNotBlank() }
            ?.let { return it }

        val profile = authRepository.getProfile().getOrNull() ?: return null
        authRepository.saveUserId(profile.userId)
        return profile.userId
    }

    private suspend fun downloadImageToAppStorage(historyId: String, token: String): String? {
        val downloadedImage = NetworkDataSource.downloadHistoryImage(historyId, token) ?: return null
        val extension = when {
            downloadedImage.mimeType?.contains("png", ignoreCase = true) == true -> ".png"
            downloadedImage.mimeType?.contains("webp", ignoreCase = true) == true -> ".webp"
            downloadedImage.mimeType?.contains("gif", ignoreCase = true) == true -> ".gif"
            else -> ".jpg"
        }

        val directory = File(context.filesDir, SYNC_IMAGES_DIRECTORY).apply { mkdirs() }
        val outputFile = File(directory, "${historyId}$extension")

        return runCatching {
            FileOutputStream(outputFile, false).use { stream ->
                stream.write(downloadedImage.bytes)
                stream.flush()
            }
            Uri.fromFile(outputFile).toString()
        }.getOrNull()
    }

    private suspend fun deleteHistoryItem(
        serverHistoryId: String?,
        imageUri: String?,
        deleteLocalRecord: suspend () -> Unit
    ): Result<Unit> {
        return try {
            syncMutex.withLock {
                if (!serverHistoryId.isNullOrBlank()) {
                    val token = authRepository.getToken()
                        ?.takeIf { it.isNotBlank() }
                        ?: throw IllegalStateException(
                            "Khong the xoa lich su tren server vi ban chua dang nhap."
                        )

                    NetworkDataSource.deleteUserHistory(serverHistoryId, token).getOrThrow()
                }

                deleteLocalRecord()
                deleteSyncedImageIfOwned(imageUri)
            }
            Result.success(Unit)
        } catch (error: Exception) {
            Result.failure(error)
        }
    }

    private fun deleteSyncedImageIfOwned(imageUri: String?) {
        if (imageUri.isNullOrBlank()) {
            return
        }

        val imagePath = runCatching { Uri.parse(imageUri).path }.getOrNull()
            ?.takeIf { it.isNotBlank() }
            ?: return

        val syncDirectory = runCatching {
            File(context.filesDir, SYNC_IMAGES_DIRECTORY).canonicalFile
        }.getOrNull() ?: return
        val targetFile = runCatching { File(imagePath).canonicalFile }.getOrNull() ?: return

        val targetPath = targetFile.path
        val syncDirectoryPath = syncDirectory.path + File.separator
        if (!targetPath.startsWith(syncDirectoryPath)) {
            return
        }

        runCatching {
            if (targetFile.exists() && !targetFile.delete()) {
                Log.w(TAG, "Failed to delete synced history image: $targetPath")
            }
        }.onFailure { error ->
            Log.w(TAG, "Failed to cleanup synced history image", error)
        }
    }
}
