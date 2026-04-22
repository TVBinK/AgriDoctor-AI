package com.baothanhbin.feature.processimage

import android.app.Application
import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.exifinterface.media.ExifInterface
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.baothanhbin.core.data.repository.AuthRepository
import com.baothanhbin.core.data.repository.DiagnoseResultRepository
import com.baothanhbin.core.database.model.toEntity
import com.baothanhbin.core.database.model.toPlantEntity
import com.baothanhbin.core.model.ApiType
import com.baothanhbin.core.model.ClassifyData
import com.baothanhbin.core.model.DetectionData
import com.baothanhbin.core.model.LatestDiagnoseResultCache
import com.baothanhbin.core.network.NetworkDataSource
import com.baothanhbin.core.ui.util.LocationHelper
import com.baothanhbin.core.ui.util.LocationStateHolder
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
import java.io.ByteArrayOutputStream
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
    data class NavigateToResult(
        val imageUri: Uri?, 
        val apiType: ApiType = ApiType.DETECT,
        val classifyData: com.baothanhbin.core.model.ClassifyData? = null // For CLASSIFY API - chứa tất cả thông tin
    ) : ProcessImageNavigationEvent()
    data class NavigateToFailed(val imageUri: Uri?, val apiType: ApiType = ApiType.DETECT) : ProcessImageNavigationEvent()
}

