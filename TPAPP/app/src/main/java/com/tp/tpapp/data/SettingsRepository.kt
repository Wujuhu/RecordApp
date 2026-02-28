package com.tp.tpapp.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {

    val themeModeFlow: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[THEME_MODE] ?: -1
    }

    val fontSizeFlow: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[FONT_SIZE] ?: 1 
    }

    val maxLinesFlow: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[MAX_LINES] ?: 5
    }

    val showIndexFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[SHOW_INDEX] ?: true
    }

    val recordDeleteConfirmFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[RECORD_DELETE_CONFIRM] ?: true
    }

    val passwordDeleteConfirmFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PASSWORD_DELETE_CONFIRM] ?: true
    }

    val startupTabFlow: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[STARTUP_TAB] ?: STARTUP_TAB_RECORD
    }

    val passwordDefaultVisibleFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PASSWORD_DEFAULT_VISIBLE] ?: false
    }

    suspend fun setThemeMode(mode: Int) {
        context.dataStore.edit { preferences ->
            preferences[THEME_MODE] = mode
        }
    }

    suspend fun setFontSize(size: Int) {
        context.dataStore.edit { preferences ->
            preferences[FONT_SIZE] = size
        }
    }

    suspend fun setMaxLines(lines: Int) {
        context.dataStore.edit { preferences ->
            preferences[MAX_LINES] = lines
        }
    }

    suspend fun setShowIndex(show: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[SHOW_INDEX] = show
        }
    }

    suspend fun setRecordDeleteConfirm(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[RECORD_DELETE_CONFIRM] = enabled
        }
    }

    suspend fun setPasswordDeleteConfirm(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PASSWORD_DELETE_CONFIRM] = enabled
        }
    }

    suspend fun setStartupTab(tab: Int) {
        context.dataStore.edit { preferences ->
            preferences[STARTUP_TAB] = tab
        }
    }

    suspend fun setPasswordDefaultVisible(visible: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PASSWORD_DEFAULT_VISIBLE] = visible
        }
    }

    // Singleton logic similar to TPRepository
    companion object {
        val THEME_MODE = intPreferencesKey("theme_mode") // -1: System, 0: Light, 1: Dark
        val FONT_SIZE = intPreferencesKey("font_size") // 0: Small, 1: Normal, 2: Large
        val MAX_LINES = intPreferencesKey("max_lines") // max lines for record preview, default 5
        val SHOW_INDEX = booleanPreferencesKey("show_index") // whether to show sequence numbering
        val RECORD_DELETE_CONFIRM = booleanPreferencesKey("record_delete_confirm")
        val PASSWORD_DELETE_CONFIRM = booleanPreferencesKey("password_delete_confirm")
        val STARTUP_TAB = intPreferencesKey("startup_tab")
        val PASSWORD_DEFAULT_VISIBLE = booleanPreferencesKey("password_default_visible")

        const val STARTUP_TAB_RECORD = 0
        const val STARTUP_TAB_PASSWORD = 1

        @Volatile
        private var INSTANCE: SettingsRepository? = null

        fun getInstance(context: Context): SettingsRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = SettingsRepository(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
}
