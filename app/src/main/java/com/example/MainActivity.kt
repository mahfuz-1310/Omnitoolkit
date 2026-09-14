package com.example

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.service.FloatingOverlayService
import com.example.ui.MainViewModel
import com.example.ui.MainViewModelFactory
import com.example.ui.theme.NameGenProTheme
import rikka.shizuku.Shizuku

class MainActivity : ComponentActivity(), Shizuku.OnRequestPermissionResultListener {
    
    fun checkAndRequestShizukuPermission() {
        try {
            if (Shizuku.pingBinder()) {
                if (Shizuku.checkSelfPermission() != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    Shizuku.requestPermission(1001)
                } else {
                    // Permission already granted
                }
            } else {
                // android.widget.Toast.makeText(this, "Shizuku service is not running!", android.widget.Toast.LENGTH_SHORT).show()
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    override fun onRequestPermissionResult(requestCode: Int, grantResult: Int) {
        if (grantResult == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            android.widget.Toast.makeText(this, "Shizuku Permission Granted", android.widget.Toast.LENGTH_SHORT).show()
        } else {
            android.widget.Toast.makeText(this, "Shizuku Permission Denied", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            Shizuku.removeRequestPermissionResultListener(this)
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        com.example.feature.common.metrics.StartupMetrics.onActivityCreate()
        
        // Register Shizuku permission listener without auto-requesting permission prompts on startup
        com.example.utils.DnsSecureSettingsHelper.registerPermissionListener()
        try {
            Shizuku.addRequestPermissionResultListener(this)
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        
        enableEdgeToEdge()

        val app = application as NameGenApplication
        val viewModel = app.sharedViewModel

        setContent {
            com.example.feature.common.metrics.StartupMetrics.onFirstCompose()
            
            LaunchedEffect(Unit) {
                com.example.feature.common.metrics.StartupMetrics.onFirstDraw()
                com.example.feature.common.metrics.StartupMetrics.onFullContentRendered()
                com.example.feature.common.startup.StartupDefer.postFirstFrame {
                    // Preload light caches if needed
                }
            }
            val darkModeState by viewModel.darkMode.collectAsStateWithLifecycle()
            val whiteThemeState by viewModel.whiteTheme.collectAsStateWithLifecycle()
            val isDarkTheme = darkModeState ?: isSystemInDarkTheme()
            val uiColorLong by viewModel.uiColor.collectAsStateWithLifecycle()
            val buttonColorLong by viewModel.buttonColor.collectAsStateWithLifecycle()
            val uiColor = Color(uiColorLong.toInt())
            val buttonColor = Color(buttonColorLong.toInt())

            val context = androidx.compose.ui.platform.LocalContext.current
            val activity = context as? android.app.Activity
            LaunchedEffect(isDarkTheme, whiteThemeState) {
                activity?.window?.let { win ->
                    val isLightMode = !isDarkTheme || whiteThemeState
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                        win.insetsController?.setSystemBarsAppearance(
                            if (isLightMode) android.view.WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS else 0,
                            android.view.WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
                        )
                        win.insetsController?.setSystemBarsAppearance(
                            if (isLightMode) android.view.WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS else 0,
                            android.view.WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                        )
                    } else {
                        @Suppress("DEPRECATION")
                        var flags = win.decorView.systemUiVisibility
                        flags = if (isLightMode) {
                            flags or android.view.View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR or android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                        } else {
                            flags and android.view.View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR.inv() and android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
                        }
                        win.decorView.systemUiVisibility = flags
                    }
                    win.navigationBarColor = if (whiteThemeState) 0xFFFFFFFF.toInt() else if (isDarkTheme) 0xFF0B0B0F.toInt() else 0xFFF8F9FA.toInt()
                }
            }

            NameGenProTheme(darkTheme = isDarkTheme, whiteTheme = whiteThemeState, uiColor = uiColor, buttonColor = buttonColor) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    com.example.ui.navigation.MainAppScreen(viewModel = viewModel)
                }
            }
        }
    }
}
