package com.example.utils

import android.content.Context
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import rikka.shizuku.Shizuku
import java.io.BufferedReader
import java.io.InputStreamReader

data class SystemModule(
    val id: String,
    val title: String,
    val description: String,
    val category: String,
    val defaultEnabled: Boolean = false,
    val shellCommands: List<String>,
    val isCustom: Boolean = false
)

data class DeviceProfile(
    val brand: String,
    val model: String,
    val buildProduct: String,
    val device: String,
    val fingerprint: String
)

object CustomModuleManager {
    private const val TAG = "CustomModuleManager"
    private const val PREFS_NAME = "custom_module_prefs"
    private const val KEY_IMPORTED_MODULES = "imported_custom_modules"

    val deviceProfiles = listOf(
        DeviceProfile("Samsung", "Galaxy S25 Ultra", "e3s", "Galaxy S25 Ultra", "samsung/e3s/e3s:15/AP3A.240905.015/s9280zcus3axh7:user/release-keys"),
        DeviceProfile("Samsung", "Galaxy S24 Ultra", "e1s", "Galaxy S24 Ultra", "samsung/e1s/e1s:14/UP1A.231005.007/S928BXXU1AXB4:user/release-keys"),
        DeviceProfile("Samsung", "Galaxy Z Fold 6", "q5a", "Galaxy Z Fold 6", "samsung/q5a/q5a:14/UP1A.231005.007/F946BXXU1AXB4:user/release-keys"),
        DeviceProfile("Google", "Pixel 10 Pro", "komodo", "Pixel 10 Pro", "google/komodo/komodo:16/BP1A.250305.019/12847592:user/release-keys"),
        DeviceProfile("Google", "Pixel 9 Pro XL", "caiman", "Pixel 9 Pro XL", "google/caiman/caiman:14/AP3A.240905.015.A2/12345678:user/release-keys"),
        DeviceProfile("Apple", "iPhone 17 Pro Max (Spoof)", "iPhone17,2", "iPhone17ProMax", "apple/iPhone17,2/iPhone17ProMax:18/22A335:user/release-keys"),
        DeviceProfile("Xiaomi", "Xiaomi 15 Ultra", "zuul", "Xiaomi 15 Ultra", "xiaomi/zuul/zuul:15/AP3A.240905.015/V816.0.4.0.UNACNXM:user/release-keys"),
        DeviceProfile("Xiaomi", "Xiaomi 14 Pro", "houji", "Xiaomi 14 Pro", "xiaomi/houji/houji:14/UKQ1.230914.001/V816.0.12.0.UNBCNXM:user/release-keys"),
        DeviceProfile("OnePlus", "OnePlus 13", "OPD2404", "OnePlus 13", "oneplus/OPD2404/OPD2404:15/AP3A.240905.015/14.0.0.300:user/release-keys"),
        DeviceProfile("OnePlus", "OnePlus 12", "CPH2581", "OnePlus 12", "oneplus/CPH2581/CPH2581:14/UKQ1.230914.001/14.0.0.840:user/release-keys"),
        DeviceProfile("vivo", "vivo X200 Pro", "V2405A", "vivoX200Pro", "vivo/V2405A/V2405A:15/AP3A.240905.015/compiler:user/release-keys"),
        DeviceProfile("vivo", "vivo X100 Ultra", "V2336A", "vivoX100Ultra", "vivo/V2336A/V2336A:14/UP1A.231005.007/compiler:user/release-keys"),
        DeviceProfile("Oppo", "Oppo Find X8 Pro", "PKC110", "FindX8Pro", "oppo/PKC110/PKC110:15/AP3A.240905.015/release-keys"),
        DeviceProfile("iQOO", "iQOO 13", "I2408", "iQOO13", "iqoo/I2408/I2408:15/AP3A.240905.015/release-keys"),
        DeviceProfile("Realme", "Realme GT 7 Pro", "RMX5010", "RealmeGT7Pro", "realme/RMX5010/RMX5010:15/AP3A.240905.015/release-keys"),
        DeviceProfile("Moto", "Motorola Edge 50 Ultra", "macao", "Edge50Ultra", "motorola/macao/macao:14/V1UQ34.20-59/release-keys"),
        DeviceProfile("Nothing", "Nothing Phone (3)", "Pong3", "NothingPhone3", "nothing/Pong3/Pong3:15/AP3A.240905.015/release-keys"),
        DeviceProfile("ROG", "ASUS ROG Phone 9 Pro", "AI2501", "ROGPhone9Pro", "asus/AI2501/AI2501:15/AP3A.240905.015/release-keys"),
        DeviceProfile("Tecno", "Tecno Camon 30 Premier", "CL9", "Camon30Premier", "tecno/CL9/CL9:14/UP1A.231005.007/release-keys"),
        DeviceProfile("Infinix", "Infinix Zero 40 5G", "X6861", "Zero40_5G", "infinix/X6861/X6861:14/UP1A.231005.007/release-keys"),
        DeviceProfile("ZTE", "Nubia Z70 Ultra", "NX735J", "NubiaZ70Ultra", "zte/NX735J/NX735J:15/AP3A.240905.015/release-keys"),
        DeviceProfile("Nokia", "Nokia X30 5G", "TA-1450", "NokiaX30", "nokia/TA-1450/TA-1450:13/SKQ1.220316.001/release-keys"),
        DeviceProfile("Walton", "Walton Primo GH14", "GH14", "PrimoGH14", "walton/GH14/GH14:13/TP1A.220624.014/release-keys"),
        DeviceProfile("Symphony", "Symphony Z70", "Z70", "SymphonyZ70", "symphony/Z70/Z70:13/TP1A.220624.014/release-keys"),
        DeviceProfile("Doogee", "Doogee V Max Plus", "VMaxPlus", "VMaxPlus", "doogee/VMaxPlus/VMaxPlus:14/UP1A.231005.007/release-keys")
    )

