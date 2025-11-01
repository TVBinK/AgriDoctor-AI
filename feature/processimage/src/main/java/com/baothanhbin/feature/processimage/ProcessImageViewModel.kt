package com.baothanhbin.feature.processimage

import android.app.Application
import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.baothanhbin.core.data.repository.DiagnoseResultRepository
import com.baothanhbin.core.database.model.toEntity
import com.baothanhbin.core.network.NetworkDataSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.OutputStream
import java.net.URLConnection
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class ProcessImageUiState(
    val step: Int = 0 // 0: pending, 1: uploading, 2: processing, 3: done
)

sealed class ProcessImageNavigationEvent {
    data class NavigateToResult(val imageUri: Uri?) : ProcessImageNavigationEvent()
    data class NavigateToFailed(val imageUri: Uri?) : ProcessImageNavigationEvent()
}

@HiltViewModel
class ProcessImageViewModel @Inject constructor(
    application: Application,
    private val diagnoseResultRepository: DiagnoseResultRepository
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(ProcessImageUiState())
    val uiState: StateFlow<ProcessImageUiState> = _uiState.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<ProcessImageNavigationEvent>()
    val navigationEvent: SharedFlow<ProcessImageNavigationEvent> = _navigationEvent.asSharedFlow()

    fun processImage(imageUri: Uri?) {
        viewModelScope.launch {
            _uiState.value = ProcessImageUiState(step = 0)
            if (imageUri == null) return@launch

            try {
                val context = getApplication<Application>()
                val bytes = context.contentResolver.openInputStream(imageUri)?.use { it.readBytes() }
                if (bytes == null) {
                    return@launch
                }

                // Guess mime from URI path
                val name = imageUri.lastPathSegment ?: "image.jpg"
                val mime = URLConnection.guessContentTypeFromName(name) ?: "image/jpeg"

                _uiState.value = ProcessImageUiState(step = 1) // uploading

                val result = NetworkDataSource.detectImageTyped(bytes, name, mime)

                // Log API response
                Log.d("ProcessImage", "API Response received")
                if (result != null) {
                    Log.d("ProcessImage", "Result: success=${result.success}, diseaseName=${result.data.diseaseName}")
                    Log.d("ProcessImage", "Full result: $result")
                } else {
                    Log.d("ProcessImage", "Result is null")
                }

                _uiState.value = ProcessImageUiState(step = 2) // processing done
                delay(500) // Small delay to show processing state

                // Check if result is valid (success and not "No disease detected")
                val isValidResult = result != null &&
                    result.success &&
                    result.data.diseaseName != "No disease detected"

                if (isValidResult) {
                    // Cache to Room database - must save before navigating
                    try {
                        // Copy image to app storage to ensure persistent access
                        val savedImageUri = withContext(Dispatchers.IO) {
                            copyImageToAppStorage(context, imageUri)
                        }
                        
                        // Use saved URI if available, otherwise use original (for camera images)
                        val uriToSave = savedImageUri ?: imageUri
                        val entity = result.data.toEntity(imageUri = uriToSave.toString())
                        diagnoseResultRepository.insertDiagnoseResult(entity)
                        
                        // Navigate with saved URI if available
                        val navigationUri = savedImageUri ?: imageUri
                        _navigationEvent.emit(ProcessImageNavigationEvent.NavigateToResult(navigationUri))
                    } catch (dbError: Exception) {
                        Log.e("ProcessImage", "Database save error: ${dbError.message}", dbError)
                        // Continue even if database save fails
                        _navigationEvent.emit(ProcessImageNavigationEvent.NavigateToResult(imageUri))
                    }
                } else {
                    val reason = when {
                        result == null -> "result is null"
                        !result.success -> "success=false"
                        result.data.diseaseName == "No disease detected" -> "No disease detected"
                        else -> "unknown reason"
                    }
                    Log.d("ProcessImage", "Navigating to DiagnoseFailedScreen - $reason")
                    // Navigate to failed screen (fail case, null result, or no disease detected)
                    _navigationEvent.emit(ProcessImageNavigationEvent.NavigateToFailed(imageUri))
                }

                _uiState.value = ProcessImageUiState(step = 3)
            } catch (e: Exception) {
                // Navigate to failed screen when exception occurs
                Log.e("ProcessImage", "Exception occurred: ${e.message}", e)
                _uiState.value = ProcessImageUiState(step = 2) // Mark as processing done even on error
                delay(500)
                _navigationEvent.emit(ProcessImageNavigationEvent.NavigateToFailed(imageUri))
                _uiState.value = ProcessImageUiState(step = 3)
            }
        }
    }
    
    /**
     * Copy image from source URI (especially Photo Picker) to app storage
     * Returns the new URI or null if copy fails
     */
    private suspend fun copyImageToAppStorage(context: Application, sourceUri: Uri): Uri? = withContext(Dispatchers.IO) {
        try {
            // Check if URI is from Photo Picker (needs to be copied)
            val uriString = sourceUri.toString()
            val isPickerUri = uriString.contains("media/picker") || uriString.contains("photopicker")
            
            // If not a picker URI, return null (no need to copy, e.g., camera images are already saved)
            if (!isPickerUri) {
                return@withContext null
            }
            
            // Read the source image
            val inputStream = context.contentResolver.openInputStream(sourceUri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            
            if (bitmap == null) {
                Log.e("ProcessImage", "Failed to read image from URI: $sourceUri")
                return@withContext null
            }
            
            // Save to app storage
            val timestamp = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US).format(Date())
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, "diagnosis_$timestamp.jpg")
                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/AgriDoctorAI")
            }
            
            val outputUri = context.contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            )
            
            if (outputUri == null) {
                Log.e("ProcessImage", "Failed to create output URI")
                bitmap.recycle()
                return@withContext null
            }
            
            val outputStream: OutputStream? = context.contentResolver.openOutputStream(outputUri)
            if (outputStream == null) {
                Log.e("ProcessImage", "Failed to open output stream")
                bitmap.recycle()
                return@withContext null
            }
            
            bitmap.compress(Bitmap.CompressFormat.JPEG, 95, outputStream)
            outputStream.flush()
            outputStream.close()
            bitmap.recycle()
            
            Log.d("ProcessImage", "Image copied from $sourceUri to $outputUri")
            outputUri
        } catch (e: Exception) {
            Log.e("ProcessImage", "Error copying image: ${e.message}", e)
            null
        }
    }
}

