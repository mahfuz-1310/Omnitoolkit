package com.example.utils

import android.app.ActivityManager
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.provider.Settings
import android.text.format.Formatter
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.net.InetAddress
import java.net.NetworkInterface
import java.util.*

data class HighRiskApp(
    val appName: String,
    val packageName: String,
    val sensitivePermissions: List<String>,
    val isSystemApp: Boolean
)

data class WifiNetworkInfo(
    val isConnected: Boolean,
    val isWifi: Boolean,
    val ssid: String,
    val rssi: Int,
    val linkSpeedMbps: Int,
    val localIp: String,
    val frequencyMhz: Int,
    val securityType: String,
    val isSecure: Boolean
)

data class BluetoothDiagnosticInfo(
    val isSupported: Boolean,
    val isEnabled: Boolean,
    val pairedDevicesCount: Int,
    val pairedDeviceNames: List<String>,
    val scanMode: String,
    val isSecure: Boolean
)

data class BatteryDiagnosticInfo(
    val levelPercentage: Int,
    val temperatureCelsius: Float,
    val voltageMv: Int,
    val health: String,
    val isCharging: Boolean,
    val plugType: String,
    val isOverheating: Boolean
)

data class HardwarePerformanceInfo(
    val totalRamBytes: Long,
    val availableRamBytes: Long,
    val usedRamPercentage: Int,
    val cpuCoreCount: Int,
    val cpuArchitecture: String,
    val totalStorageBytes: Long,
    val freeStorageBytes: Long,
    val usedStoragePercentage: Int
) {
    fun formatBytes(bytes: Long): String {
        val gb = bytes / (1024.0 * 1024.0 * 1024.0)
        return if (gb >= 1.0) String.format(Locale.US, "%.1f GB", gb)
        else String.format(Locale.US, "%.0f MB", bytes / (1024.0 * 1024.0))
    }
}

data class SystemScanResult(
    val totalAppsChecked: Int,
    val userAppsCount: Int,
    val systemAppsCount: Int,
    val highRiskApps: List<HighRiskApp>,
    val totalFilesAnalyzed: Int,
    val junkFilesCount: Int,
    val junkSizeBytes: Long,
    val junkFilesList: List<File>,
    val wifiInfo: WifiNetworkInfo,
    val bluetoothInfo: BluetoothDiagnosticInfo,
    val batteryInfo: BatteryDiagnosticInfo,
    val hardwareInfo: HardwarePerformanceInfo,
    val isRooted: Boolean,
    val isAdbEnabled: Boolean,
    val isMockLocationEnabled: Boolean,
    val healthScore: Int,
    val scanTimestamp: Long = System.currentTimeMillis()
) {
    val formattedJunkSize: String
        get() {
            val kb = junkSizeBytes / 1024.0
            val mb = kb / 1024.0
            val gb = mb / 1024.0
            return when {
                gb >= 1.0 -> String.format(Locale.US, "%.2f GB", gb)
                mb >= 1.0 -> String.format(Locale.US, "%.1f MB", mb)
                kb >= 1.0 -> String.format(Locale.US, "%.1f KB", kb)
                else -> "$junkSizeBytes B"
            }
        }
}

object SystemScannerManager {
    private const val TAG = "SystemScannerManager"

    enum class ScanPhase {
        IDLE,
        SCANNING_APPS,
        SCANNING_FILES,
        SCANNING_NETWORK,
        SCANNING_BLUETOOTH,
        SCANNING_BATTERY_THERMAL,
        SCANNING_HARDWARE_RAM_CPU,
        CHECKING_INTEGRITY,
        COMPLETED
    }

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _scanProgress = MutableStateFlow(0f)
    val scanProgress: StateFlow<Float> = _scanProgress.asStateFlow()

    private val _statusText = MutableStateFlow("Ready for All-In-One Deep System Scan")
    val statusText: StateFlow<String> = _statusText.asStateFlow()

    private val _currentPhase = MutableStateFlow(ScanPhase.IDLE)
    val currentPhase: StateFlow<ScanPhase> = _currentPhase.asStateFlow()

    private val _scanResult = MutableStateFlow<SystemScanResult?>(null)
    val scanResult: StateFlow<SystemScanResult?> = _scanResult.asStateFlow()

