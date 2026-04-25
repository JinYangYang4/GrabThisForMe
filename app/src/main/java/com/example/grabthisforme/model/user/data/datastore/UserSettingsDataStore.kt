package com.example.grabthisforme.model.user.data.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.grabthisforme.model.user.domain.UserSetting
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.userSettingsDataStore by preferencesDataStore(name = "user_settings")

@Singleton
class UserSettingsRepository @Inject constructor(
    @ApplicationContext context: Context
) {
    private val dataStore = context.userSettingsDataStore

    private object Keys {
        val themeMode = intPreferencesKey("theme_mode")
        val homePageMode = intPreferencesKey("home_page_mode")
        val chatBackground = stringPreferencesKey("chat_background")
        val receiveNotification = booleanPreferencesKey("receive_notification")
    }

    val settings: Flow<UserSetting> = dataStore.data.map { preferences ->
        UserSetting(
            themeMode = preferences[Keys.themeMode] ?: 0,
            homePageMode = preferences[Keys.homePageMode] ?: 0,
            chatBackground = preferences[Keys.chatBackground] ?: "",
            receiveNotification = preferences[Keys.receiveNotification] ?: true
        )
    }

    suspend fun updateSettings(settings: UserSetting) {
        dataStore.edit { preferences ->
            preferences[Keys.themeMode] = settings.themeMode
            preferences[Keys.homePageMode] = settings.homePageMode
            preferences[Keys.chatBackground] = settings.chatBackground
            preferences[Keys.receiveNotification] = settings.receiveNotification
        }
    }

    suspend fun updateThemeMode(themeMode: Int) {
        dataStore.edit { preferences ->
            preferences[Keys.themeMode] = themeMode
        }
    }

    suspend fun updateHomePageMode(homePageMode: Int) {
        dataStore.edit { preferences ->
            preferences[Keys.homePageMode] = homePageMode
        }
    }

    suspend fun updateChatBackground(chatBackground: String) {
        dataStore.edit { preferences ->
            preferences[Keys.chatBackground] = chatBackground
        }
    }

    suspend fun updateReceiveNotification(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[Keys.receiveNotification] = enabled
        }
    }
}
