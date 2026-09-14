package com.example.utils

import android.content.Context
import android.content.pm.PackageManager
import android.provider.Settings
import android.util.Log
import rikka.shizuku.Shizuku
import java.io.BufferedReader
import java.io.InputStreamReader

object RefreshRateManager {
    private const val TAG = "RefreshRateManager"

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

    fun setRefreshRates(context: Context, peak: Float?, min: Float?): Boolean {
        val cmds = mutableListOf<String>()
        if (peak != null) {
            cmds.add("settings put system peak_refresh_rate $peak")
        } else {
            cmds.add("settings delete system peak_refresh_rate")
        }

        if (min != null) {
            cmds.add("settings put system min_refresh_rate $min")
        } else {
            cmds.add("settings delete system min_refresh_rate")
        }

        // 1. Try Shizuku
        if (isShizukuAvailable() && hasShizukuPermission()) {
            try {
                var allSuccess = true
                for (cmd in cmds) {
                    if (!executeShizukuCommand(cmd)) {
                        allSuccess = false
                    }
                }
                if (allSuccess) {
                    Log.d(TAG, "Successfully updated refresh rates via Shizuku: peak=$peak, min=$min")
                    return true
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update refresh rates via Shizuku", e)
            }
        }

        // 2. Try Secure Settings (if WRITE_SECURE_SETTINGS or WRITE_SETTINGS is granted)
        try {
            val cr = context.contentResolver
            if (peak != null) {
                Settings.System.putFloat(cr, "peak_refresh_rate", peak)
            } else {
                Settings.System.putString(cr, "peak_refresh_rate", null)
            }
            if (min != null) {
                Settings.System.putFloat(cr, "min_refresh_rate", min)
            } else {
                Settings.System.putString(cr, "min_refresh_rate", null)
            }
            Log.d(TAG, "Successfully updated refresh rates via System Settings: peak=$peak, min=$min")
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update refresh rates via System Settings", e)
        }

        // 3. Try Root (su)
        if (DnsSecureSettingsHelper.isRootAvailable()) {
            try {
                var allSuccess = true
                for (cmd in cmds) {
                    val process = Runtime.getRuntime().exec(arrayOf("su", "-c", cmd))
                    if (process.waitFor() != 0) {
                        allSuccess = false
                    }
                }
                if (allSuccess) {
                    Log.d(TAG, "Successfully updated refresh rates via Root: peak=$peak, min=$min")
                    return true
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update refresh rates via Root", e)
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

    fun getCurrentPeakRefreshRate(context: Context): Float {
        try {
            val valStr = Settings.System.getString(context.contentResolver, "peak_refresh_rate")
            if (!valStr.isNullOrEmpty()) {
                val parsed = valStr.toFloatOrNull()
                if (parsed != null && parsed > 0f) return parsed
            }
        } catch (e: Exception) {
            // Ignore
        }

        if (isShizukuAvailable() && hasShizukuPermission()) {
            var process: Process? = null
            try {
                val cmdArgs = arrayOf("settings", "get", "system", "peak_refresh_rate")
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
                    return output.toFloatOrNull() ?: 120.0f
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to read peak refresh rate via Shizuku", e)
            } finally {
                process?.destroy()
            }
        }

        return 120.0f
    }

    fun getCurrentMinRefreshRate(context: Context): Float {
        try {
            val valStr = Settings.System.getString(context.contentResolver, "min_refresh_rate")
            if (!valStr.isNullOrEmpty()) {
                val parsed = valStr.toFloatOrNull()
                if (parsed != null && parsed > 0f) return parsed
            }
        } catch (e: Exception) {
            // Ignore
        }

        if (isShizukuAvailable() && hasShizukuPermission()) {
            var process: Process? = null
            try {
                val cmdArgs = arrayOf("settings", "get", "system", "min_refresh_rate")
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
                    return output.toFloatOrNull() ?: 60.0f
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to read min refresh rate via Shizuku", e)
            } finally {
                process?.destroy()
            }
        }

        return 60.0f
    }
}
