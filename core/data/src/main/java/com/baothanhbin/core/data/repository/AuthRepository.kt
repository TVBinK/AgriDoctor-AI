package com.baothanhbin.core.data.repository

import com.baothanhbin.core.model.AuthResponse
import com.baothanhbin.core.model.LoginRequest
import com.baothanhbin.core.model.SignupRequest
import com.baothanhbin.core.model.VerifyOtpRequest
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val isLoggedIn: Flow<Boolean>
    suspend fun login(request: LoginRequest): Result<AuthResponse>
    suspend fun signup(request: SignupRequest): Result<AuthResponse>
    suspend fun verifyOtp(request: VerifyOtpRequest): Result<AuthResponse>
    suspend fun logout()
    suspend fun saveToken(token: String)
    suspend fun getToken(): String?
    fun isUserLoggedIn(): Boolean
}
