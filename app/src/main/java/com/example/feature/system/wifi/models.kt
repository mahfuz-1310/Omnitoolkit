package com.example.feature.system.wifi

sealed interface DeviceEvent {
    data class OnHostFound(val ip: String, val mac: String?, val latencyMs: Long) : DeviceEvent
    data class OnNameResolved(val ip: String, val friendlyName: String, val source: String) : DeviceEvent
    data class OnProgress(val scanned: Int, val total: Int) : DeviceEvent
    data class OnDone(val summary: String) : DeviceEvent
}

enum class ScanMode {
    FAST, DEEP
}

data class NetworkInfo(
    val ssid: String,
    val bssid: String,
    val localIp: String,
    val gateway: String,
    val subnetCidr: Int,
    val dnsServers: List<String>,
    val linkSpeedMbps: Int,
    val isConnected: Boolean
)

data class DeviceInfo(
    val ip: String,
    val mac: String,
    val vendor: String,
    val friendlyName: String?,
    val hostname: String?,
    val nameSource: String?,
    val latencyMs: Long?,
    val openPorts: List<Int> = emptyList()
)