@HiltViewModel
class ProcessImageViewModel @Inject constructor(
    application: Application,
    private val diagnoseResultRepository: DiagnoseResultRepository,
    private val plantRepository: com.baothanhbin.core.data.repository.PlantRepository,
    private val authRepository: AuthRepository
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(ProcessImageUiState())
    val uiState: StateFlow<ProcessImageUiState> = _uiState.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<ProcessImageNavigationEvent>()
    val navigationEvent: SharedFlow<ProcessImageNavigationEvent> = _navigationEvent.asSharedFlow()

    fun processImage(
        imageUri: Uri?,
        currentAddress: String?,
        locationStateHolder: LocationStateHolder?,
        apiType: ApiType = ApiType.DETECT
    ) {
        viewModelScope.launch {
            _uiState.value = ProcessImageUiState(step = 0)
            if (imageUri == null) return@launch

            try {
                val context = getApplication<Application>()
                val preparedUpload = withContext(Dispatchers.IO) {
                    prepareImageUpload(context, imageUri)
                }
                if (preparedUpload == null) {
                    return@launch
                }
                val bytes = preparedUpload.bytes

                // Guess mime from URI path
                val name = imageUri.lastPathSegment ?: "image.jpg"
                val mime = preparedUpload.mimeType

                _uiState.value = ProcessImageUiState(step = 1) // uploading
                
                // Get Auth Token
                val token = authRepository.getToken()
                Log.d("ProcessImage", "Token retrieved: ${if (token != null) "Yes (length=${token.length})" else "No"}")

                // Log apiType received
                Log.d("ProcessImage", "Processing image with apiType: $apiType (DETECT=${apiType == ApiType.DETECT}, CLASSIFY=${apiType == ApiType.CLASSIFY})")

                // For detect API, use existing logic
                if (apiType == ApiType.DETECT) {
                    Log.d("ProcessImage", "Calling DETECT API")
                    val detectResult = NetworkDataSource.detectImageTyped(bytes, name, mime, token)

                    // Log API response
                    Log.d("ProcessImage", "API Response received")
                    if (detectResult != null) {
                        Log.d("ProcessImage", "Result: success=${detectResult.success}, diseaseName=${detectResult.data.diseaseName}")
                        Log.d("ProcessImage", "Full result: $detectResult")
                    } else {
                        Log.d("ProcessImage", "Result is null")
                    }

                    _uiState.value = ProcessImageUiState(step = 2) // processing done
                    delay(500) // Small delay to show processing state

                    // Check if result is valid (success and not "No disease detected")
                    val isValidResult = detectResult != null &&
                        detectResult.success &&
                        detectResult.data.diseaseName != "No disease detected"

                    if (isValidResult) {
                    // Cache to Room database - must save before navigating
                    try {
                        // Render detections directly on the normalized bitmap before caching
                        val savedImageUri = withContext(Dispatchers.IO) {
                            cacheDiagnoseImage(context, imageUri, detectResult.data.detections)
                        }
                        
                        // Lấy location hiện tại nếu có quyền
                        val locationFromState = currentAddress ?: locationStateHolder?.currentAddress
                        val currentLocation = locationFromState ?: withContext(Dispatchers.IO) {
                            getCurrentLocationAddress(context)
                        }
                        if (currentLocation != null && locationStateHolder?.currentAddress != currentLocation) {
                            locationStateHolder?.updateAddress(currentLocation)
                        }
                        
                        // Use saved URI if available, otherwise use original (for camera images)
                        val uriToSave = savedImageUri ?: imageUri
                        val entity = detectResult.data.toEntity(
                            imageUri = uriToSave.toString(),
                            location = currentLocation
                        )
                        diagnoseResultRepository.insertDiagnoseResult(entity)
                        
                        // Navigate with saved URI if available
                        val navigationUri = savedImageUri ?: imageUri
                        LatestDiagnoseResultCache.store(navigationUri?.toString(), detectResult.data)
                        _navigationEvent.emit(ProcessImageNavigationEvent.NavigateToResult(navigationUri, apiType))
                    } catch (dbError: Exception) {
                        Log.e("ProcessImage", "Database save error: ${dbError.message}", dbError)
                        // Continue even if database save fails
                        LatestDiagnoseResultCache.store(imageUri.toString(), detectResult.data)
                        _navigationEvent.emit(ProcessImageNavigationEvent.NavigateToResult(imageUri, apiType))
                    }
                    } else {
                        val reason = when {
                            detectResult == null -> "result is null"
                            !detectResult.success -> "success=false"
                            detectResult.data.diseaseName == "No disease detected" -> "No disease detected"
                            else -> "unknown reason"
                        }
                        Log.d("ProcessImage", "Navigating to DiagnoseFailedScreen - $reason")
                        // Navigate to failed screen (fail case, null result, or no disease detected)
                        _navigationEvent.emit(ProcessImageNavigationEvent.NavigateToFailed(imageUri, apiType))
                    }
                } else {
                    // Handle classify API
                    Log.d("ProcessImage", "Calling CLASSIFY API")
                    val classifyResult = NetworkDataSource.classifyImageTyped(bytes, name, mime, token)
                    
                    Log.d("ProcessImage", "Classify API Response received")
                    if (classifyResult != null) {
                        Log.d("ProcessImage", "Result: success=${classifyResult.success}, plantName=${classifyResult.data.plantName}")
                    } else {
                        Log.d("ProcessImage", "Classify result is null")
                    }
                    
                    _uiState.value = ProcessImageUiState(step = 2) // processing done
                    delay(500)
                    
                    // Check if classify result is valid
                    if (classifyResult != null && classifyResult.success) {
                        val classifyData = classifyResult.data
                        Log.d("ProcessImage", "Classify successful: plantName=${classifyData.plantName}, plantNameVN=${classifyData.plantNameVN}, confidence=${classifyData.confidence}")
                        // Navigate to result screen (DiagnoseResultScreen will handle displaying classify data)
                        try {
                            // Copy image to app storage to ensure persistent access
                            val savedImageUri = withContext(Dispatchers.IO) {
                                copyImageToAppStorage(context, imageUri)
                            }
                            
                            // Lấy location hiện tại nếu có quyền
                            val locationFromState = currentAddress ?: locationStateHolder?.currentAddress
                            val currentLocation = locationFromState ?: withContext(Dispatchers.IO) {
                                getCurrentLocationAddress(context)
                            }
                            if (currentLocation != null && locationStateHolder?.currentAddress != currentLocation) {
                                locationStateHolder?.updateAddress(currentLocation)
                            }
                            
                            // Insert Plant to DB
                            val uriToSave = savedImageUri ?: imageUri
                            val entity = classifyData.toPlantEntity(
                                imageUri = uriToSave.toString(),
                                location = currentLocation
                            )
                            plantRepository.insertPlant(entity)

                            // Navigate with saved URI if available
                            val navigationUri = savedImageUri ?: imageUri
                            _navigationEvent.emit(ProcessImageNavigationEvent.NavigateToResult(
                                navigationUri, 
                                apiType,
                                classifyData = classifyData
                            ))
                        } catch (dbError: Exception) {
                            Log.e("ProcessImage", "Error saving classify result: ${dbError.message}", dbError)
                            // Continue even if save fails
                            _navigationEvent.emit(ProcessImageNavigationEvent.NavigateToResult(
                                imageUri, 
                                apiType,
                                classifyData = classifyData
                            ))
                        }
                    } else {
                        val reason = when {
                            classifyResult == null -> "result is null"
                            !classifyResult.success -> "success=false"
                            else -> "unknown reason"
                        }
                        Log.d("ProcessImage", "Classify failed - $reason")
                        _navigationEvent.emit(ProcessImageNavigationEvent.NavigateToFailed(imageUri, apiType))
                    }
                }

                _uiState.value = ProcessImageUiState(step = 3)
            } catch (e: Exception) {
                // Navigate to failed screen when exception occurs
                Log.e("ProcessImage", "Exception occurred: ${e.message}", e)
                _uiState.value = ProcessImageUiState(step = 2) // Mark as processing done even on error
                delay(500)
                _navigationEvent.emit(ProcessImageNavigationEvent.NavigateToFailed(imageUri, apiType))
                _uiState.value = ProcessImageUiState(step = 3)
            }
        }
    }
    
    /**
     * Copy image from source URI to app storage for persistent access
     * Returns the new URI or null if copy fails
     */
    private suspend fun copyImageToAppStorage(context: Application, sourceUri: Uri): Uri? = withContext(Dispatchers.IO) {
        try {
            // Skip copying when the captured image is already a MediaStore item
            // under the app's gallery folder.
            if (isAlreadyInAppStorage(context, sourceUri)) {
                Log.d("ProcessImage", "Image already in app storage: $sourceUri")
                return@withContext sourceUri
            }
            
            // Read the source image
            val bitmap = loadBitmapWithCorrectOrientation(context, sourceUri)
            
            if (bitmap == null) {
                Log.e("ProcessImage", "Failed to read image from URI: $sourceUri")
                return@withContext null
            }
            
            // Save to app storage
            val timestamp = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US).format(Date())
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, "plant_$timestamp.jpg")
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

    private suspend fun cacheDiagnoseImage(
        context: Application,
        sourceUri: Uri,
        detections: List<DetectionData>
    ): Uri? = withContext(Dispatchers.IO) {
        try {
            if (isAlreadyInAppStorage(context, sourceUri) && detections.isEmpty()) {
                Log.d("ProcessImage", "Image already in app storage with no detections: $sourceUri")
                return@withContext sourceUri
            }

            val bitmap = loadBitmapWithCorrectOrientation(context, sourceUri) ?: return@withContext null
            val annotatedBitmap = if (detections.isNotEmpty()) {
                drawDetectionsOnBitmap(bitmap, detections)
            } else {
                bitmap
            }

            saveBitmapToAppStorage(
                context = context,
                bitmap = annotatedBitmap,
                filePrefix = "diagnose"
            ).also {
                if (annotatedBitmap != bitmap) {
                    annotatedBitmap.recycle()
                }
                bitmap.recycle()
            }
        } catch (e: Exception) {
            Log.e("ProcessImage", "Error caching diagnose image: ${e.message}", e)
            null
        }
    }

    private fun drawDetectionsOnBitmap(
        source: Bitmap,
        detections: List<DetectionData>
    ): Bitmap {
        val mutableBitmap = source.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(mutableBitmap)
        val strokeWidthPx = (mutableBitmap.width / 120f).coerceAtLeast(4f)
        val textSizePx = (mutableBitmap.width / 24f).coerceAtLeast(28f)

        val boxPaint = Paint().apply {
            style = Paint.Style.STROKE
            strokeWidth = strokeWidthPx
            isAntiAlias = true
        }

        detections.forEach { detection ->
            if (detection.box.size < 4) return@forEach

            val isHealthy = detection.name.contains("khoe", ignoreCase = true) ||
                detection.name.contains("healthy", ignoreCase = true)
            val color = if (isHealthy) {
                android.graphics.Color.parseColor("#2E7D32")
            } else {
                android.graphics.Color.parseColor("#D32F2F")
            }

            val left = detection.box[0].toFloat().coerceIn(0f, mutableBitmap.width.toFloat())
            val top = detection.box[1].toFloat().coerceIn(0f, mutableBitmap.height.toFloat())
            val right = detection.box[2].toFloat().coerceIn(0f, mutableBitmap.width.toFloat())
            val bottom = detection.box[3].toFloat().coerceIn(0f, mutableBitmap.height.toFloat())

            boxPaint.color = color
            canvas.drawRect(left, top, right, bottom, boxPaint)
        }

        return mutableBitmap
    }

    private fun saveBitmapToAppStorage(
        context: Application,
        bitmap: Bitmap,
        filePrefix: String
    ): Uri? {
        val timestamp = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US).format(Date())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "${filePrefix}_$timestamp.jpg")
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/AgriDoctorAI")
        }

        val outputUri = context.contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        ) ?: return null

        val outputStream = context.contentResolver.openOutputStream(outputUri) ?: return null
        outputStream.use { stream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 95, stream)
            stream.flush()
        }
        return outputUri
    }

    private fun isAlreadyInAppStorage(context: Application, sourceUri: Uri): Boolean {
        if (sourceUri.scheme != "content") return false

        return runCatching {
            val projection = arrayOf(MediaStore.Images.Media.RELATIVE_PATH)
            context.contentResolver.query(sourceUri, projection, null, null, null)?.use { cursor ->
                val relativePathIndex = cursor.getColumnIndex(MediaStore.Images.Media.RELATIVE_PATH)
                if (relativePathIndex == -1 || !cursor.moveToFirst()) {
                    return@use false
                }

                val relativePath = cursor.getString(relativePathIndex).orEmpty()
                relativePath.contains("Pictures/AgriDoctorAI", ignoreCase = true)
            } ?: false
        }.getOrElse { error ->
            Log.w("ProcessImage", "Unable to inspect MediaStore path for $sourceUri: ${error.message}")
            false
        }
    }

    private fun loadBitmapWithCorrectOrientation(context: Application, sourceUri: Uri): Bitmap? {
        val bitmap = context.contentResolver.openInputStream(sourceUri)?.use { inputStream ->
            BitmapFactory.decodeStream(inputStream)
        } ?: return null

        val rotationDegrees = context.contentResolver.openInputStream(sourceUri)?.use { inputStream ->
            when (ExifInterface(inputStream).getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                else -> 0f
            }
        } ?: 0f

        if (rotationDegrees == 0f) return bitmap

        val matrix = Matrix().apply {
            postRotate(rotationDegrees)
        }

        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true).also {
            if (it != bitmap) {
                bitmap.recycle()
            }
        }
    }

    private data class PreparedUploadImage(
        val bytes: ByteArray,
        val mimeType: String = "image/jpeg"
    )

    private fun prepareImageUpload(context: Application, sourceUri: Uri): PreparedUploadImage? {
        val bitmap = loadBitmapWithCorrectOrientation(context, sourceUri) ?: return null

        return try {
            val output = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 95, output)
            PreparedUploadImage(
                bytes = output.toByteArray(),
                mimeType = "image/jpeg"
            )
        } finally {
            bitmap.recycle()
        }
    }
    
    /**
     * Lấy địa chỉ vị trí hiện tại nếu có quyền
     * Returns địa chỉ string hoặc null nếu không có quyền hoặc lỗi
     */
    private suspend fun getCurrentLocationAddress(context: Application): String? = withContext(Dispatchers.IO) {
        try {
            // Kiểm tra quyền
            if (!LocationHelper.hasLocationPermission(context)) {
                Log.d("ProcessImage", "Không có quyền location, bỏ qua lưu location")
                return@withContext null
            }
            
            var locationResult: android.location.Location? = null
            var locationError: Exception? = null
            
            // Lấy location
            LocationHelper.getCurrentLocation(
                context = context,
                onLocationReceived = { location ->
                    locationResult = location
                },
                onError = { exception ->
                    locationError = exception
                }
            )
            
            // Đợi một chút để location được lấy (tối đa 3 giây)
            var waitCount = 0
            while (locationResult == null && locationError == null && waitCount < 30) {
                kotlinx.coroutines.delay(100)
                waitCount++
            }
            
            if (locationError != null) {
                Log.e("ProcessImage", "Lỗi lấy location: ${locationError?.message}")
                return@withContext null
            }
            
            val location = locationResult ?: return@withContext null
            
            // Lấy địa chỉ từ tọa độ
            val address = LocationHelper.getAddressFromLocation(
                context = context,
                latitude = location.latitude,
                longitude = location.longitude
            )
            
            Log.d("ProcessImage", "Đã lấy location: $address")
            return@withContext address
        } catch (e: Exception) {
            Log.e("ProcessImage", "Lỗi khi lấy location address: ${e.message}", e)
            return@withContext null
        }
    }
}

