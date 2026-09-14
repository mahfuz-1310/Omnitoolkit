package com.example.utils

import android.content.Context
import android.content.pm.PackageManager
import android.provider.Settings
import android.util.Log
import rikka.shizuku.Shizuku
import java.io.BufferedReader
import java.io.InputStreamReader

object AnimationTweakManager {
    private const val TAG = "AnimationTweakManager"

    fun isShizukuAvailable(): Boolean {
        return try {
            Shizuku.pingBinder()
        } catch (e: Throwable) {
            false
        }
    }

    fun hasShizukuPermission(): Boolean {
        if (!isShizukuAvailable()) return false
        return try {
            if (Shizuku.getLatestServiceVersion() >= 11) {
                Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        } catch (e: Throwable) {
            false
        }
    }

    fun requestShizukuPermission(requestCode: Int) {
        try {
            Shizuku.requestPermission(requestCode)
        } catch (e: Throwable) {
            Log.e(TAG, "Failed to request Shizuku permission", e)
        }
    }

    fun setAnimationScales(context: Context, scale: Float): Boolean {
        val cmd1 = "settings put global window_animation_scale $scale"
        val cmd2 = "settings put global transition_animation_scale $scale"
        val cmd3 = "settings put global animator_duration_scale $scale"

        // 1. Try Shizuku if available and permitted
        if (isShizukuAvailable() && hasShizukuPermission()) {
            try {
                val s1 = executeShizukuCommand(cmd1)
                val s2 = executeShizukuCommand(cmd2)
                val s3 = executeShizukuCommand(cmd3)
                if (s1 && s2 && s3) {
                    Log.d(TAG, "Successfully set animation scales via Shizuku to $scale")
                    return true
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to set animation scales via Shizuku", e)
            }
        }

        // 2. Try Direct Secure Settings Write (if WRITE_SECURE_SETTINGS is granted)
        if (DnsSecureSettingsHelper.hasWriteSecureSettingsPermission(context)) {
            try {
                Settings.Global.putFloat(context.contentResolver, Settings.Global.WINDOW_ANIMATION_SCALE, scale)
                Settings.Global.putFloat(context.contentResolver, Settings.Global.TRANSITION_ANIMATION_SCALE, scale)
                Settings.Global.putFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, scale)
                Log.d(TAG, "Successfully set animation scales via Secure Settings to $scale")
                return true
            } catch (e: Exception) {
                Log.e(TAG, "Failed to set animation scales via Secure Settings", e)
            }
        }

        // 3. Fallback to root (su) if available
        if (DnsSecureSettingsHelper.isRootAvailable()) {
            try {
                val p1 = Runtime.getRuntime().exec(arrayOf("su", "-c", cmd1))
                val p2 = Runtime.getRuntime().exec(arrayOf("su", "-c", cmd2))
                val p3 = Runtime.getRuntime().exec(arrayOf("su", "-c", cmd3))
                if (p1.waitFor() == 0 && p2.waitFor() == 0 && p3.waitFor() == 0) {
                    Log.d(TAG, "Successfully set animation scales via Root to $scale")
                    return true
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to set animation scales via Root", e)
            }
        }

        return false
    }

    private fun executeShizukuCommand(command: String): Boolean {
        var process: Process? = null
        return try {
            val cmdArgs = command.split(" ").toTypedArray()
            val newProcessMethod = Shizuku::class.java.getDeclaredMethod(
                "newProcess",
                Array<String>::class.java,
                Array<String>::class.java,
                String::class.java
            )
            newProcessMethod.isAccessible = true
            process = newProcessMethod.invoke(null, cmdArgs, null, null) as Process
            val exitValue = process.waitFor()
            exitValue == 0
        } catch (e: Exception) {
            Log.e(TAG, "Error executing Shizuku command: $command", e)
            false
        } finally {
            process?.destroy()
        }
    }

    fun getCurrentAnimationScale(context: Context): Float {
        // Try reading via Secure Settings first
        try {
            val scale = Settings.Global.getFloat(context.contentResolver, Settings.Global.WINDOW_ANIMATION_SCALE, 1.0f)
            return scale
        } catch (e: Exception) {
            // Ignore
        }

        // Try reading via Shizuku if available
        if (isShizukuAvailable() && hasShizukuPermission()) {
            var process: Process? = null
            try {
                val cmdArgs = arrayOf("settings", "get", "global", "window_animation_scale")
                val newProcessMethod = Shizuku::class.java.getDeclaredMethod(
                    "newProcess",
                    Array<String>::class.java,
                    Array<String>::class.java,
                    String::class.java
                )
                newProcessMethod.isAccessible = true
                process = newProcessMethod.invoke(null, cmdArgs, null, null) as Process
                val reader = BufferedReader(InputStreamReader(process.inputStream))
                val output = reader.readLine()?.trim()
                process.waitFor()
                if (!output.isNullOrEmpty() && output != "null") {
                    return output.toFloatOrNull() ?: 1.0f
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to read animation scale via Shizuku", e)
            } finally {
                process?.destroy()
            }
        }

        return 1.0f
    }
}
