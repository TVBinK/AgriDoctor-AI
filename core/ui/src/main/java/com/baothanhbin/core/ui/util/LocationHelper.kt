package com.baothanhbin.core.ui.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

object LocationHelper {
    private const val TAG = "LocationHelper"
    /**
     * Kiểm tra xem có quyền truy cập location không
     */
    fun hasLocationPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Lấy location hiện tại của thiết bị sử dụng Fused Location Provider API
     * Fused Location Provider tự động chọn nguồn tốt nhất (GPS, Network, WiFi)
     * để đảm bảo tốc độ nhanh và độ chính xác cao, đồng thời tiết kiệm pin
     */
    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    fun getCurrentLocation(
        context: Context,
        onLocationReceived: (Location) -> Unit,
        onError: (Exception) -> Unit
    ) {
        try {
            // Tạo FusedLocationProviderClient - API hiện đại của Google Play Services
            val fusedLocationClient: FusedLocationProviderClient = 
                LocationServices.getFusedLocationProviderClient(context)
            
            var locationReceived = false
            val cancellationTokenSource = com.google.android.gms.tasks.CancellationTokenSource()

            // getCurrentLocation() tối ưu để lấy location một lần
            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY, // Ưu tiên GPS cho độ chính xác cao
                cancellationTokenSource.token // Token để có thể cancel request nếu cần
            ).addOnSuccessListener { location ->
                if (location != null && !locationReceived) {
                    locationReceived = true
                    Log.d(
                        TAG,
                        "getCurrentLocation() -> nhận được location: lat=${location.latitude}, lon=${location.longitude}, accuracy=${location.accuracy}m, provider=${location.provider}"
                    )
                    onLocationReceived(location)
                } else if (!locationReceived && location == null) {
                    locationReceived = true
                    Log.w(TAG, "getCurrentLocation() -> location null, có thể GPS chưa sẵn sàng")
                    onError(Exception("Không thể lấy vị trí. Vui lòng bật GPS và thử lại."))
                }
            }.addOnFailureListener { exception ->
                Log.e(TAG, "getCurrentLocation() -> lỗi khi request location", exception)
                if (!locationReceived) {
                    locationReceived = true
                    onError(exception)
                }
            }

            // Set timeout 10 giây
            android.os.Handler(Looper.getMainLooper()).postDelayed({
                if (!locationReceived) {
                    Log.e(TAG, "getCurrentLocation() -> timeout sau 10 giây")
                    locationReceived = true
                    
                    // Cancel location request
                    cancellationTokenSource.cancel()
                    
                    // Báo lỗi timeout
                    onError(Exception("Không thể lấy vị trí sau 10 giây. Vui lòng bật GPS và thử lại."))
                }
            }, 10000L) // 10 giây timeout
            
        } catch (e: SecurityException) {
            // Xử lý lỗi quyền truy cập
            Log.e(TAG, "getCurrentLocation() -> lỗi quyền truy cập", e)
            onError(e)
        } catch (e: Exception) {
            // Xử lý các lỗi khác
            Log.e(TAG, "getCurrentLocation() -> lỗi không xác định", e)
            onError(e)
        }
    }

    /**
     * Lấy địa chỉ từ tọa độ (Geocoding - chuyển đổi tọa độ thành địa chỉ)
     * Chuyển đổi latitude/longitude thành địa chỉ văn bản (ví dụ: "123 Đường ABC, Quận XYZ, TP.HCM")
     * Sử dụng Geocoder API của Android để query Google Maps API
     * @return Địa chỉ dạng string hoặc null nếu không tìm thấy
     */
    suspend fun getAddressFromLocation(
        context: Context,
        latitude: Double,
        longitude: Double
    ): String? = withContext(Dispatchers.IO) {
        // Chạy trên background thread vì Geocoding là I/O operation
        try {
            // Tạo Geocoder với locale mặc định của thiết bị (để trả về địa chỉ theo ngôn ngữ phù hợp)
            val geocoder = Geocoder(context, Locale.getDefault())

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // Android 13 (API 33) trở lên sử dụng API mới với callback (async)
                var addressResult: String? = null
                val geocodeListener = object : Geocoder.GeocodeListener {
                    // Callback khi geocoding thành công
                    override fun onGeocode(addresses: List<Address>) {
                        Log.d(TAG, "onGeocode() -> nhận được ${addresses.size} address(es)")
                        if (addresses.isNotEmpty()) {
                            // Lấy địa chỉ đầu tiên và format nó
                            addressResult = formatAddress(addresses[0])
                        }
                    }

                    // Callback khi geocoding thất bại
                    override fun onError(errorMessage: String?) {
                        Log.e(TAG, "Geocoding error: $errorMessage")
                    }
                }

                // Gọi API geocoding async (không blocking)
                Log.d(
                    TAG,
                    "getAddressFromLocation() -> gọi geocoder.getFromLocation(lat=$latitude, lon=$longitude)"
                )
                geocoder.getFromLocation(latitude, longitude, 1, geocodeListener)
                
                // Đợi kết quả (polling - tối đa 5 giây: 50 lần x 100ms)
                // Lưu ý: Đây là cách đơn giản để đợi async callback, có thể cải thiện bằng coroutines
                var count = 0
                while (addressResult == null && count < 50) {
                    Thread.sleep(100)
                    count++
                }
                
                if (addressResult == null) {
                    Log.w(TAG, "getAddressFromLocation() -> hết thời gian đợi geocoding, trả về null")
                } else {
                    Log.d(TAG, "getAddressFromLocation() -> geocoding thành công: $addressResult")
                }
                return@withContext addressResult
            } else {
                // Android 12 (API 32) trở xuống sử dụng API cũ (sync - blocking)
                @Suppress("DEPRECATION")
                Log.d(
                    TAG,
                    "getAddressFromLocation() -> (API<=32) gọi geocoder.getFromLocation(lat=$latitude, lon=$longitude)"
                )
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                
                // getFromLocation() trả về List<Address> hoặc null
                if (addresses != null && addresses.isNotEmpty()) {
                    Log.d(TAG, "getAddressFromLocation() -> geocoding thành công (${addresses.size} address)")
                    return@withContext formatAddress(addresses[0])
                }
                Log.w(TAG, "getAddressFromLocation() -> không tìm thấy address nào")
            }

            return@withContext null
        } catch (e: Exception) {
            Log.e(TAG, "Error getting address: ${e.message}", e)
            return@withContext null
        }
    }

    /**
     * Format địa chỉ từ Address object thành string dễ đọc
     * Address object chứa nhiều thông tin chi tiết (số nhà, đường, quận, thành phố, v.v.)
     * Hàm này chọn các thông tin quan trọng nhất và ghép lại thành string
     * @param address Address object từ Geocoder
     * @return String địa chỉ đã được format (ví dụ: "123 Đường ABC, Quận XYZ, TP.HCM")
     */
    private fun formatAddress(address: Address): String {
        val parts = mutableListOf<String>()

        // Thêm địa chỉ chi tiết (số nhà, đường) - ví dụ: "123 Đường ABC"
        address.thoroughfare?.let { parts.add(it) }

        // Thêm quận/huyện - ví dụ: "Quận XYZ"
        address.subAdminArea?.let { parts.add(it) }

        // Thêm thành phố/tỉnh - ví dụ: "TP.HCM" hoặc "Hà Nội"
        address.adminArea?.let { parts.add(it) }

        // Nếu không có thông tin chi tiết (thoroughfare, subAdminArea), 
        // thử dùng locality (địa danh) và adminArea
        if (parts.isEmpty()) {
            address.locality?.let { parts.add(it) }  // Địa danh (ví dụ: "Hà Đông")
            address.adminArea?.let { parts.add(it) }  // Tỉnh/thành phố
        }

        // Nếu có ít nhất một phần, ghép lại bằng dấu phẩy
        return if (parts.isNotEmpty()) {
            parts.joinToString(", ")
        } else {
            // Fallback: nếu không có thông tin địa chỉ nào, hiển thị tọa độ
            "Lat: ${"%.4f".format(address.latitude)}, Lon: ${"%.4f".format(address.longitude)}"
        }
    }
}


