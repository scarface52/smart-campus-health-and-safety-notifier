package com.theretros.smartcampus

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

class SessionManager(context: Context) {

    private val prefs =
        context.getSharedPreferences("session", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_USER_ID = "user_id"
        private const val KEY_IS_ADMIN = "is_admin"
    }

    fun saveSession(userId: String, isAdmin: Boolean) {
        prefs.edit()
            .putString(KEY_USER_ID, userId)
            .putBoolean(KEY_IS_ADMIN, isAdmin)
            .apply()
    }

    fun getUserId(): String? =
        prefs.getString(KEY_USER_ID, null)

    fun isAdmin(): Boolean =
        prefs.getBoolean(KEY_IS_ADMIN, false)

    fun clearSession() {
        prefs.edit().clear().apply()
    }
}
