package com.baothanhbin.agridoctorai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.baothanhbin.core.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    authRepository: AuthRepository
) : ViewModel() {

    // Sử dụng runBlocking hoặc truy cập trực tiếp nếu có thể để lấy trạng thái ban đầu
    // Tuy nhiên, để đơn giản và hiệu quả, ta có thể dùng Loading state cho MainApp
    // Nhưng AuthRepositoryImpl khởi tạo _isLoggedIn ngay lập tức từ SharedPreferences.
    // Vấn đề là `stateIn` cần initialValue.
    // Ta có thể gọi hàm getToken() synchronous từ Repository (nếu có) để set initial value.
    // AuthRepository có `suspend fun getToken(): String?`.
    // Let's rely on the Flow emitting immediately.
    
    val isLoggedIn: StateFlow<Boolean> = authRepository.isLoggedIn
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = authRepository.isUserLoggedIn() 
        )
}
