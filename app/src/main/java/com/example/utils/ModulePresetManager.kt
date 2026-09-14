package com.example.utils

import android.content.Context
import org.json.JSONObject

data class ModulePreset(
    val name: String,
    val description: String,
    val enabledModuleIds: List<String>
)

object ModulePresetManager {
    private const val PREFS_NAME = "module_presets_prefs"
    private const val KEY_PRESETS = "saved_presets"

    val defaultPresets = listOf(
        ModulePreset(
            name = "Gaming Mode",
            description = "Maximum performance, high refresh rate, and touch optimization.",
            enabledModuleIds = listOf("ui_perf_boost", "high_refresh_rate", "touch_optimizer")
        ),
        ModulePreset(
            name = "Power Save",
            description = "Minimizes background activity and balances performance.",
            enabledModuleIds = listOf("ram_cleanser")
        ),
        ModulePreset(
            name = "Extreme Speed",
            description = "All performance and standby killers active.",
            enabledModuleIds = listOf("ui_perf_boost", "ram_cleanser", "high_refresh_rate", "touch_optimizer")
        )
    )

    fun getPresets(context: Context): List<ModulePreset> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonStr = prefs.getString(KEY_PRESETS, null) ?: return defaultPresets
        return try {
            val jsonObj = JSONObject(jsonStr)
            val list = mutableListOf<ModulePreset>()
            // Always include default presets + user custom presets
            list.addAll(defaultPresets)
            // Load custom stored ones if any
            val keys = jsonObj.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                if (defaultPresets.none { it.name == key }) {
                    val arr = jsonObj.getJSONArray(key)
                    val ids = mutableListOf<String>()
                    for (i in 0 until arr.length()) {
                        ids.add(arr.getString(i))
                    }
                    list.add(ModulePreset(name = key, description = "Custom saved profile", enabledModuleIds = ids))
                }
            }
            list
        } catch (e: Exception) {
            defaultPresets
        }
    }

    fun saveCustomPreset(context: Context, name: String, enabledIds: List<String>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonStr = prefs.getString(KEY_PRESETS, "{}")
        try {
            val jsonObj = JSONObject(jsonStr ?: "{}")
            val arr = org.json.JSONArray(enabledIds)
            jsonObj.put(name, arr)
            prefs.edit().putString(KEY_PRESETS, jsonObj.toString()).apply()
        } catch (e: Exception) {
            // ignore
        }
    }
}
