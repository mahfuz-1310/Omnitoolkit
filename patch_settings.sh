sed -i -e '/val ANIMATIONS_KEY = booleanPreferencesKey("animations")/a\
        val FONT_STYLE_KEY = androidx.datastore.preferences.core.stringPreferencesKey("font_style")' \
       -e '/val animationsFlow: Flow<Boolean> = dataStore.data.map { preferences ->/i\
    val fontStyleFlow: Flow<String> = dataStore.data.map { preferences ->\
        preferences[FONT_STYLE_KEY] ?: "Default"\
    }\
' \
       -e '/suspend fun setAnimations(enabled: Boolean) {/i\
    suspend fun setFontStyle(style: String) {\
        dataStore.edit { preferences ->\
            preferences[FONT_STYLE_KEY] = style\
        }\
    }\
' app/src/main/java/com/example/data/datastore/SettingsRepository.kt
