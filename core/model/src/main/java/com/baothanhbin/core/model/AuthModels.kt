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
data class AuthResponse(
    val token: String? = null,
    val userId: String? = null,
    val name: String? = null,
    val email: String? = null,
    val message: String? = null,
    val needRegister: Boolean = false,
    val isRegister: Boolean = false
)
