package com.example.grabthisforme.model.auth.data.repository

import android.util.Log
import com.example.grabthisforme.model.auth.data.network.api.AuthApi
import com.example.grabthisforme.model.auth.data.network.dto.LoginRequest
import com.example.grabthisforme.model.auth.data.network.dto.RegisterRequest
import com.example.grabthisforme.model.network.AuthTokenDataStore
import com.example.grabthisforme.model.user.data.repository.UserRepository
import com.example.grabthisforme.model.user.domain.User
import com.example.grabthisforme.model.user.mapper.toDomain
import javax.inject.Inject
import javax.inject.Singleton
import org.json.JSONObject
import retrofit2.HttpException

@Singleton
class AuthRepository @Inject constructor(
    private val authApi: AuthApi,
    private val authTokenDataStore: AuthTokenDataStore,
    private val userRepository: UserRepository
) {

    suspend fun login(identifier: String, password: String): Result<User> {
        return runCatching {
            val response = authApi.login(
                LoginRequest(
                    identifier = identifier,
                    password = password
                )
            )
            val data = requireSuccessfulData(response.code, response.message, response.data)
            authTokenDataStore.saveToken(data.token)
            val user = data.user.toDomain(
                passwordHash = password,
                isLoginAccount = true
            )
            userRepository.upsertAndSetCurrent(user)
            user
        }.recoverCatching { throwable ->
            throw throwable.toReadableAuthException()
        }
    }

    suspend fun register(
        accountName: String,
        password: String,
        displayName: String? = null,
        phone: String? = null,
        email: String? = null
    ): Result<User> {
        return runCatching {
            val response = authApi.register(
                RegisterRequest(
                    accountName = accountName,
                    password = password,
                    displayName = displayName,
                    phone = phone,
                    email = email
                )
            )

            val data = requireSuccessfulData(response.code, response.message, response.data)
            Log.d("test11", "saveToken: ${data.token}")
            authTokenDataStore.saveToken(data.token)
            val user = data.user.toDomain(
                passwordHash = password,
                isLoginAccount = true
            )
            userRepository.upsertUser(user)
            user
        }.recoverCatching { throwable ->
            throw throwable.toReadableAuthException()
        }
    }

    private fun <T> requireSuccessfulData(code: Int, message: String, data: T?): T {
        if (code != 0 || data == null) {
            error(message.ifBlank { "Network request failed" })
        }
        return data
    }

    private fun Throwable.toReadableAuthException(): Throwable {
        if (this !is HttpException) return this
        val errorBody = response()?.errorBody()?.string().orEmpty()
        val readableMessage = runCatching {
            JSONObject(errorBody).optString("message")
        }.getOrNull().orEmpty()
        if (readableMessage.isNotBlank()) {
            return IllegalStateException(readableMessage, this)
        }
        return this
    }
}
