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
import com.baothanhbin.core.model.SignupRequest
import com.baothanhbin.core.model.UpdateProfileRequest
import com.baothanhbin.core.model.UpdateProfileResponse
import com.baothanhbin.core.model.UserProfile
import com.baothanhbin.core.model.VerifyForgotOtpRequest
import com.baothanhbin.core.model.VerifyForgotOtpResponse
import com.baothanhbin.core.model.VerifyOtpRequest
import com.baothanhbin.core.network.NetworkClients
import io.ktor.client.call.body
import io.ktor.client.plugins.ResponseException
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

    override suspend fun login(request: LoginRequest): Result<AuthResponse> = executeRequest {
        val response = NetworkClients.authClient.post("login") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body<AuthResponse>()

        if (!response.token.isNullOrEmpty()) {
            response.token?.let { saveToken(it) }
        }

        response
    }

    override suspend fun signup(request: SignupRequest): Result<AuthResponse> = executeRequest {
        NetworkClients.authClient.post("signup") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    override suspend fun verifyOtp(request: VerifyOtpRequest): Result<AuthResponse> = executeRequest {
        val response = NetworkClients.authClient.post("verify-otp") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body<AuthResponse>()

        if (!response.token.isNullOrEmpty()) {
            response.token?.let { saveToken(it) }
        }

        response
    }

    override suspend fun getProfile(): Result<UserProfile> {
        return runAuthorized { token ->
            NetworkClients.authClient.get("me") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }.body()
        }
    }

    override suspend fun updateProfile(request: UpdateProfileRequest): Result<UpdateProfileResponse> {
        return runAuthorized { token ->
            NetworkClients.authClient.put("me") {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body()
        }
    }

    override suspend fun changePassword(request: ChangePasswordRequest): Result<MessageResponse> {
        return runAuthorized { token ->
            NetworkClients.authClient.post("change-password") {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body()
        }
    }

    override suspend fun forgotPassword(request: ForgotPasswordRequest): Result<MessageResponse> = executeRequest {
        NetworkClients.authClient.post("forgot-password") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    override suspend fun verifyForgotOtp(request: VerifyForgotOtpRequest): Result<VerifyForgotOtpResponse> = executeRequest {
        NetworkClients.authClient.post("verify-forgot-otp") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    override suspend fun resetPassword(request: ResetPasswordRequest): Result<MessageResponse> = executeRequest {
        NetworkClients.authClient.post("reset-password") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    override suspend fun logout() {
        authDataStore.clearToken()
    }

    override suspend fun saveToken(token: String) {
        authDataStore.saveToken(token)
    }

    override suspend fun getToken(): String? {
        return authDataStore.getToken()
    }

    override fun isUserLoggedIn(): Boolean {
        return runCatching {
            kotlinx.coroutines.runBlocking {
                authDataStore.getToken() != null
            }
        }.getOrDefault(false)
    }

    private suspend inline fun <reified T> executeRequest(
        crossinline requestBlock: suspend () -> T
    ): Result<T> {
        return try {
            Result.success(requestBlock())
        } catch (error: ResponseException) {
            Result.failure(IllegalStateException(extractApiError(error.response)))
        } catch (error: IOException) {
            Result.failure(IllegalStateException("Không thể kết nối đến máy chủ."))
        } catch (error: Exception) {
            Result.failure(error)
        }
    }

    private suspend fun extractApiError(response: HttpResponse): String {
        return runCatching {
            val bodyText = response.bodyAsText()
            val parsed = errorJson.decodeFromString<ApiErrorResponse>(bodyText)
            parsed.message ?: parsed.error ?: "Yêu cầu không thành công."
        }.getOrDefault("Yêu cầu không thành công.")
    }

    private suspend inline fun <reified T> runAuthorized(
        crossinline block: suspend (String) -> T
    ): Result<T> {
        val token = authDataStore.getToken()
            ?: return Result.failure(IllegalStateException("Phiên đăng nhập không hợp lệ."))

        return executeRequest {
            block(token)
        }
    }
}
