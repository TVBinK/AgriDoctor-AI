package com.baothanhbin.core.data.impl

import android.content.Context
import android.content.SharedPreferences
import com.baothanhbin.core.data.repository.AuthRepository
import com.baothanhbin.core.model.AuthResponse
import com.baothanhbin.core.model.LoginRequest
import com.baothanhbin.core.model.SignupRequest
import com.baothanhbin.core.model.VerifyOtpRequest
import com.baothanhbin.core.network.NetworkClients
import dagger.hilt.android.qualifiers.ApplicationContext
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : AuthRepository {

    private val prefs: SharedPreferences = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
    private val _isLoggedIn = MutableStateFlow(prefs.getString("jwt_token", null) != null)
    override val isLoggedIn: Flow<Boolean> = _isLoggedIn.asStateFlow()

    override suspend fun login(request: LoginRequest): Result<AuthResponse> {
        android.util.Log.d("AuthRepository", "Logging in with email: ${request.email}")
        android.util.Log.d("AuthRepository", "Login request body: $request")
        return try {
            val response = NetworkClients.authClient.post("login") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body<AuthResponse>()
            
            android.util.Log.d("AuthRepository", "Login response: $response")

            // If API returns token immediately (e.g. password login), save it
            if (!response.token.isNullOrEmpty()) {
                response.token?.let { saveToken(it) }
            }
            Result.success(response)
        } catch (e: Exception) {
            android.util.Log.e("AuthRepository", "Login failed", e)
            Result.failure(e)
        }
    }

    override suspend fun signup(request: SignupRequest): Result<AuthResponse> {
        android.util.Log.d("AuthRepository", "Signing up with email: ${request.email}")
        android.util.Log.d("AuthRepository", "Signup request body: $request")
        return try {
            val response = NetworkClients.authClient.post("signup") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            
            val responseBody = response.bodyAsText()
            android.util.Log.d("AuthRepository", "Signup raw response: $responseBody")
            
            val authResponse = Json { 
                ignoreUnknownKeys = true 
                coerceInputValues = true
            }.decodeFromString<AuthResponse>(responseBody)
            
            Result.success(authResponse)
        } catch (e: Exception) {
            android.util.Log.e("AuthRepository", "Signup failed", e)
            Result.failure(e)
        }
    }

    override suspend fun verifyOtp(request: VerifyOtpRequest): Result<AuthResponse> {
        android.util.Log.d("AuthRepository", "Verify OTP request body: $request")
        return try {
            val response = NetworkClients.authClient.post("verify-otp") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body<AuthResponse>()
            
            android.util.Log.d("AuthRepository", "Verify OTP response: $response")
            
            if (!response.token.isNullOrEmpty()) {
                response.token?.let { saveToken(it) }
            }
            Result.success(response)
        } catch (e: Exception) {
            android.util.Log.e("AuthRepository", "Verify OTP failed", e)
            Result.failure(e)
        }
    }

    override suspend fun logout() {
        prefs.edit().remove("jwt_token").apply()
        _isLoggedIn.value = false
    }

    override suspend fun saveToken(token: String) {
        prefs.edit().putString("jwt_token", token).apply()
        _isLoggedIn.value = true
    }

    override suspend fun getToken(): String? {
        return prefs.getString("jwt_token", null)
    }

    override fun isUserLoggedIn(): Boolean {
        return prefs.getString("jwt_token", null) != null
    }
}
