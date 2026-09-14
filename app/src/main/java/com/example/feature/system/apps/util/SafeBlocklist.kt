package com.example.feature.system.apps.util

import android.content.Context
import android.content.Intent

object SafeBlocklist {
    private val HARDCODED_CRITICAL_PACKAGES = setOf(
        "android",
        "com.android.systemui",
        "com.android.packageinstaller",
        "com.google.android.packageinstaller",
        "com.android.settings",
        "com.android.vending",
        "com.google.android.gms",
        "com.google.android.gsf",
        "rikka.shizuku",
        "moe.shizuku.privileged.api"
    )

    fun isSafeBlocklisted(packageName: String, context: Context): Boolean {
        // 1. Current app must never disable itself
        if (packageName == context.packageName) return true

        // 2. Critical system services
        if (HARDCODED_CRITICAL_PACKAGES.contains(packageName.lowercase())) return true

        // 3. Default home launcher (disabling launcher soft-locks system UI)
        try {
            val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
            val resolveInfo = context.packageManager.resolveActivity(intent, 0)
            if (resolveInfo?.activityInfo?.packageName.equals(packageName, ignoreCase = true)) {
                return true
            }
        } catch (_: Exception) {}

        return false
    }
}
