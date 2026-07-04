package com.example.grabthisforme.model.network

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val Context.authTokenDataStore by preferencesDataStore(name = "auth_token")

@Singleton
class AuthTokenDataStore @Inject constructor(
    @ApplicationContext context: Context
) {
    private val dataStore = context.authTokenDataStore

    private object Keys {
        val token = stringPreferencesKey("token")
    }

    val tokenFlow: Flow<String?> = dataStore.data.map { preferences ->
        preferences[Keys.token]
    }

    suspend fun getToken(): String? {
        return tokenFlow.first()
    }

    suspend fun saveToken(token: String) {
        dataStore.edit { preferences ->
            preferences[Keys.token] = token
        }
    }

    suspend fun clearToken() {
        dataStore.edit { preferences ->
            preferences.remove(Keys.token)
        }
    }
}

