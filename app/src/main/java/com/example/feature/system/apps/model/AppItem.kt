package com.example.feature.system.apps.model

import android.graphics.drawable.Drawable
import androidx.compose.runtime.Immutable

@Immutable
enum class AppEnabledState {
    ENABLED,
    DISABLED,
    DISABLED_USER,
    SUSPENDED,
    HIDDEN
}

@Immutable
data class AppItem(
    val appName: String,
    val packageName: String,
    val versionName: String,
    val versionCode: Long,
    val firstInstallTime: Long,
    val lastUpdateTime: Long,
    val firstInstallTimeFormatted: String,
    val lastUpdateTimeFormatted: String,
    val isSystemApp: Boolean,
    val isLaunchable: Boolean,
    val isEnabled: Boolean,
    val enabledState: AppEnabledState,
    val isSafeBlocklisted: Boolean,
    val icon: Drawable? = null
)
