package com.baothanhbin.feature.camera

import android.app.Application
import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview as CameraXPreview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
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
    private val _uiState = MutableStateFlow(CameraUiState())
    val uiState: StateFlow<CameraUiState> = _uiState.asStateFlow()
    
    private var imageCapture: ImageCapture? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var lifecycleOwner: LifecycleOwner? = null
    private var previewView: PreviewView? = null
    private var isBackCamera = true

    fun selectMode(index: Int) {
        if (index == _uiState.value.selectedModeIndex) return
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
                cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageCapture)
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
            provider.bindToLifecycle(owner, cameraSelector, preview, imageCapture)
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
        val name = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US).format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/AgriDoctorAI")
        }
        
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
                    output.savedUri?.let { uri ->
                        cropImage(uri, previewWidth, previewHeight, focusWidth, focusHeight, onPhotoSaved, onError)
                    } ?: onError("Failed to get saved URI")
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
        // Read the original image
        val inputStream = context.contentResolver.openInputStream(originalUri)
        val originalBitmap = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()
        
        if (originalBitmap == null) {
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
        
        // Save the cropped image
        val name = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US).format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/AgriDoctorAI")
        }
        
        val outputUri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        if (outputUri == null) {
            onError("Failed to create output URI")
            croppedBitmap.recycle()
            return
        }
        
        val outputStream: OutputStream? = context.contentResolver.openOutputStream(outputUri)
        if (outputStream == null) {
            onError("Failed to open output stream")
            croppedBitmap.recycle()
            return
        }
        
        croppedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        outputStream.flush()
        outputStream.close()
        croppedBitmap.recycle()
        
        // Delete the original full image
        context.contentResolver.delete(originalUri, null, null)
        
        onPhotoSaved(outputUri)
    }
}


