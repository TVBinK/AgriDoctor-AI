package com.baothanhbin.feature.lightmeter

import android.Manifest
import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.YuvImage
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
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
import java.io.ByteArrayOutputStream
import java.util.concurrent.Executors
import javax.inject.Inject
import kotlin.math.log10
import kotlin.math.pow
import com.baothanhbin.agridoctorai.resources.R

data class LightMeterUiState(
    val permissionGranted: Boolean = false,
    val luxValue: Float = 0f,
    val evValue: Float = 0f,
    val brightness: Float = 0f,
    val lightLevel: String = "",
    val recommendation: String = ""
)

@HiltViewModel
class LightMeterViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {
    
    private val _uiState = MutableStateFlow(LightMeterUiState())
    val uiState: StateFlow<LightMeterUiState> = _uiState.asStateFlow()
    
    private var cameraProvider: ProcessCameraProvider? = null
    private var imageAnalysis: ImageAnalysis? = null
    private val analysisExecutor = Executors.newSingleThreadExecutor()
    
    fun onPermissionResult(granted: Boolean) {
        if (granted != _uiState.value.permissionGranted) {
            _uiState.value = _uiState.value.copy(permissionGranted = granted)
        }
    }
    
    fun bindCamera(lifecycleOwner: LifecycleOwner, previewView: PreviewView) {
        val context = getApplication<Application>()
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        
        cameraProviderFuture.addListener({
            val provider = cameraProviderFuture.get()
            this.cameraProvider = provider
            
            // Preview
            val preview: CameraXPreview = CameraXPreview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
            
            // Image Analysis để đo độ sáng
            imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also { analysis ->
                    analysis.setAnalyzer(analysisExecutor) { imageProxy ->
                        analyzeBrightness(imageProxy)
                    }
                }
            
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            
            try {
                provider.unbindAll()
                provider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageAnalysis
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(context))
    }
    
    private fun analyzeBrightness(imageProxy: ImageProxy) {
        try {
            // Chuyển đổi ImageProxy sang Bitmap
            val bitmap = imageProxyToBitmap(imageProxy)
            
            if (bitmap != null) {
                // Tính toán độ sáng trung bình
                val brightness = calculateBrightness(bitmap)
                
                // Chuyển đổi brightness sang Lux (ước lượng)
                val lux = brightnessToLux(brightness)
                
                // Tính EV (Exposure Value)
                val ev = luxToEV(lux)
                
                // Xác định mức độ ánh sáng
                val lightLevel = getLightLevel(lux)
                
                // Đưa ra khuyến nghị
                val recommendation = getRecommendation(lux)
                
                // Cập nhật UI state
                _uiState.value = _uiState.value.copy(
                    brightness = brightness,
                    luxValue = lux,
                    evValue = ev,
                    lightLevel = lightLevel,
                    recommendation = recommendation
                )
                
                bitmap.recycle()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            imageProxy.close()
        }
    }
    
    private fun imageProxyToBitmap(imageProxy: ImageProxy): Bitmap? {
        val yBuffer = imageProxy.planes[0].buffer
        val uBuffer = imageProxy.planes[1].buffer
        val vBuffer = imageProxy.planes[2].buffer
        
        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()
        
        val nv21 = ByteArray(ySize + uSize + vSize)
        
        yBuffer.get(nv21, 0, ySize)
        vBuffer.get(nv21, ySize, vSize)
        uBuffer.get(nv21, ySize + vSize, uSize)
        
        val yuvImage = YuvImage(nv21, ImageFormat.NV21, imageProxy.width, imageProxy.height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(
            android.graphics.Rect(0, 0, imageProxy.width, imageProxy.height),
            100,
            out
        )
        val imageBytes = out.toByteArray()
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }
    
    private fun calculateBrightness(bitmap: Bitmap): Float {
        // Lấy mẫu từ tâm ảnh để tính độ sáng chính xác hơn
        val centerX = bitmap.width / 2
        val centerY = bitmap.height / 2
        val sampleSize = 100 // Lấy mẫu 100x100 pixels ở giữa
        
        var totalBrightness = 0.0
        var pixelCount = 0
        
        val startX = (centerX - sampleSize / 2).coerceAtLeast(0)
        val endX = (centerX + sampleSize / 2).coerceAtMost(bitmap.width)
        val startY = (centerY - sampleSize / 2).coerceAtLeast(0)
        val endY = (centerY + sampleSize / 2).coerceAtMost(bitmap.height)
        
        for (x in startX until endX step 2) {
            for (y in startY until endY step 2) {
                val pixel = bitmap.getPixel(x, y)
                val r = (pixel shr 16) and 0xff
                val g = (pixel shr 8) and 0xff
                val b = pixel and 0xff
                
                // Công thức tính độ sáng theo chuẩn ITU-R BT.709
                val brightness = 0.2126 * r + 0.7152 * g + 0.0722 * b
                totalBrightness += brightness
                pixelCount++
            }
        }
        
        return if (pixelCount > 0) {
            (totalBrightness / pixelCount).toFloat()
        } else {
            0f
        }
    }
    
    private fun brightnessToLux(brightness: Float): Float {
        // Công thức ước lượng Lux từ brightness (0-255)
        // Đây là công thức thực nghiệm, có thể hiệu chỉnh cho chính xác hơn
        return when {
            brightness < 10 -> brightness * 0.5f
            brightness < 50 -> (brightness - 10) * 2.5f + 5
            brightness < 100 -> (brightness - 50) * 10f + 105
            brightness < 150 -> (brightness - 100) * 50f + 605
            brightness < 200 -> (brightness - 150) * 200f + 3105
            else -> (brightness - 200) * 500f + 13105
        }
    }
    
    private fun luxToEV(lux: Float): Float {
        // EV = log2(Lux / 2.5) với ISO 100
        // Công thức chuẩn: EV = log2(Lux * ISO / (250 * K))
        // K thường là 12.5 cho reflected light metering
        val iso = 100f
        val k = 12.5f
        return if (lux > 0) {
            log10((lux * iso / (250 * k)).toDouble()).toFloat() / log10(2.0).toFloat()
        } else {
            0f
        }
    }
    
    private fun getLightLevel(lux: Float): String {
        val context = getApplication<Application>()
        return when {
            lux < 1 -> context.getString(R.string.light_level_complete_darkness)
            lux < 10 -> context.getString(R.string.light_level_very_dark)
            lux < 50 -> context.getString(R.string.light_level_dark)
            lux < 100 -> context.getString(R.string.light_level_dim)
            lux < 300 -> context.getString(R.string.light_level_moderate)
            lux < 500 -> context.getString(R.string.light_level_fair)
            lux < 1000 -> context.getString(R.string.light_level_bright)
            lux < 5000 -> context.getString(R.string.light_level_very_bright)
            lux < 10000 -> context.getString(R.string.light_level_dazzling)
            lux < 30000 -> context.getString(R.string.light_level_sunlight)
            else -> context.getString(R.string.light_level_extremely_bright)
        }
    }
    
    private fun getRecommendation(lux: Float): String {
        val context = getApplication<Application>()
        return when {
            lux < 50 -> context.getString(R.string.recommendation_too_dark)
            lux < 200 -> context.getString(R.string.recommendation_shade_plants)
            lux < 800 -> context.getString(R.string.recommendation_indoor_plants)
            lux < 2000 -> context.getString(R.string.recommendation_most_indoor_plants)
            lux < 5000 -> context.getString(R.string.recommendation_light_loving)
            lux < 10000 -> context.getString(R.string.recommendation_outdoor_plants)
            lux < 30000 -> context.getString(R.string.recommendation_direct_sunlight)
            else -> context.getString(R.string.recommendation_too_bright)
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        analysisExecutor.shutdown()
        cameraProvider?.unbindAll()
    }
}

