package com.baothanhbin.core.data.impl

import com.baothanhbin.core.data.repository.AuthRepository
import com.baothanhbin.core.datastore.AuthDataStore
import com.baothanhbin.core.model.ApiErrorResponse
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
import com.baothanhbin.core.model.VerifyForgotOtpRequest
import com.baothanhbin.core.model.VerifyForgotOtpResponse
import com.baothanhbin.core.model.VerifyOtpRequest
import com.baothanhbin.core.network.NetworkClients
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.Json

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authDataStore: AuthDataStore
) : AuthRepository {

    private val errorJson = Json { ignoreUnknownKeys = true }

    override val isLoggedIn: Flow<Boolean> = authDataStore.isLoggedInFlow()
    override val currentUserId: Flow<String?> = authDataStore.currentUserIdFlow()

    override suspend fun login(request: LoginRequest): Result<AuthResponse> {
        return executeResponseRequest<AuthResponse> {
            NetworkClients.authClient.post("login") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        }.onSuccess { response ->
            if (!response.token.isNullOrEmpty()) {
                response.token?.let { saveSession(it, response.userId) }
            }
        }
    }

    override suspend fun signup(request: SignupRequest): Result<AuthResponse> {
        return executeResponseRequest {
            NetworkClients.authClient.post("signup") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        }
    }

    override suspend fun verifyOtp(request: VerifyOtpRequest): Result<AuthResponse> {
        return executeResponseRequest<AuthResponse> {
            NetworkClients.authClient.post("verify-otp") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        }.onSuccess { response ->
            if (!response.token.isNullOrEmpty()) {
                response.token?.let { saveSession(it, response.userId) }
            }
        }
    }

    override suspend fun resendOtp(request: ResendOtpRequest): Result<MessageResponse> {
        return executeResponseRequest {
            NetworkClients.authClient.post("resend-otp") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        }
    }

    override suspend fun getProfile(): Result<UserProfile> {
        return runAuthorized { token ->
            NetworkClients.authClient.get("me") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
        }
    }

    override suspend fun updateProfile(request: UpdateProfileRequest): Result<UpdateProfileResponse> {
        return runAuthorized { token ->
            NetworkClients.authClient.put("me") {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        }
    }

    override suspend fun changePassword(request: ChangePasswordRequest): Result<MessageResponse> {
        return runAuthorized { token ->
            NetworkClients.authClient.post("change-password") {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        }
    }

    override suspend fun forgotPassword(request: ForgotPasswordRequest): Result<MessageResponse> {
        return executeResponseRequest {
            NetworkClients.authClient.post("forgot-password") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        }
    }

    override suspend fun verifyForgotOtp(request: VerifyForgotOtpRequest): Result<VerifyForgotOtpResponse> {
        return executeResponseRequest {
            NetworkClients.authClient.post("verify-forgot-otp") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        }
    }

    override suspend fun resetPassword(request: ResetPasswordRequest): Result<MessageResponse> {
        return executeResponseRequest {
            NetworkClients.authClient.post("reset-password") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        }
    }

    override suspend fun logout() {
        authDataStore.clearToken()
    }

    override suspend fun saveToken(token: String) {
        authDataStore.saveToken(token)
    }

    override suspend fun saveSession(token: String, userId: String?) {
        authDataStore.saveSession(token, userId)
    }

    override suspend fun saveUserId(userId: String) {
        authDataStore.saveUserId(userId)
    }

    override suspend fun getToken(): String? {
        return authDataStore.getToken()
    }

    override suspend fun getCurrentUserId(): String? {
        return authDataStore.getUserId()
    }

    override fun isUserLoggedIn(): Boolean {
        return runCatching {
            kotlinx.coroutines.runBlocking {
                authDataStore.getToken() != null
            }
        }.getOrDefault(false)
    }

    private suspend inline fun <reified T> executeResponseRequest(
        crossinline requestBlock: suspend () -> HttpResponse
    ): Result<T> {
        return try {
            val response = requestBlock()
            if (!response.status.isSuccess()) {
                return Result.failure(IllegalStateException(extractApiError(response)))
            }
            Result.success(response.body())
        } catch (error: IOException) {
            Result.failure(IllegalStateException("Khong the ket noi den may chu."))
        } catch (error: Exception) {
            Result.failure(error)
        }
    }

    private suspend fun extractApiError(response: HttpResponse): String {
        return runCatching {
            val bodyText = response.bodyAsText()
            val parsed = errorJson.decodeFromString<ApiErrorResponse>(bodyText)
            parsed.message ?: parsed.error ?: "Yeu cau khong thanh cong."
        }.getOrDefault("Yeu cau khong thanh cong.")
    }

    private suspend inline fun <reified T> runAuthorized(
        crossinline block: suspend (String) -> HttpResponse
    ): Result<T> {
        val token = authDataStore.getToken()
            ?: return Result.failure(IllegalStateException("Phien dang nhap khong hop le."))

        return executeResponseRequest {
            block(token)
        }
    }
}
