package com.baothanhbin.core.ui.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.core.content.ContextCompat
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
     * Lấy location hiện tại
     */
    fun getCurrentLocation(
        context: Context,
        onLocationReceived: (Location) -> Unit,
        onError: (Exception) -> Unit
    ) {
        if (!hasLocationPermission(context)) {
            onError(SecurityException("Không có quyền truy cập vị trí"))
            return
        }

        try {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

            // Thử lấy cached location trước
            val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

            if (location != null) {
                onLocationReceived(location)
                return
            }

            // Nếu không có cached location, request location update một lần
            val locationListener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    onLocationReceived(location)
                    locationManager.removeUpdates(this)
                }

                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                override fun onProviderEnabled(provider: String) {}
                override fun onProviderDisabled(provider: String) {}
            }

            // Request location update
            when {
                locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) -> {
                    locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        0L,
                        0f,
                        locationListener
                    )
                }
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> {
                    locationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        0L,
                        0f,
                        locationListener
                    )
                }
                else -> {
                    onError(Exception("Location services đang tắt"))
                }
            }
        } catch (e: SecurityException) {
            onError(e)
        } catch (e: Exception) {
            onError(e)
        }
    }

    /**
     * Lấy địa chỉ từ tọa độ (Geocoding)
     */
    suspend fun getAddressFromLocation(
        context: Context,
        latitude: Double,
        longitude: Double
    ): String? = withContext(Dispatchers.IO) {
        try {
            if (!Geocoder.isPresent()) {
                return@withContext null
            }

            val geocoder = Geocoder(context, Locale.getDefault())

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // Android 13 trở lên sử dụng API mới
                var addressResult: String? = null
                val geocodeListener = object : Geocoder.GeocodeListener {
                    override fun onGeocode(addresses: List<Address>) {
                        if (addresses.isNotEmpty()) {
                            addressResult = formatAddress(addresses[0])
                        }
                    }

                    override fun onError(errorMessage: String?) {
                        Log.e(TAG, "Geocoding error: $errorMessage")
                    }
                }

                geocoder.getFromLocation(latitude, longitude, 1, geocodeListener)
                
                // Đợi kết quả (tối đa 5 giây)
                var count = 0
                while (addressResult == null && count < 50) {
                    Thread.sleep(100)
                    count++
                }
                
                return@withContext addressResult
            } else {
                // Android 12 trở xuống sử dụng API cũ
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                
                if (addresses != null && addresses.isNotEmpty()) {
                    return@withContext formatAddress(addresses[0])
                }
            }

            return@withContext null
        } catch (e: Exception) {
            Log.e(TAG, "Error getting address: ${e.message}", e)
            return@withContext null
        }
    }

    /**
     * Format địa chỉ từ Address object
     */
    private fun formatAddress(address: Address): String {
        val parts = mutableListOf<String>()

        // Thêm địa chỉ chi tiết (số nhà, đường)
        address.thoroughfare?.let { parts.add(it) }

        // Thêm quận/huyện
        address.subAdminArea?.let { parts.add(it) }

        // Thêm thành phố/tỉnh
        address.adminArea?.let { parts.add(it) }

        // Nếu không có thông tin chi tiết, thêm locality
        if (parts.isEmpty()) {
            address.locality?.let { parts.add(it) }
            address.adminArea?.let { parts.add(it) }
        }

        return if (parts.isNotEmpty()) {
            parts.joinToString(", ")
        } else {
            // Fallback: hiển thị tọa độ
            "Lat: ${"%.4f".format(address.latitude)}, Lon: ${"%.4f".format(address.longitude)}"
        }
    }
}

