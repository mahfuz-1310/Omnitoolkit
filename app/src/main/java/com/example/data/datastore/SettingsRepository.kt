package com.example.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(private val dataStore: DataStore<Preferences>) {

    companion object {
        val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")
        val WHITE_THEME_KEY = booleanPreferencesKey("white_theme")
        val HAPTIC_FEEDBACK_KEY = booleanPreferencesKey("haptic_feedback")
        val SOUND_EFFECTS_KEY = booleanPreferencesKey("sound_effects")
        val APP_ICON_KEY = androidx.datastore.preferences.core.stringPreferencesKey("app_icon")
        val ANIMATIONS_KEY = booleanPreferencesKey("animations")
        val FONT_STYLE_KEY = androidx.datastore.preferences.core.stringPreferencesKey("font_style")
        val FLOATING_MODE_KEY = booleanPreferencesKey("floating_mode")
        val REMEMBER_FLOATING_POSITION_KEY = booleanPreferencesKey("remember_floating_position")
        val DEFAULT_FLOATING_SIZE_KEY = androidx.datastore.preferences.core.stringPreferencesKey("default_floating_size")
        val FLOATING_POS_X_KEY = androidx.datastore.preferences.core.floatPreferencesKey("floating_pos_x")
        val FLOATING_POS_Y_KEY = androidx.datastore.preferences.core.floatPreferencesKey("floating_pos_y")
        val UI_COLOR_KEY = androidx.datastore.preferences.core.longPreferencesKey("ui_color")
        val BUTTON_COLOR_KEY = androidx.datastore.preferences.core.longPreferencesKey("button_color")
        val SAVED_TEXT_KEY = androidx.datastore.preferences.core.stringPreferencesKey("saved_text")
        val SAVED_LIST_KEY = androidx.datastore.preferences.core.stringPreferencesKey("saved_list_json")
    }

    val darkModeFlow: Flow<Boolean?> = dataStore.data.map { preferences ->
        preferences[DARK_MODE_KEY]
    }

    val whiteThemeFlow: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[WHITE_THEME_KEY] ?: false
    }

    val hapticFeedbackFlow: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[HAPTIC_FEEDBACK_KEY] ?: true
    }

    val soundEffectsFlow: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[SOUND_EFFECTS_KEY] ?: true
    }

    val appIconFlow: Flow<String> = dataStore.data.map { preferences ->
        preferences[APP_ICON_KEY] ?: "Default Blue"
    }

    val fontStyleFlow: Flow<String> = dataStore.data.map { preferences ->
        preferences[FONT_STYLE_KEY] ?: "Default"
    }

    val animationsFlow: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[ANIMATIONS_KEY] ?: true
    }

    val floatingModeFlow: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[FLOATING_MODE_KEY] ?: false
    }

    val rememberFloatingPositionFlow: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[REMEMBER_FLOATING_POSITION_KEY] ?: true
    }

    val defaultFloatingSizeFlow: Flow<String> = dataStore.data.map { preferences ->
        preferences[DEFAULT_FLOATING_SIZE_KEY] ?: "Compact"
    }

    val floatingPosXFlow: Flow<Float> = dataStore.data.map { preferences ->
        preferences[FLOATING_POS_X_KEY] ?: 50f
    }

    val floatingPosYFlow: Flow<Float> = dataStore.data.map { preferences ->
        preferences[FLOATING_POS_Y_KEY] ?: 150f
    }

    val uiColorFlow: Flow<Long> = dataStore.data.map { preferences ->
        preferences[UI_COLOR_KEY] ?: 0xFF6C63FFL
    }

    val buttonColorFlow: Flow<Long> = dataStore.data.map { preferences ->
        preferences[BUTTON_COLOR_KEY] ?: 0xFF6C63FFL
    }

    val savedTextFlow: Flow<String> = dataStore.data.map { preferences ->
        preferences[SAVED_TEXT_KEY] ?: ""
    }

    val savedListFlow: Flow<List<String>> = dataStore.data.map { preferences ->
        val jsonString = preferences[SAVED_LIST_KEY]
        if (!jsonString.isNullOrEmpty()) {
            try {
                val array = org.json.JSONArray(jsonString)
                val list = mutableListOf<String>()
                for (i in 0 until array.length()) {
                    list.add(array.getString(i))
                }
                list
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            val oldText = preferences[SAVED_TEXT_KEY] ?: ""
            if (oldText.isNotBlank()) listOf(oldText) else emptyList()
        }
    }

    suspend fun setDarkMode(isDark: Boolean?) {
        dataStore.edit { preferences ->
            if (isDark == null) {
                preferences.remove(DARK_MODE_KEY)
            } else {
                preferences[DARK_MODE_KEY] = isDark
            }
        }
    }

    suspend fun setWhiteTheme(isWhite: Boolean) {
        dataStore.edit { preferences ->
            preferences[WHITE_THEME_KEY] = isWhite
        }
    }

    suspend fun setHapticFeedback(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[HAPTIC_FEEDBACK_KEY] = enabled
        }
    }

    suspend fun setSoundEffects(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[SOUND_EFFECTS_KEY] = enabled
        }
    }

    suspend fun setAppIcon(iconName: String) {
        dataStore.edit { preferences ->
            preferences[APP_ICON_KEY] = iconName
        }
    }

    suspend fun setFontStyle(style: String) {
        dataStore.edit { preferences ->
            preferences[FONT_STYLE_KEY] = style
        }
    }

    suspend fun setAnimations(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[ANIMATIONS_KEY] = enabled
        }
    }

    suspend fun setFloatingMode(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[FLOATING_MODE_KEY] = enabled
        }
    }

    suspend fun setRememberFloatingPosition(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[REMEMBER_FLOATING_POSITION_KEY] = enabled
        }
    }

    suspend fun setDefaultFloatingSize(size: String) {
        dataStore.edit { preferences ->
            preferences[DEFAULT_FLOATING_SIZE_KEY] = size
        }
    }

    suspend fun setFloatingPosition(x: Float, y: Float) {
        dataStore.edit { preferences ->
            preferences[FLOATING_POS_X_KEY] = x
            preferences[FLOATING_POS_Y_KEY] = y
        }
    }

    suspend fun setUiColor(color: Long) {
        dataStore.edit { preferences ->
            preferences[UI_COLOR_KEY] = color
        }
    }

    suspend fun setButtonColor(color: Long) {
        dataStore.edit { preferences ->
            preferences[BUTTON_COLOR_KEY] = color
        }
    }

    suspend fun setSavedText(text: String) {
        dataStore.edit { preferences ->
            preferences[SAVED_TEXT_KEY] = text
        }
    }

    suspend fun addSavedText(text: String) {
        if (text.isBlank()) return
        dataStore.edit { preferences ->
            val jsonString = preferences[SAVED_LIST_KEY]
            val currentList = mutableListOf<String>()
            if (!jsonString.isNullOrEmpty()) {
                try {
                    val array = org.json.JSONArray(jsonString)
                    for (i in 0 until array.length()) {
                        currentList.add(array.getString(i))
                    }
                } catch (e: Exception) { }
            } else {
                val oldText = preferences[SAVED_TEXT_KEY] ?: ""
                if (oldText.isNotBlank()) currentList.add(oldText)
            }
            // Prepend new text to top
            currentList.add(0, text)
            preferences[SAVED_LIST_KEY] = org.json.JSONArray(currentList).toString()
            preferences.remove(SAVED_TEXT_KEY)
        }
    }

    suspend fun deleteSavedTextAt(index: Int) {
        dataStore.edit { preferences ->
            val jsonString = preferences[SAVED_LIST_KEY]
            val currentList = mutableListOf<String>()
            if (!jsonString.isNullOrEmpty()) {
                try {
                    val array = org.json.JSONArray(jsonString)
                    for (i in 0 until array.length()) {
                        currentList.add(array.getString(i))
                    }
                } catch (e: Exception) { }
            } else {
                val oldText = preferences[SAVED_TEXT_KEY] ?: ""
                if (oldText.isNotBlank()) currentList.add(oldText)
            }
            if (index in currentList.indices) {
                currentList.removeAt(index)
                preferences[SAVED_LIST_KEY] = org.json.JSONArray(currentList).toString()
            }
            preferences.remove(SAVED_TEXT_KEY)
        }
    }

    suspend fun clearSavedList() {
        dataStore.edit { preferences ->
            preferences.remove(SAVED_LIST_KEY)
            preferences.remove(SAVED_TEXT_KEY)
        }
    }

    suspend fun resetColors() {
        dataStore.edit { preferences ->
            preferences[UI_COLOR_KEY] = 0xFF6C63FFL
            preferences[BUTTON_COLOR_KEY] = 0xFF6C63FFL
        }
    }
}
