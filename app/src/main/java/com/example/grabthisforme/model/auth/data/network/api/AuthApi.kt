package com.example.grabthisforme.model.auth.data.network.api

import com.example.grabthisforme.model.auth.data.network.dto.AuthResultDto
import com.example.grabthisforme.model.auth.data.network.dto.LoginRequest
import com.example.grabthisforme.model.auth.data.network.dto.RegisterRequest
import com.example.grabthisforme.model.network.ApiResponse
import com.example.grabthisforme.model.user.data.network.dto.UserDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthApi {

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): ApiResponse<AuthResultDto>

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): ApiResponse<AuthResultDto>

    @GET("api/auth/me")
    suspend fun me(): ApiResponse<UserDto>
}
