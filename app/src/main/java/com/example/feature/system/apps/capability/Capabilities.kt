package com.example.feature.system.apps.capability

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import com.example.deviceadmin.AppDeviceAdminReceiver
import rikka.shizuku.Shizuku

enum class AppManagerCapability {
    SHIZUKU,
    DEVICE_OWNER,
    NONE
}

object Capabilities {
    fun detectCapability(context: Context): AppManagerCapability {
        // 1. Check Shizuku
        try {
            if (Shizuku.pingBinder()) {
                val check = Shizuku.checkSelfPermission()
                if (check == PackageManager.PERMISSION_GRANTED) {
                    return AppManagerCapability.SHIZUKU
                }
            }
        } catch (_: Throwable) {}

        // 2. Check Device Owner / Profile Owner
        try {
            val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as? DevicePolicyManager
            if (dpm != null) {
                if (dpm.isDeviceOwnerApp(context.packageName) || dpm.isProfileOwnerApp(context.packageName)) {
                    return AppManagerCapability.DEVICE_OWNER
                }
            }
        } catch (_: Throwable) {}

        return AppManagerCapability.NONE
    }

    fun isShizukuInstalledAndRunning(): Boolean {
        return try {
            Shizuku.pingBinder()
        } catch (_: Throwable) {
            false
        }
    }

    fun requestShizukuPermission(requestCode: Int = 1001): Boolean {
        return try {
            if (Shizuku.pingBinder()) {
                if (Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED) {
                    Shizuku.requestPermission(requestCode)
                    true
                } else {
                    true
                }
            } else {
                false
            }
        } catch (_: Throwable) {
            false
        }
    }

    fun getAdminComponent(context: Context): ComponentName {
        return ComponentName(context, AppDeviceAdminReceiver::class.java)
    }
}
