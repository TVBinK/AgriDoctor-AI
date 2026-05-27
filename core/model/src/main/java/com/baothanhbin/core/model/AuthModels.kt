package com.baothanhbin.core.model

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val email: String,
    val password: String? = null
)

@Serializable
data class SignupRequest(
    val email: String,
    val name: String,
    val password: String? = null
)

@Serializable
data class VerifyOtpRequest(
    val email: String,
    val otp: String
)

@Serializable
data class ResendOtpRequest(
    val email: String,
    val purpose: String
)

@Serializable
data class UserProfile(
    val userId: String,
    val name: String,
    val email: String,
    val phone: String = "",
    val address: String = "",
    val createdAt: String? = null,
    val updatedAt: String? = null
)

@Serializable
data class UpdateProfileRequest(
    val name: String,
    val phone: String = "",
    val address: String = ""
)

@Serializable
data class UpdateProfileResponse(
    val message: String,
    val user: UserProfile
)

@Serializable
data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String,
    val confirmPassword: String
)

@Serializable
data class ForgotPasswordRequest(
    val email: String
)

@Serializable
data class VerifyForgotOtpRequest(
    val email: String,
    val otp: String
)

@Serializable
data class VerifyForgotOtpResponse(
    val message: String,
    val resetToken: String
)

@Serializable
data class ResetPasswordRequest(
    val email: String,
    val resetToken: String,
    val newPassword: String,
    val confirmPassword: String
)

@Serializable
data class MessageResponse(
    val message: String
)

@Serializable
data class ApiErrorResponse(
    val message: String? = null,
    val error: String? = null
)

@Serializable
data class AuthResponse(
    val token: String? = null,
    val userId: String? = null,
    val name: String? = null,
    val email: String? = null,
    val message: String? = null,
    val needRegister: Boolean = false,
    val isRegister: Boolean = false
)
