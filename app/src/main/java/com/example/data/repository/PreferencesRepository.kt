package com.example.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.vakilDataStore: DataStore<Preferences> by preferencesDataStore(name = "vakilyar_prefs")

class PreferencesRepository(private val context: Context) {

    private object Keys {
        val DARK_THEME = booleanPreferencesKey("dark_theme")
        val LOGGED_IN_USER_ID = longPreferencesKey("logged_in_user_id")
        val SEEDED = booleanPreferencesKey("initial_seeded")
    }

    val isDarkTheme: Flow<Boolean> = context.vakilDataStore.data.map { prefs ->
        prefs[Keys.DARK_THEME] ?: false
    }

    val loggedInUserId: Flow<Long?> = context.vakilDataStore.data.map { prefs ->
        prefs[Keys.LOGGED_IN_USER_ID]?.takeIf { it > 0L }
    }

    val isSeeded: Flow<Boolean> = context.vakilDataStore.data.map { prefs ->
        prefs[Keys.SEEDED] ?: false
    }

    suspend fun setDarkTheme(enabled: Boolean) {
        context.vakilDataStore.edit { it[Keys.DARK_THEME] = enabled }
    }

    suspend fun setLoggedInUserId(userId: Long?) {
        context.vakilDataStore.edit { prefs ->
            if (userId == null) prefs.remove(Keys.LOGGED_IN_USER_ID)
            else prefs[Keys.LOGGED_IN_USER_ID] = userId
        }
    }

    suspend fun markSeeded() {
        context.vakilDataStore.edit { it[Keys.SEEDED] = true }
    }
}