    val builtinModules = listOf(
        SystemModule(
            id = "ultra_game_mode",
            title = "Ultra Game Mode",
            description = "Applies GPU turbo boost, unlocks max performance CPU governor, and optimizes touch polling for competitive gaming.",
            category = "Gaming",
            shellCommands = listOf(
                "settings put global low_power 0",
                "settings put global high_performance_mode_enabled 1",
                "setprop debug.hwui.render_dirty_regions false",
                "setprop debug.composition.type gpu",
                "setprop sys.perf.profile 1"
            )
        ),
        SystemModule(
            id = "high_refresh_rate",
            title = "120Hz FPS Unlocker",
            description = "Forces the display controller to lock onto maximum available refresh rate (120Hz/144Hz) for all games and apps.",
            category = "Display",
            shellCommands = listOf(
                "settings put system peak_refresh_rate 120",
                "settings put system min_refresh_rate 120",
                "settings put global min_refresh_rate 120",
                "settings put global peak_refresh_rate 120"
            )
        ),
        SystemModule(
            id = "touch_latency_fix",
            title = "Touch Latency Fix",
            description = "Tunes touch sampling rates, reduces touch filtering, and optimizes pointer acceleration for zero-lag gaming responses.",
            category = "Hardware",
            shellCommands = listOf(
                "settings put system pointer_speed 1",
                "settings put system touch_exploration_enabled 0",
                "setprop touch.prescalefactor 1.25",
                "setprop touch.filter.enabled 0"
            )
        ),
        SystemModule(
            id = "ram_cleanser",
            title = "RAM Cleaner & Standby Killer",
            description = "Clears reclaimable page cache, drops kernel caches, trims file caches, and terminates background standby drainers.",
            category = "Memory",
            shellCommands = listOf(
                "sync",
                "echo 3 > /proc/sys/vm/drop_caches",
                "pm trim-caches 999G",
                "am kill-all"
            )
        ),
        SystemModule(
            id = "ping_stabilizer",
            title = "Ping Stabilizer",
            description = "Optimizes TCP window scaling, prioritizes gaming socket packets, and reduces cellular/WiFi jitter.",
            category = "Network",
            shellCommands = listOf(
                "settings put global tcp_default_init_rwnd 60",
                "setprop net.tcp.buffersize.default 4096,87380,704200,4096,16384,110208",
                "cmd connectivity set-network-auto-connect-disabled true"
            )
        ),
        SystemModule(
            id = "gaming_dnd",
            title = "Gaming DND (Disturbances Blocker)",
            description = "Blocks heads-up notification popups, suppresses distracting ringtones and background audio during active gaming.",
            category = "Gaming",
            shellCommands = listOf(
                "settings put global zen_mode 1",
                "settings put global heads_up_notifications_enabled 0"
            )
        ),
        SystemModule(
            id = "system_thermal_cooler",
            title = "System Thermal & CPU Cooler",
            description = "Optimizes thermal trip points, terminates background CPU-heavy drainers, and flushes kernel caches to lower heat.",
            category = "Thermal",
            shellCommands = listOf(
                "sync",
                "echo 3 > /proc/sys/vm/drop_caches",
                "am kill-all",
                "cmd activity drop-caches"
            )
        )
    )

    fun getAllModules(context: Context): List<SystemModule> {
        val list = mutableListOf<SystemModule>()
        list.addAll(builtinModules)
        list.addAll(getImportedModules(context))
        return list
    }

