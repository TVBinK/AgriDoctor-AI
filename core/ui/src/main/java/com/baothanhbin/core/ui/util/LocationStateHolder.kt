package com.baothanhbin.core.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/**
 * State holder chung để lưu vị trí và địa chỉ, chia sẻ giữa các tab
 * Sử dụng remember ở MainActivity để giữ state khi chuyển tab
 */
class LocationStateHolder {
    var currentAddress: String? by mutableStateOf(null)
        private set
    
    fun updateAddress(address: String?) {
        currentAddress = address
    }
    
    fun clearAddress() {
        currentAddress = null
    }
}

@Composable
fun rememberLocationStateHolder(): LocationStateHolder {
    return remember { LocationStateHolder() }
}