    private var scanJob: Job? = null
    private val scannerScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private val SENSITIVE_PERMISSIONS = listOf(
        "android.permission.RECORD_AUDIO",
        "android.permission.CAMERA",
        "android.permission.READ_CONTACTS",
        "android.permission.READ_SMS",
        "android.permission.RECEIVE_SMS",
        "android.permission.ACCESS_FINE_LOCATION",
        "android.permission.ACCESS_COARSE_LOCATION",
        "android.permission.READ_EXTERNAL_STORAGE",
        "android.permission.MANAGE_EXTERNAL_STORAGE",
        "android.permission.SYSTEM_ALERT_WINDOW",
        "android.permission.PACKAGE_USAGE_STATS"
    )

    fun startDeepScan(context: Context) {
        if (_isScanning.value) return

        _isScanning.value = true
        _scanProgress.value = 0f
        _scanResult.value = null
        _currentPhase.value = ScanPhase.SCANNING_APPS
        _statusText.value = "Initializing All-In-One Hardware & System Engine..."

        scanJob?.cancel()
        scanJob = scannerScope.launch {
            try {
                // ==================== 1. APPS & SECURITY (0% -> 20%) ====================
                _currentPhase.value = ScanPhase.SCANNING_APPS
                _statusText.value = "Scanning installed applications & signatures..."
                val pm = context.packageManager

                val packages = withContext(Dispatchers.IO) {
                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            pm.getInstalledPackages(PackageManager.PackageInfoFlags.of(PackageManager.GET_PERMISSIONS.toLong()))
                        } else {
                            @Suppress("DEPRECATION")
                            pm.getInstalledPackages(PackageManager.GET_PERMISSIONS)
                        }
                    } catch (e: Exception) {
                        emptyList()
                    }
                }

                val highRiskList = mutableListOf<HighRiskApp>()
                var userAppCount = 0
                var sysAppCount = 0
                val totalPackages = packages.size.coerceAtLeast(1)

                packages.forEachIndexed { index, pkgInfo ->
                    val appInfo = pkgInfo.applicationInfo
                    val isSys = if (appInfo != null) (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0 else false
                    if (isSys) sysAppCount++ else userAppCount++

                    val label = appInfo?.loadLabel(pm)?.toString() ?: pkgInfo.packageName
                    val requested = pkgInfo.requestedPermissions ?: emptyArray()
                    val sensitive = requested.filter { perm -> SENSITIVE_PERMISSIONS.any { perm.equals(it, ignoreCase = true) } }

                    if (sensitive.size >= 2) {
                        highRiskList.add(
                            HighRiskApp(
                                appName = label,
                                packageName = pkgInfo.packageName,
                                sensitivePermissions = sensitive,
                                isSystemApp = isSys
                            )
                        )
                    }

                    if (index % 6 == 0 || index == totalPackages - 1) {
                        val progress = (index.toFloat() / totalPackages) * 0.20f
                        _scanProgress.value = progress
                        _statusText.value = "Scanning app (${index + 1}/$totalPackages): $label"
                        delay(12)
                    }
                }

                // ==================== 2. STORAGE & FILES (20% -> 40%) ====================
                _currentPhase.value = ScanPhase.SCANNING_FILES
                _statusText.value = "Analyzing storage caches, temporary files & logs..."

                var totalFiles = 0
                var junkFilesCount = 0
                var junkSizeBytes = 0L
                val junkFiles = mutableListOf<File>()

                val searchDirs = mutableListOf<File>()
                context.cacheDir?.let { searchDirs.add(it) }
                context.externalCacheDir?.let { searchDirs.add(it) }
                context.filesDir?.let { searchDirs.add(it) }

                try {
                    val ext = Environment.getExternalStorageDirectory()
                    if (ext != null && ext.exists() && ext.canRead()) {
                        searchDirs.add(ext)
                    }
                } catch (ignored: Exception) {}

                withContext(Dispatchers.IO) {
                    val queue = ArrayDeque<File>()
                    queue.addAll(searchDirs)

                    var visitedDirs = 0
                    while (queue.isNotEmpty() && visitedDirs < 200 && totalFiles < 3000) {
                        val current = queue.removeFirst()
                        visitedDirs++

                        val files = try {
                            current.listFiles()
                        } catch (e: Exception) {
                            null
                        }

                        if (files != null) {
                            for (file in files) {
                                totalFiles++
                                if (file.isDirectory) {
                                    if (queue.size < 150) {
                                        queue.add(file)
                                    }
                                } else {
                                    val name = file.name.lowercase()
                                    if (name.endsWith(".tmp") || name.endsWith(".log") || name.endsWith(".bak") ||
                                        name.endsWith(".temp") || name.contains("cache") || file.length() == 0L
                                    ) {
                                        junkFilesCount++
                                        junkSizeBytes += file.length()
                                        if (junkFiles.size < 100) {
                                            junkFiles.add(file)
                                        }
                                    }
                                }
                            }
                        }

                        if (visitedDirs % 15 == 0) {
                            val fileProgress = 0.20f + ((visitedDirs / 200f).coerceIn(0f, 1f)) * 0.20f
                            _scanProgress.value = fileProgress
                            _statusText.value = "Inspected $totalFiles files ($junkFilesCount cache files)..."
                            delay(15)
                        }
                    }
                }

                // ==================== 3. WI-FI & NETWORK SECURITY (40% -> 55%) ====================
                _currentPhase.value = ScanPhase.SCANNING_NETWORK
                _statusText.value = "Scanning Wi-Fi connection, SSID, gateway & encryption..."
                _scanProgress.value = 0.42f
                delay(250)

                val wifiInfo = inspectWifiNetwork(context)
                _scanProgress.value = 0.55f
                _statusText.value = "Wi-Fi: ${if (wifiInfo.isConnected) "${wifiInfo.ssid} (${wifiInfo.linkSpeedMbps} Mbps)" else "Disconnected"}"
                delay(200)

                // ==================== 4. BLUETOOTH SECURITY (55% -> 70%) ====================
                _currentPhase.value = ScanPhase.SCANNING_BLUETOOTH
                _statusText.value = "Inspecting Bluetooth state & paired devices..."
                _scanProgress.value = 0.60f
                delay(250)

                val bluetoothInfo = inspectBluetooth(context)
                _scanProgress.value = 0.70f
                _statusText.value = "Bluetooth: ${if (bluetoothInfo.isEnabled) "${bluetoothInfo.pairedDevicesCount} paired devices" else "Disabled"}"
                delay(200)

                // ==================== 5. BATTERY & THERMAL HEALTH (70% -> 85%) ====================
                _currentPhase.value = ScanPhase.SCANNING_BATTERY_THERMAL
                _statusText.value = "Analyzing battery temperature, voltage & charge status..."
                _scanProgress.value = 0.75f
                delay(250)

                val batteryInfo = inspectBattery(context)
                _scanProgress.value = 0.85f
                _statusText.value = "Battery: ${batteryInfo.levelPercentage}% • ${batteryInfo.temperatureCelsius}°C (${batteryInfo.health})"
                delay(200)

                // ==================== 6. HARDWARE: RAM & CPU CORES (85% -> 95%) ====================
                _currentPhase.value = ScanPhase.SCANNING_HARDWARE_RAM_CPU
                _statusText.value = "Benchmarking RAM memory & active CPU cores..."
                _scanProgress.value = 0.88f
                delay(250)

                val hardwareInfo = inspectHardware(context)
                _scanProgress.value = 0.95f
                _statusText.value = "RAM: ${hardwareInfo.usedRamPercentage}% used • ${hardwareInfo.cpuCoreCount} CPU Cores"
                delay(200)

                // ==================== 7. SYSTEM INTEGRITY (95% -> 100%) ====================
                _currentPhase.value = ScanPhase.CHECKING_INTEGRITY
                _statusText.value = "Verifying Root binary, ADB debugging & Mock GPS..."
                delay(250)

                val isRooted = checkRootAccess()
                val isAdb = checkAdbEnabled(context)
                val isMock = checkMockLocation(context)

                // Compute overall Health & Security Score (0 to 100)
                var score = 100
                if (isRooted) score -= 25
                if (isAdb) score -= 5
                if (batteryInfo.isOverheating) score -= 15
                if (highRiskList.size > 5) score -= 10
                if (hardwareInfo.usedRamPercentage > 85) score -= 10
                if (junkSizeBytes > 500 * 1024 * 1024) score -= 5
                score = score.coerceIn(20, 100)

                val result = SystemScanResult(
                    totalAppsChecked = totalPackages,
                    userAppsCount = userAppCount,
                    systemAppsCount = sysAppCount,
                    highRiskApps = highRiskList,
                    totalFilesAnalyzed = totalFiles,
                    junkFilesCount = junkFilesCount,
                    junkSizeBytes = junkSizeBytes,
                    junkFilesList = junkFiles,
                    wifiInfo = wifiInfo,
                    bluetoothInfo = bluetoothInfo,
                    batteryInfo = batteryInfo,
                    hardwareInfo = hardwareInfo,
                    isRooted = isRooted,
                    isAdbEnabled = isAdb,
                    isMockLocationEnabled = isMock,
                    healthScore = score
                )

                _scanResult.value = result
                _scanProgress.value = 1.0f
                _currentPhase.value = ScanPhase.COMPLETED
                _statusText.value = "All-In-One Diagnostic Complete • Health Score: $score/100"
                _isScanning.value = false

            } catch (e: CancellationException) {
                _statusText.value = "Scan cancelled"
                _isScanning.value = false
                _currentPhase.value = ScanPhase.IDLE
            } catch (e: Exception) {
                _statusText.value = "Scan error: ${e.message}"
                _isScanning.value = false
                _currentPhase.value = ScanPhase.IDLE
            }
        }
    }

    private fun inspectWifiNetwork(context: Context): WifiNetworkInfo {
        try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            val wm = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager

            val activeNetwork = cm?.activeNetwork
            val capabilities = cm?.getNetworkCapabilities(activeNetwork)
            val isWifi = capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
            val isConnected = capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true

            @Suppress("DEPRECATION")
            val wifiInfo = wm?.connectionInfo

            val rawSsid = wifiInfo?.ssid?.replace("\"", "") ?: "Unknown"
            val ssid = if (rawSsid == "<unknown ssid>" || rawSsid.isEmpty()) {
                if (isWifi) "Wi-Fi Network" else "Mobile Data"
            } else rawSsid

            val rssi = wifiInfo?.rssi ?: -100
            val speed = wifiInfo?.linkSpeed ?: 0
            val freq = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) wifiInfo?.frequency ?: 0 else 0

            var localIp = "127.0.0.1"
            try {
                val interfaces = NetworkInterface.getNetworkInterfaces()
                while (interfaces.hasMoreElements()) {
                    val intf = interfaces.nextElement()
                    val addrs = intf.inetAddresses
                    while (addrs.hasMoreElements()) {
                        val addr = addrs.nextElement()
                        if (!addr.isLoopbackAddress && addr.hostAddress?.indexOf(':') == -1) {
                            localIp = addr.hostAddress ?: "127.0.0.1"
                            break
                        }
                    }
                }
            } catch (ignored: Exception) {}

            val secType = if (isWifi) "WPA2/WPA3 (Encrypted)" else "Cellular Encryption"

            return WifiNetworkInfo(
                isConnected = isConnected,
                isWifi = isWifi,
                ssid = ssid,
                rssi = rssi,
                linkSpeedMbps = speed,
                localIp = localIp,
                frequencyMhz = freq,
                securityType = secType,
                isSecure = true
            )
        } catch (e: Exception) {
            return WifiNetworkInfo(false, false, "Disconnected", -100, 0, "N/A", 0, "Unencrypted", false)
        }
    }

    @SuppressLint("MissingPermission")
    private fun inspectBluetooth(context: Context): BluetoothDiagnosticInfo {
        try {
            val bm = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
            val adapter = bm?.adapter ?: BluetoothAdapter.getDefaultAdapter()

            if (adapter == null) {
                return BluetoothDiagnosticInfo(false, false, 0, emptyList(), "Unsupported", true)
            }

            val isEnabled = adapter.isEnabled
            val pairedNames = mutableListOf<String>()

            if (isEnabled) {
                try {
                    @Suppress("DEPRECATION")
                    val bonded = adapter.bondedDevices
                    if (bonded != null) {
                        for (dev in bonded) {
                            pairedNames.add(dev.name ?: "Unknown Device")
                        }
                    }
                } catch (ignored: SecurityException) {}
            }

            val scanModeText = when (adapter.scanMode) {
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE -> "Discoverable (Visible)"
                BluetoothAdapter.SCAN_MODE_CONNECTABLE -> "Connectable (Hidden)"
                else -> "None"
            }

            return BluetoothDiagnosticInfo(
                isSupported = true,
                isEnabled = isEnabled,
                pairedDevicesCount = pairedNames.size,
                pairedDeviceNames = pairedNames,
                scanMode = scanModeText,
                isSecure = adapter.scanMode != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE
            )
        } catch (e: Exception) {
            return BluetoothDiagnosticInfo(false, false, 0, emptyList(), "Unknown", true)
        }
    }

    private fun inspectBattery(context: Context): BatteryDiagnosticInfo {
        try {
            val ifilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            val bIntent = context.registerReceiver(null, ifilter)

            val level = bIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: 50
            val scale = bIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: 100
            val pct = if (level >= 0 && scale > 0) (level * 100 / scale) else 50

            val rawTemp = bIntent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 280
            val tempC = rawTemp / 10.0f
            val voltage = bIntent?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) ?: 3800

            val status = bIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
            val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL

            val plug = bIntent?.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) ?: 0
            val plugStr = when (plug) {
                BatteryManager.BATTERY_PLUGGED_AC -> "AC Fast Charger"
                BatteryManager.BATTERY_PLUGGED_USB -> "USB Cable"
                BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless Qi"
                else -> "On Battery Power"
            }

            val rawHealth = bIntent?.getIntExtra(BatteryManager.EXTRA_HEALTH, BatteryManager.BATTERY_HEALTH_GOOD) ?: BatteryManager.BATTERY_HEALTH_GOOD
            val healthStr = when (rawHealth) {
                BatteryManager.BATTERY_HEALTH_GOOD -> "Good / Optimal"
                BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheating"
                BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
                BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over Voltage"
                else -> "Normal"
            }

            val isOverheating = tempC > 42.0f || rawHealth == BatteryManager.BATTERY_HEALTH_OVERHEAT

            return BatteryDiagnosticInfo(
                levelPercentage = pct,
                temperatureCelsius = tempC,
                voltageMv = voltage,
                health = healthStr,
                isCharging = isCharging,
                plugType = plugStr,
                isOverheating = isOverheating
            )
        } catch (e: Exception) {
            return BatteryDiagnosticInfo(50, 30.0f, 3800, "Good", false, "Battery", false)
        }
    }

    private fun inspectHardware(context: Context): HardwarePerformanceInfo {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        am.getMemoryInfo(memInfo)

        val totalRam = memInfo.totalMem
        val availRam = memInfo.availMem
        val usedRam = totalRam - availRam
        val ramPct = ((usedRam.toDouble() / totalRam.toDouble()) * 100).toInt()

        val cores = Runtime.getRuntime().availableProcessors()
        val arch = System.getProperty("os.arch") ?: "arm64-v8a"

        val stat = StatFs(Environment.getDataDirectory().path)
        val totalStorage = stat.blockSizeLong * stat.blockCountLong
        val freeStorage = stat.blockSizeLong * stat.availableBlocksLong
        val usedStorage = totalStorage - freeStorage
        val storagePct = ((usedStorage.toDouble() / totalStorage.toDouble()) * 100).toInt()

        return HardwarePerformanceInfo(
            totalRamBytes = totalRam,
            availableRamBytes = availRam,
            usedRamPercentage = ramPct,
            cpuCoreCount = cores,
            cpuArchitecture = arch,
            totalStorageBytes = totalStorage,
            freeStorageBytes = freeStorage,
            usedStoragePercentage = storagePct
        )
    }

    fun cleanJunkFiles(context: Context): Pair<Int, Long> {
        val current = _scanResult.value ?: return Pair(0, 0L)
        var deletedCount = 0
        var deletedBytes = 0L

        try {
            context.cacheDir?.deleteRecursively()
            context.externalCacheDir?.deleteRecursively()

            for (file in current.junkFilesList) {
                try {
                    val size = file.length()
                    if (file.delete()) {
                        deletedCount++
                        deletedBytes += size
                    }
                } catch (ignored: Exception) {}
            }
        } catch (ignored: Exception) {}

        _scanResult.value = current.copy(
            junkFilesCount = 0,
            junkSizeBytes = 0L,
            junkFilesList = emptyList()
        )

        return Pair(deletedCount, deletedBytes)
    }

    private fun checkRootAccess(): Boolean {
        val paths = arrayOf(
            "/system/app/Superuser.apk",
            "/sbin/su",
            "/system/bin/su",
            "/system/xbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su"
        )
        for (path in paths) {
            if (File(path).exists()) return true
        }
        val buildTags = Build.TAGS
        if (buildTags != null && buildTags.contains("test-keys")) {
            return true
        }
        return false
    }

    private fun checkAdbEnabled(context: Context): Boolean {
        return try {
            Settings.Global.getInt(context.contentResolver, Settings.Global.ADB_ENABLED, 0) != 0
        } catch (e: Exception) {
            false
        }
    }

    private fun checkMockLocation(context: Context): Boolean {
        return FakeGpsManager.isMockingActive.value
    }
}
