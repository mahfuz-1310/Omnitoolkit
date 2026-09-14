package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.ui.screens.AnimationSettingsScreen
import com.example.ui.theme.NameGenProTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class AnimationSettingsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Read styling preferences synchronously
        val app = application as NameGenApplication
        val isDark = runBlocking { app.settingsRepository.darkModeFlow.first() }
        val uiColorVal = runBlocking { app.settingsRepository.uiColorFlow.first() }
        val buttonColorVal = runBlocking { app.settingsRepository.buttonColorFlow.first() }

        val isSystemDark = (resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES
        val resolvedDarkTheme = isDark ?: isSystemDark

        setContent {
            NameGenProTheme(
                darkTheme = resolvedDarkTheme,
                uiColor = Color(uiColorVal.toInt()),
                buttonColor = Color(buttonColorVal.toInt())
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AnimationSettingsScreen(
                        onBack = { finish() }
                    )
                }
            }
        }
    }
}
