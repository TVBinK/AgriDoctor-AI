package com.baothanhbin.core.data.repository

import com.baothanhbin.core.model.AuthResponse
import com.baothanhbin.core.model.ChangePasswordRequest
import com.baothanhbin.core.model.ForgotPasswordRequest
import com.baothanhbin.core.model.LoginRequest
import com.baothanhbin.core.model.MessageResponse
import com.baothanhbin.core.model.ResetPasswordRequest
import com.baothanhbin.core.model.ResendOtpRequest
import com.baothanhbin.core.model.SignupRequest
import com.baothanhbin.core.model.UpdateProfileRequest
import com.baothanhbin.core.model.UpdateProfileResponse
import com.baothanhbin.core.model.UserProfile
import com.baothanhbin.core.model.VerifyOtpRequest
import com.baothanhbin.core.model.VerifyForgotOtpRequest
import com.baothanhbin.core.model.VerifyForgotOtpResponse
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val isLoggedIn: Flow<Boolean>
    val currentUserId: Flow<String?>
    suspend fun login(request: LoginRequest): Result<AuthResponse>
    suspend fun signup(request: SignupRequest): Result<AuthResponse>
    suspend fun verifyOtp(request: VerifyOtpRequest): Result<AuthResponse>
    suspend fun resendOtp(request: ResendOtpRequest): Result<MessageResponse>
    suspend fun getProfile(): Result<UserProfile>
    suspend fun updateProfile(request: UpdateProfileRequest): Result<UpdateProfileResponse>
    suspend fun changePassword(request: ChangePasswordRequest): Result<MessageResponse>
    suspend fun forgotPassword(request: ForgotPasswordRequest): Result<MessageResponse>
    suspend fun verifyForgotOtp(request: VerifyForgotOtpRequest): Result<VerifyForgotOtpResponse>
    suspend fun resetPassword(request: ResetPasswordRequest): Result<MessageResponse>
    suspend fun logout()
    suspend fun saveToken(token: String)
    suspend fun saveSession(token: String, userId: String?)
    suspend fun saveUserId(userId: String)
    suspend fun getToken(): String?
    suspend fun getCurrentUserId(): String?
    fun isUserLoggedIn(): Boolean
}