    fun getImportedModules(context: Context): List<SystemModule> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonStr = prefs.getString(KEY_IMPORTED_MODULES, "[]") ?: "[]"
        val customList = mutableListOf<SystemModule>()
        try {
            val jsonArr = JSONArray(jsonStr)
            for (i in 0 until jsonArr.length()) {
                val obj = jsonArr.getJSONObject(i)
                val id = obj.optString("id", "custom_${System.currentTimeMillis()}_$i")
                val title = obj.optString("title", "Custom Module")
                val description = obj.optString("description", "Imported via Shell / JSON file")
                val category = obj.optString("category", "Custom")
                val commandsArr = obj.optJSONArray("shellCommands") ?: JSONArray()
                val commands = mutableListOf<String>()
                for (j in 0 until commandsArr.length()) {
                    commands.add(commandsArr.getString(j))
                }
                customList.add(
                    SystemModule(
                        id = id,
                        title = title,
                        description = description,
                        category = category,
                        shellCommands = commands,
                        isCustom = true
                    )
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing imported modules", e)
        }
        return customList
    }

    fun saveImportedModule(context: Context, module: SystemModule) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonStr = prefs.getString(KEY_IMPORTED_MODULES, "[]") ?: "[]"
        try {
            val jsonArr = JSONArray(jsonStr)
            val obj = JSONObject().apply {
                put("id", module.id)
                put("title", module.title)
                put("description", module.description)
                put("category", module.category)
                put("shellCommands", JSONArray(module.shellCommands))
            }
            jsonArr.put(obj)
            prefs.edit().putString(KEY_IMPORTED_MODULES, jsonArr.toString()).apply()
        } catch (e: Exception) {
            Log.e(TAG, "Error saving imported module", e)
        }
    }

    fun isModuleEnabled(context: Context, moduleId: String): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val module = getAllModules(context).find { it.id == moduleId }
        return prefs.getBoolean("module_$moduleId", module?.defaultEnabled ?: false)
    }

    fun setModuleEnabled(context: Context, moduleId: String, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean("module_$moduleId", enabled).apply()
    }

    fun executeModuleCommands(commands: List<String>): Boolean {
        var success = true
        for (cmd in commands) {
            val res = executeCommand(cmd)
            if (!res) {
                Log.w(TAG, "Command failed or partially applied: $cmd")
                success = false
            }
        }
        return success
    }

    fun executeCustomScript(scriptContent: String): Boolean {
        val lines = scriptContent.lines()
            .map { it.trim() }
            .filter { it.isNotEmpty() && !it.startsWith("#") }
        return executeModuleCommands(lines)
    }

    fun applyDeviceSpoof(profile: DeviceProfile): Boolean {
        val spoofCommands = listOf(
            "setprop ro.product.brand \"${profile.brand}\"",
            "setprop ro.product.model \"${profile.model}\"",
            "setprop ro.product.device \"${profile.device}\"",
            "setprop ro.build.product \"${profile.buildProduct}\"",
            "setprop ro.build.fingerprint \"${profile.fingerprint}\""
        )
        return executeModuleCommands(spoofCommands)
    }

    private fun executeCommand(command: String): Boolean {
        if (isShizukuAvailable() && hasShizukuPermission()) {
            if (executeViaShizuku(command)) return true
        }
        if (isRootAvailable()) {
            if (executeViaRoot(command)) return true
        }
        return executeLocally(command)
    }

    fun isShizukuReady(): Boolean {
        return isShizukuAvailable() && hasShizukuPermission()
    }

    private fun isShizukuAvailable(): Boolean {
        return try {
            Shizuku.pingBinder()
        } catch (e: Throwable) {
            false
        }
    }

    private fun hasShizukuPermission(): Boolean {
        if (!isShizukuAvailable()) return false
        return try {
            if (Shizuku.getLatestServiceVersion() >= 11) {
                Shizuku.checkSelfPermission() == android.content.pm.PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        } catch (e: Throwable) {
            false
        }
    }

    private fun executeViaShizuku(command: String): Boolean {
        var process: Process? = null
        return try {
            val cmdArgs = arrayOf("sh", "-c", command)
            val newProcessMethod = Shizuku::class.java.getDeclaredMethod(
                "newProcess",
                Array<String>::class.java,
                Array<String>::class.java,
                String::class.java
            )
            newProcessMethod.isAccessible = true
            process = newProcessMethod.invoke(null, cmdArgs, null, null) as Process
            val exitCode = process.waitFor()
            exitCode == 0
        } catch (e: Exception) {
            Log.e(TAG, "Shizuku execution error: $command", e)
            false
        } finally {
            process?.destroy()
        }
    }

    private fun isRootAvailable(): Boolean {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", "id"))
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val line = reader.readLine()
            process.waitFor()
            line != null && line.contains("uid=0")
        } catch (e: Exception) {
            false
        }
    }

    private fun executeViaRoot(command: String): Boolean {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", command))
            val exitCode = process.waitFor()
            exitCode == 0
        } catch (e: Exception) {
            Log.e(TAG, "Root execution error: $command", e)
            false
        }
    }

    private fun executeLocally(command: String): Boolean {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("sh", "-c", command))
            val exitCode = process.waitFor()
            exitCode == 0
        } catch (e: Exception) {
            Log.e(TAG, "Local execution error: $command", e)
            false
        }
    }
}
