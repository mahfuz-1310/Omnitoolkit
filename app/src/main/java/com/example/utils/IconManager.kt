package com.example.utils

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import com.example.R

/**
 * Dynamic Custom Launcher Icon Manager.
 * Safely switches application launcher activity-aliases via PackageManager.setComponentEnabledSetting().
 */
object IconManager {

    private const val TAG = "IconManager"

    data class IconVariant(
        val name: String,
        val alias: String,
        val description: String,
        val drawableResId: Int,
        val isDefault: Boolean = false
    )

    // Complete list of registered activity-aliases in AndroidManifest.xml
    val variants = listOf(
        IconVariant(
            name = "Default Blue",
            alias = ".DefaultBlue",
            description = "Standard clean blue theme",
            drawableResId = R.drawable.namegen_premium_logo_1787339369777,
            isDefault = true
        ),
        IconVariant(
            name = "Dark Stealth",
            alias = ".DarkStealth",
            description = "Sleek dark stealth mode",
            drawableResId = R.drawable.ic_launcher_dark
        ),
        IconVariant(
            name = "Vibrant Gradient",
            alias = ".VibrantGradient",
            description = "Neon pink & cyan gradient",
            drawableResId = R.drawable.ic_launcher_neon
        ),
        IconVariant(
            name = "Crimson Red",
            alias = ".CrimsonRed",
            description = "Bold crimson accent",
            drawableResId = R.drawable.ic_launcher_crimson
        ),
        IconVariant(
            name = "Gold Premium",
            alias = ".GoldPremium",
            description = "Luxury gold & black",
            drawableResId = R.drawable.ic_launcher_gold
        ),
        IconVariant(
            name = "Cyber Purple",
            alias = ".CyberPurple",
            description = "Futuristic cyber purple",
            drawableResId = R.drawable.ic_launcher_purple
        )
    )

    /**
     * Safely applies the target launcher icon by enabling its activity-alias and
     * disabling all other alias components without killing the application process.
     */
    fun applyIcon(context: Context, targetAliasOrName: String): Boolean {
        return try {
            val packageName = context.packageName
            val pm = context.packageManager

            // Normalize target alias
            val matchedVariant = variants.find {
                it.alias.equals(targetAliasOrName, ignoreCase = true) ||
                it.name.equals(targetAliasOrName, ignoreCase = true) ||
                targetAliasOrName.endsWith(it.alias)
            } ?: variants[0]

            var enabledSuccess = false

            variants.forEach { variant ->
                val fullAliasName = if (variant.alias.startsWith(".")) {
                    "$packageName${variant.alias}"
                } else {
                    "$packageName.${variant.alias}"
                }

                val componentName = ComponentName(packageName, fullAliasName)
                val isTarget = (variant.alias == matchedVariant.alias)

                val newState = if (isTarget) {
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                } else {
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED
                }

                try {
                    val currentState = pm.getComponentEnabledSetting(componentName)
                    if (currentState != newState) {
                        pm.setComponentEnabledSetting(
                            componentName,
                            newState,
                            PackageManager.DONT_KILL_APP
                        )
                        Log.d(TAG, "Component $fullAliasName state set to $newState")
                    }
                    if (isTarget) {
                        enabledSuccess = true
                    }
                } catch (componentEx: Exception) {
                    Log.w(TAG, "Error updating component state for $fullAliasName: ${componentEx.message}")
                }
            }

            Toast.makeText(
                context,
                "Launcher icon updated to ${matchedVariant.name}!",
                Toast.LENGTH_SHORT
            ).show()
            enabledSuccess
        } catch (e: Exception) {
            Log.e(TAG, "Failed to apply icon: ${e.message}", e)
            Toast.makeText(
                context,
                "Could not switch icon: ${e.localizedMessage ?: "Unknown error"}",
                Toast.LENGTH_SHORT
            ).show()
            false
        }
    }

    /**
     * Determines which activity-alias is currently active.
     */
    fun getActiveIcon(context: Context): IconVariant {
        return try {
            val pm = context.packageManager
            val packageName = context.packageName

            for (variant in variants) {
                val fullAliasName = if (variant.alias.startsWith(".")) {
                    "$packageName${variant.alias}"
                } else {
                    "$packageName.${variant.alias}"
                }
                val componentName = ComponentName(packageName, fullAliasName)
                val state = pm.getComponentEnabledSetting(componentName)
                if (state == PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
                    return variant
                }
            }
            variants.firstOrNull { it.isDefault } ?: variants[0]
        } catch (e: Exception) {
            variants[0]
        }
    }
}
