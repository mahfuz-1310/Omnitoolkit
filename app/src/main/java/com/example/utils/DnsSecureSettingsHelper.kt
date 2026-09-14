package com.example.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import rikka.shizuku.Shizuku
import java.io.BufferedReader
import java.io.InputStreamReader

object DnsSecureSettingsHelper {
    private const val TAG = "DnsSecureSettings"

    private var isListenerRegistered = false
    private var pendingAction: (() -> Unit)? = null

    /**
     * Registers the Shizuku permission listener if it hasn't been registered yet.
     */
    fun registerPermissionListener() {
        if (isListenerRegistered) return
        try {
            Shizuku.addRequestPermissionResultListener { _, grantResult ->
                if (grantResult == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "Shizuku permission granted! Executing pending action.")
                    pendingAction?.invoke()
                    pendingAction = null
                }
            }
            isListenerRegistered = true
        } catch (e: Throwable) {
            Log.e(TAG, "Failed to register Shizuku permission listener", e)
        }
    }

    /**
     * Sets a pending action to execute as soon as Shizuku permission is granted.
     */
    fun setPendingAction(action: () -> Unit) {
        this.pendingAction = action
    }

    /**
     * Launches Android's native Private DNS settings with a direct intent or fallback sequence.
     */
    fun openPrivateDnsSettings(context: Context) {
        try {
            // a) Direct private DNS settings
            val intent = Intent("android.settings.PRIVATE_DNS_SETTINGS")
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            try {
                // b) Fallback 1: Wireless Settings
                val intent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            } catch (ex: Exception) {
                try {
                    // b) Fallback 2: Network Operator Settings
                    val intent = Intent(Settings.ACTION_NETWORK_OPERATOR_SETTINGS)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                } catch (exc: Exception) {
                    try {
                        // Final Fallback: Base Settings
                        val intent = Intent(Settings.ACTION_SETTINGS)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                    } catch (finalEx: Exception) {
                        Toast.makeText(context, "Could not open settings.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    /**
     * Checks if Shizuku is running.
     */
    fun isShizukuAvailable(): Boolean {
        return try {
            Shizuku.pingBinder()
        } catch (e: Throwable) {
            false
        }
    }

    /**
     * Checks if Shizuku permission is granted.
     */
    fun hasShizukuPermission(): Boolean {
        if (!isShizukuAvailable()) return false
        return try {
            if (Shizuku.getLatestServiceVersion() >= 11) {
                Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
            } else {
                // Older versions
                true
            }
        } catch (e: Throwable) {
            false
        }
    }

    /**
     * Requests Shizuku permission.
     */
    fun requestShizukuPermission(requestCode: Int) {
        try {
            Shizuku.requestPermission(requestCode)
        } catch (e: Throwable) {
            Log.e(TAG, "Failed to request Shizuku permission", e)
        }
    }

    /**
     * Checks if the app has WRITE_SECURE_SETTINGS permission natively.
     */
    fun hasWriteSecureSettingsPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            context.checkSelfPermission(android.Manifest.permission.WRITE_SECURE_SETTINGS) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    /**
     * Checks if root access (su) is available.
     */
    fun isRootAvailable(): Boolean {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("which", "su"))
            val exitCode = process.waitFor()
            exitCode == 0
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Attempts to update Private DNS settings using:
     * 1. Direct secure settings write (if WRITE_SECURE_SETTINGS is granted)
     * 2. Shizuku API (if Shizuku is active and authorized)
     * 3. Root superuser (if su is available)
     *
     * Returns true if any method succeeds, false otherwise.
     */
    fun setPrivateDns(context: Context, mode: String, specifier: String?): Boolean {
        Log.d(TAG, "Attempting to set Private DNS. Mode: $mode, Specifier: $specifier")

        // 1. Try Direct Secure Settings Write (requires WRITE_SECURE_SETTINGS ADB permission)
        if (hasWriteSecureSettingsPermission(context)) {
            try {
                Settings.Global.putString(context.contentResolver, "private_dns_mode", mode)
                if (mode == "hostname" && specifier != null) {
                    Settings.Global.putString(context.contentResolver, "private_dns_specifier", specifier)
                }
                Log.d(TAG, "Successfully updated DNS via direct Secure Settings write.")
                return true
            } catch (e: Exception) {
                Log.e(TAG, "Direct Secure Settings write failed", e)
            }
        }

        // 2. Try Shizuku API
        if (isShizukuAvailable() && hasShizukuPermission()) {
            try {
                val modeSuccess = executeShizukuCommand("settings put global private_dns_mode $mode")
                val specifierSuccess = if (mode == "hostname" && specifier != null) {
                    executeShizukuCommand("settings put global private_dns_specifier $specifier")
                } else {
                    true
                }
                if (modeSuccess && specifierSuccess) {
                    Log.d(TAG, "Successfully updated DNS via Shizuku shell process.")
                    return true
                }
            } catch (e: Exception) {
                Log.e(TAG, "Shizuku settings write failed", e)
            }
        }

        // 3. Try Root SU execution
        if (isRootAvailable()) {
            try {
                val modeCmd = "settings put global private_dns_mode $mode"
                val specifierCmd = if (mode == "hostname" && specifier != null) {
                    "settings put global private_dns_specifier $specifier"
                } else {
                    null
                }

                val modeSuccess = executeRootCommand(modeCmd)
                val specifierSuccess = if (specifierCmd != null) executeRootCommand(specifierCmd) else true

                if (modeSuccess && specifierSuccess) {
                    Log.d(TAG, "Successfully updated DNS via Root command execution.")
                    return true
                }
            } catch (e: Exception) {
                Log.e(TAG, "Root command execution failed", e)
            }
        }

        return false
    }

    private fun executeShizukuCommand(command: String): Boolean {
        var process: Process? = null
        return try {
            val cmdArgs = command.split(" ").toTypedArray()
            // Invoke Shizuku.newProcess via reflection to bypass Kotlin visibility restrictions
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

    private fun executeRootCommand(command: String): Boolean {
        var process: Process? = null
        return try {
            process = Runtime.getRuntime().exec(arrayOf("su", "-c", command))
            val exitValue = process.waitFor()
            exitValue == 0
        } catch (e: Exception) {
            Log.e(TAG, "Error executing root command: $command", e)
            false
        } finally {
            process?.destroy()
        }
    }
}
