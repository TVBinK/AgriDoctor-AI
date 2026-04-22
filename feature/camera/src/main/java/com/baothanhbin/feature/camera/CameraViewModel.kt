package com.baothanhbin.feature.camera

import android.app.Application
import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.provider.MediaStore
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview as CameraXPreview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.exifinterface.media.ExifInterface
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LifecycleOwner
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class CameraUiState(
    val selectedModeIndex: Int = 0,
    val permissionGranted: Boolean = false
)
@HiltViewModel
class CameraViewModel @javax.inject.Inject constructor(
    application: Application
) : AndroidViewModel(application) {
    companion object {
        private const val TARGET_EXPOSURE_COMPENSATION = -2
    }

    private val _uiState = MutableStateFlow(CameraUiState())
    val uiState: StateFlow<CameraUiState> = _uiState.asStateFlow()
    
    private var imageCapture: ImageCapture? = null
    private var boundCamera: Camera? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var lifecycleOwner: LifecycleOwner? = null
    private var previewView: PreviewView? = null
    private var isBackCamera = true

    fun selectMode(index: Int) {
        if (index == _uiState.value.selectedModeIndex) return
        val modeName = if (index == 0) "Chuẩn đoán (DETECT)" else "Nhận diện cây (CLASSIFY)"
        Log.d("CameraViewModel", "Tab được chọn: index=$index, mode=$modeName")
        _uiState.value = _uiState.value.copy(selectedModeIndex = index)
    }

    fun onPermissionResult(granted: Boolean) {
        if (granted != _uiState.value.permissionGranted) {
            _uiState.value = _uiState.value.copy(permissionGranted = granted)
        }
    }

    fun bindPreview(lifecycleOwner: LifecycleOwner, previewView: PreviewView) {
        this.lifecycleOwner = lifecycleOwner
        this.previewView = previewView
        
        val context = getApplication<Application>()
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            this.cameraProvider = cameraProvider
            val preview: CameraXPreview = CameraXPreview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }
            imageCapture = ImageCapture.Builder().build()
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            try {
                cameraProvider.unbindAll()
                boundCamera = cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageCapture)
                applyPreferredExposure()
            } catch (_: Exception) {}
        }, ContextCompat.getMainExecutor(context))
    }
    
    fun switchCamera() {
        val provider = cameraProvider
        val owner = lifecycleOwner
        val view = previewView
        
        if (provider == null || owner == null || view == null) return
        
        isBackCamera = !isBackCamera
        val cameraSelector = if (isBackCamera) {
            CameraSelector.DEFAULT_BACK_CAMERA
        } else {
            CameraSelector.DEFAULT_FRONT_CAMERA
        }
        
        val preview: CameraXPreview = CameraXPreview.Builder().build().also {
            it.setSurfaceProvider(view.surfaceProvider)
        }
        imageCapture = ImageCapture.Builder().build()
        try {
            provider.unbindAll()
            boundCamera = provider.bindToLifecycle(owner, cameraSelector, preview, imageCapture)
            applyPreferredExposure()
        } catch (_: Exception) {}
    }
    
    fun takePhoto(
        previewWidth: Int,
        previewHeight: Int,
        focusWidth: Int,
        focusHeight: Int,
        onPhotoSaved: (Uri) -> Unit,
        onError: (String) -> Unit
    ) {
        val imageCapture = imageCapture ?: run {
            onError("ImageCapture not initialized")
            return
        }
        
        val context = getApplication<Application>()
        val contentValues = createOutputContentValues()
        val outputFileOptions = ImageCapture.OutputFileOptions
            .Builder(
                context.contentResolver,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            )
            .build()
        
        imageCapture.takePicture(
            outputFileOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = output.savedUri
                    if (savedUri == null) {
                        onError("Photo capture failed: missing saved uri")
                        return
                    }

                    cropImage(savedUri, previewWidth, previewHeight, focusWidth, focusHeight, onPhotoSaved, onError)
                }
                
                override fun onError(exception: ImageCaptureException) {
                    onError("Photo capture failed: ${exception.message}")
                }
            }
        )
    }
    
    private fun cropImage(
        originalUri: Uri,
        previewWidth: Int,
        previewHeight: Int,
        focusWidth: Int,
        focusHeight: Int,
        onPhotoSaved: (Uri) -> Unit,
        onError: (String) -> Unit
    ) {
        val context = getApplication<Application>()
        val originalBitmap = loadBitmapWithCorrectOrientation(originalUri)
        
        if (originalBitmap == null) {
            deleteImageUri(originalUri)
            onError("Failed to read captured image")
            return
        }
        
        // Calculate crop region
        val scaleX = originalBitmap.width.toFloat() / previewWidth
        val scaleY = originalBitmap.height.toFloat() / previewHeight
        
        val focusLeft = ((previewWidth - focusWidth) / 2f * scaleX).toInt()
        val focusTop = ((previewHeight - focusHeight) / 2f * scaleY).toInt()
        val focusRight = focusLeft + (focusWidth * scaleX).toInt()
        val focusBottom = focusTop + (focusHeight * scaleY).toInt()
        
        val croppedBitmap = Bitmap.createBitmap(
            originalBitmap,
            focusLeft,
            focusTop,
            focusRight - focusLeft,
            focusBottom - focusTop
        )
        
        originalBitmap.recycle()

        val outputStream: OutputStream? = context.contentResolver.openOutputStream(originalUri, "wt")
        if (outputStream == null) {
            deleteImageUri(originalUri)
            onError("Failed to open output stream")
            croppedBitmap.recycle()
            return
        }
        
        croppedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        outputStream.flush()
        outputStream.close()
        croppedBitmap.recycle()

        onPhotoSaved(originalUri)
    }

    private fun createOutputContentValues(): ContentValues {
        val name = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US).format(System.currentTimeMillis())
        return ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/AgriDoctorAI")
        }
    }

    private fun deleteImageUri(uri: Uri) {
        runCatching {
            getApplication<Application>().contentResolver.delete(uri, null, null)
        }.onFailure { error ->
            Log.w("CameraViewModel", "Failed to delete image uri $uri: ${error.message}")
        }
    }

    private fun applyPreferredExposure() {
        val camera = boundCamera ?: return
        val exposureState = camera.cameraInfo.exposureState

        if (!exposureState.isExposureCompensationSupported) {
            Log.d("CameraViewModel", "Exposure compensation is not supported on this device")
            return
        }

        val range = exposureState.exposureCompensationRange
        val targetIndex = TARGET_EXPOSURE_COMPENSATION.coerceIn(range.lower, range.upper)
        camera.cameraControl.setExposureCompensationIndex(targetIndex)
        Log.d("CameraViewModel", "Applied exposure compensation: $targetIndex")
    }

    private fun loadBitmapWithCorrectOrientation(uri: Uri): Bitmap? {
        val context = getApplication<Application>()
        val bitmap = context.contentResolver.openInputStream(uri)?.use { inputStream ->
            BitmapFactory.decodeStream(inputStream)
        } ?: return null

        val rotationDegrees = context.contentResolver.openInputStream(uri)?.use { inputStream ->
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
}


