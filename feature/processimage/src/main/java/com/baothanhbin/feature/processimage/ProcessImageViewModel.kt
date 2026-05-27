package com.baothanhbin.feature.processimage

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.net.Uri
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
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
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
    data object NavigateToLogin : ProcessImageNavigationEvent()
}

private const val DETECT_RESULT_DIAGNOSED = "diagnosed"
private const val CLASSIFY_RESULT_CLASSIFIED = "classified"

@HiltViewModel
class ProcessImageViewModel @Inject constructor(
    application: Application,
    private val diagnoseResultRepository: DiagnoseResultRepository,
    private val plantRepository: com.baothanhbin.core.data.repository.PlantRepository,
    private val authRepository: AuthRepository
) : AndroidViewModel(application) {
    companion object {
        private const val PRIVATE_IMAGE_DIRECTORY = "diagnose_images"
    }

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
                
                val token = authRepository.getToken()
                if (token.isNullOrBlank()) {
                    Log.w("ProcessImage", "Missing auth token, redirecting to login before upload.")
                    _navigationEvent.emit(ProcessImageNavigationEvent.NavigateToLogin)
                    return@launch
                }

                // For detect API, use existing logic
                if (apiType == ApiType.DETECT) {
                    val detectResult = NetworkDataSource.detectImageTyped(bytes, name, mime, token)

                    _uiState.value = ProcessImageUiState(step = 2) // processing done
                    delay(500) // Small delay to show processing state

                    val detectResultType = detectResult?.data?.resultType
                    val isDiagnosedResult = detectResult != null &&
                        detectResult.success &&
                        detectResultType == DETECT_RESULT_DIAGNOSED

                    if (isDiagnosedResult) {
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
                            location = currentLocation,
                            serverHistoryId = detectResult.historyId
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
                        _navigationEvent.emit(ProcessImageNavigationEvent.NavigateToFailed(imageUri, apiType))
                    }
                } else {
                    // Handle classify API
                    val classifyResult = NetworkDataSource.classifyImageTyped(bytes, name, mime, token)

                    _uiState.value = ProcessImageUiState(step = 2) // processing done
                    delay(500)
                    
                    // Check if classify result is valid
                    val isClassifiedResult = classifyResult != null &&
                        classifyResult.success &&
                        classifyResult.data.resultType == CLASSIFY_RESULT_CLASSIFIED &&
                        !classifyResult.data.rejectedInput

                    if (isClassifiedResult) {
                        val classifyData = classifyResult.data
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
                                location = currentLocation,
                                serverHistoryId = classifyResult.historyId
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
            // Skip copying when the image already lives in app-private storage.
            if (isAlreadyInAppStorage(context, sourceUri)) {
                return@withContext sourceUri
            }
            
            // Read the source image
            val bitmap = loadBitmapWithCorrectOrientation(context, sourceUri)
            
            if (bitmap == null) {
                Log.e("ProcessImage", "Failed to read image from source")
                return@withContext null
            }
            
            val outputUri = saveBitmapToAppStorage(
                context = context,
                bitmap = bitmap,
                filePrefix = "plant"
            )
            bitmap.recycle()
            
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
        val textSizePx = (mutableBitmap.width / 30f).coerceAtLeast(22f)
        val labelHorizontalPaddingPx = textSizePx * 0.3f
        val labelVerticalPaddingPx = textSizePx * 0.16f
        val labelCornerRadiusPx = textSizePx * 0.24f

        val boxPaint = Paint().apply {
            style = Paint.Style.STROKE
            strokeWidth = strokeWidthPx
            isAntiAlias = true
        }

        val labelBackgroundPaint = Paint().apply {
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        val labelTextPaint = Paint().apply {
            style = Paint.Style.FILL
            color = android.graphics.Color.WHITE
            textSize = textSizePx
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val labelBounds = Rect()

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

            val confidenceLabel = formatDetectionConfidence(detection.confidence)
            labelTextPaint.getTextBounds(confidenceLabel, 0, confidenceLabel.length, labelBounds)

            val labelWidth = labelBounds.width() + (labelHorizontalPaddingPx * 2f)
            val labelHeight = labelBounds.height() + (labelVerticalPaddingPx * 2f)
            val maxLabelLeft = (mutableBitmap.width - labelWidth).coerceAtLeast(0f)
            val maxLabelTop = (mutableBitmap.height - labelHeight).coerceAtLeast(0f)
            val labelLeft = left.coerceIn(0f, maxLabelLeft)
            val preferredLabelTop = top - labelHeight
            val labelTop = if (preferredLabelTop >= 0f) {
                preferredLabelTop.coerceAtMost(maxLabelTop)
            } else {
                top.coerceIn(0f, maxLabelTop)
            }

            labelBackgroundPaint.color = color
            val labelRect = RectF(
                labelLeft,
                labelTop,
                labelLeft + labelWidth,
                labelTop + labelHeight
            )
            canvas.drawRoundRect(
                labelRect,
                labelCornerRadiusPx,
                labelCornerRadiusPx,
                labelBackgroundPaint
            )

            val baseline = labelRect.top + labelVerticalPaddingPx - labelBounds.top
            canvas.drawText(
                confidenceLabel,
                labelRect.left + labelHorizontalPaddingPx,
                baseline,
                labelTextPaint
            )
        }

        return mutableBitmap
    }

    private fun formatDetectionConfidence(confidence: Double): String {
        val percent = confidence * 100
        if (!percent.isFinite()) return "0%"

        val formatter = DecimalFormat(
            "0.##",
            DecimalFormatSymbols(Locale.US)
        )
        return "${formatter.format(percent)}%"
    }

    private fun saveBitmapToAppStorage(
        context: Application,
        bitmap: Bitmap,
        filePrefix: String
    ): Uri? {
        val timestamp = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US).format(Date())
        val directory = File(context.filesDir, PRIVATE_IMAGE_DIRECTORY).apply { mkdirs() }
        val outputFile = File(directory, "${filePrefix}_$timestamp.jpg")

        return runCatching {
            FileOutputStream(outputFile, false).use { stream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 95, stream)
                stream.flush()
            }
            Uri.fromFile(outputFile)
        }.getOrElse { error ->
            Log.e("ProcessImage", "Failed to save bitmap to app storage: ${error.message}", error)
            null
        }
    }

    private fun isAlreadyInAppStorage(context: Application, sourceUri: Uri): Boolean {
        val localFile = resolveLocalFile(sourceUri) ?: return false
        return isInsideDirectory(localFile, context.filesDir) || isInsideDirectory(localFile, context.cacheDir)
    }

    private fun loadBitmapWithCorrectOrientation(context: Application, sourceUri: Uri): Bitmap? {
        val bitmap = openInputStream(context, sourceUri)?.use { inputStream ->
            BitmapFactory.decodeStream(inputStream)
        } ?: return null

        val rotationDegrees = openInputStream(context, sourceUri)?.use { inputStream ->
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

    private fun openInputStream(context: Application, sourceUri: Uri): InputStream? {
        return resolveLocalFile(sourceUri)?.let(::FileInputStream)
            ?: context.contentResolver.openInputStream(sourceUri)
    }

    private fun resolveLocalFile(sourceUri: Uri): File? {
        val path = when (sourceUri.scheme) {
            null -> sourceUri.toString().takeIf { it.isNotBlank() }
            "file" -> sourceUri.path
            else -> null
        } ?: return null

        return File(path)
    }

    private fun isInsideDirectory(file: File, directory: File): Boolean {
        return runCatching {
            val filePath = file.canonicalPath
            val directoryPath = directory.canonicalPath.removeSuffix(File.separator) + File.separator
            filePath.startsWith(directoryPath)
        }.getOrElse { error ->
            Log.w("ProcessImage", "Unable to inspect private file path: ${error.message}")
            false
        }
    }

    private data class PreparedUploadImage(
        val bytes: ByteArray,
        val mimeType: String = "image/jpeg"
    )

    private fun prepareImageUpload(context: Application, sourceUri: Uri): PreparedUploadImage? {
        val bytes = openInputStream(context, sourceUri)?.use { inputStream ->
            inputStream.readBytes()
        } ?: return null

        return PreparedUploadImage(
            bytes = bytes,
            mimeType = resolveUploadMimeType(context, sourceUri)
        )
    }

    private fun resolveUploadMimeType(context: Application, sourceUri: Uri): String {
        context.contentResolver.getType(sourceUri)
            ?.takeIf { it.startsWith("image/") }
            ?.let { return it }

        val fileName = resolveLocalFile(sourceUri)?.name
            ?: sourceUri.lastPathSegment
            ?: return "image/jpeg"

        return URLConnection.guessContentTypeFromName(fileName)
            ?.takeIf { it.startsWith("image/") }
            ?: "image/jpeg"
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
            
            Log.d("ProcessImage", "Da lay thong tin vi tri de luu vao lich su.")
            return@withContext address
        } catch (e: Exception) {
            Log.e("ProcessImage", "Lỗi khi lấy location address: ${e.message}", e)
            return@withContext null
        }
    }
}

