package com.example.grabthisforme.model.auth.data.network.dto

import com.example.grabthisforme.model.user.data.network.dto.UserDto

data class LoginRequest(
    val identifier: String,
    val password: String
)

data class RegisterRequest(
    val accountName: String,
    val password: String,
    val displayName: String? = null,
    val phone: String? = null,
    val email: String? = null
)

data class AuthResultDto(
    val token: String,
    val user: UserDto
)
