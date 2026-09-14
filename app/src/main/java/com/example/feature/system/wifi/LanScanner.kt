package com.example.feature.system.wifi

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import com.example.feature.system.wifi.resolve.MdnsResolver
import com.example.feature.system.wifi.resolve.MulticastHelper
import com.example.feature.system.wifi.resolve.NetbiosResolver
import com.example.feature.system.wifi.resolve.ReverseDnsResolver
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flowOn
import java.io.BufferedReader
import java.io.FileReader
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Semaphore
import java.util.concurrent.atomic.AtomicInteger

object LanScanner {
    private val ouiMap = mapOf(
        "00:03:93" to "Apple", "00:05:02" to "Apple", "00:0a:27" to "Apple", "00:10:FA" to "Apple",
        "00:11:24" to "Apple", "00:14:51" to "Apple", "00:16:CB" to "Apple", "00:17:F2" to "Apple",
        "40:6C:8F" to "Apple", "BC:92:6B" to "Apple", "F8:FF:C2" to "Apple", "DC:A9:04" to "Apple",
        "3C:06:30" to "Apple", "A4:83:E7" to "Apple", "AC:BC:32" to "Apple", "F0:18:98" to "Apple",
        "00:12:3F" to "Samsung", "00:15:99" to "Samsung", "00:16:32" to "Samsung", "00:17:C4" to "Samsung",
        "28:BA:B2" to "Samsung", "3C:5A:F4" to "Samsung", "8C:73:4B" to "Samsung", "50:01:D9" to "Samsung",
        "00:90:4C" to "TP-Link", "14:CF:92" to "TP-Link", "50:3F:2D" to "TP-Link", "60:E3:27" to "TP-Link",
        "B0:95:75" to "TP-Link", "EC:08:6B" to "TP-Link", "C0:06:C3" to "TP-Link", "00:1D:0F" to "TP-Link",
        "28:6C:07" to "Xiaomi", "34:80:B3" to "Xiaomi", "50:EC:50" to "Xiaomi", "64:09:80" to "Xiaomi",
        "7C:49:EB" to "Xiaomi", "D4:97:0B" to "Xiaomi", "F0:B4:29" to "Xiaomi", "84:F3:EB" to "Xiaomi",
        "00:1E:67" to "Intel", "00:27:10" to "Intel", "34:13:E8" to "Intel", "80:86:F2" to "Intel",
        "00:10:18" to "Broadcom", "00:1B:DC" to "Broadcom", "20:A6:8D" to "Broadcom",
        "00:50:56" to "VMware", "00:0C:29" to "VMware", "08:00:27" to "VirtualBox",
        "B8:27:EB" to "Raspberry Pi", "DC:A6:32" to "Raspberry Pi", "E4:5F:01" to "Raspberry Pi",
        "2C:CF:67" to "Espressif (ESP32/8266)", "30:AE:A4" to "Espressif", "24:6F:28" to "Espressif",
        "D8:A0:1D" to "Google", "F4:F5:DB" to "Google", "54:60:09" to "Google", "70:EE:50" to "Netgear",
        "00:24:B2" to "ASUS", "78:24:AF" to "ASUS", "04:D9:F5" to "ASUS", "30:5A:3A" to "ASUS",
        "00:18:E7" to "Huawei", "00:25:9E" to "Huawei", "00:46:4B" to "Huawei", "00:E0:FC" to "Huawei"
    )

    fun getNetworkInfo(context: Context): NetworkInfo {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
        val isConnected = networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true

        if (!isConnected) {
            return NetworkInfo("", "", "", "", 0, emptyList(), 0, false)
        }

        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val wifiInfo = wifiManager.connectionInfo
        val dhcpInfo = wifiManager.dhcpInfo

        val ssid = wifiInfo.ssid?.removeSurrounding("\"") ?: "Unknown SSID"
        val bssid = wifiInfo.bssid ?: "Unknown BSSID"
        val localIp = formatIpAddress(dhcpInfo.ipAddress)
        val gateway = formatIpAddress(dhcpInfo.gateway)
        val dns1 = formatIpAddress(dhcpInfo.dns1)
        val dns2 = formatIpAddress(dhcpInfo.dns2)
        val linkSpeed = wifiInfo.linkSpeed

        val cidr = if (dhcpInfo.netmask != 0) calculateCidr(dhcpInfo.netmask) else 24

        val dnsServers = mutableListOf<String>()
        if (dns1 != "0.0.0.0") dnsServers.add(dns1)
        if (dns2 != "0.0.0.0") dnsServers.add(dns2)

        return NetworkInfo(ssid, bssid, localIp, gateway, cidr, dnsServers, linkSpeed, true)
    }

    private fun formatIpAddress(ipAddress: Int): String {
        return "${ipAddress and 0xFF}.${ipAddress shr 8 and 0xFF}.${ipAddress shr 16 and 0xFF}.${ipAddress shr 24 and 0xFF}"
    }

    private fun calculateCidr(netmask: Int): Int {
        var cidr = 0
        var mask = netmask
        while (mask != 0) {
            cidr += mask and 1
            mask = mask ushr 1
        }
        return cidr
    }

    suspend fun getNetworkPrefix(context: Context): String {
        val info = getNetworkInfo(context)
        if (info.localIp.isBlank() || info.localIp == "0.0.0.0") return ""
        val parts = info.localIp.split(".")
        if (parts.size == 4) {
            return "${parts[0]}.${parts[1]}.${parts[2]}."
        }
        return ""
    }

    /**
     * Fully thread-safe streaming scan using channelFlow.
     * All concurrent child coroutines communicate solely through channelFlow's send/trySend.
     * Guaranteed cleanup and cancellation via awaitClose.
     */
    fun scanNetworkFlow(context: Context, scanMode: ScanMode): Flow<DeviceEvent> = channelFlow {
        val prefix = getNetworkPrefix(context)
        if (prefix.isBlank()) {
            send(DeviceEvent.OnDone("Not connected to Wi-Fi"))
            return@channelFlow
        }

        val networkInfo = getNetworkInfo(context)
        val gateway = networkInfo.gateway
        val localIp = networkInfo.localIp

        // Thread-safe deduplication set of discovered IP addresses
        val discoveredIps = ConcurrentHashMap.newKeySet<String>()
        val foundHosts = ConcurrentHashMap<String, Pair<String?, Long>>()

        // Stage 1: Read ARP table first and emit immediate entries
        val arpTable = readArpTable()
        arpTable.forEach { (ip, mac) ->
            if (ip.startsWith(prefix)) {
                if (discoveredIps.add(ip)) {
                    val cleanMac = mac.takeIf { it.isNotBlank() }
                    foundHosts[ip] = Pair(cleanMac, 4L)
                    send(DeviceEvent.OnHostFound(ip, cleanMac, 4L))
                }
            }
        }

        // Always check gateway if present and not yet added
        if (gateway.isNotBlank() && gateway != "0.0.0.0" && !discoveredIps.contains(gateway)) {
            val start = System.currentTimeMillis()
            if (isHostReachable(gateway, scanMode)) {
                val latency = (System.currentTimeMillis() - start).coerceAtLeast(1L)
                val mac = arpTable[gateway]
                if (discoveredIps.add(gateway)) {
                    foundHosts[gateway] = Pair(mac, latency)
                    send(DeviceEvent.OnHostFound(gateway, mac, latency))
                }
            }
        }

        // Also add local phone IP if not present
        if (localIp.isNotBlank() && localIp != "0.0.0.0" && discoveredIps.add(localIp)) {
            val mac = arpTable[localIp]
            foundHosts[localIp] = Pair(mac, 0L)
            send(DeviceEvent.OnHostFound(localIp, mac, 0L))
        }

        // Stage 2: Live host discovery sweep with concurrency limit
        val semaphore = Semaphore(64)
        val totalIps = 254
        val scannedCounter = AtomicInteger(0)

        supervisorScope {
            val probeJobs = (1..254).map { i ->
                val targetIp = "$prefix$i"
                launch(Dispatchers.IO) {
                    semaphore.acquire()
                    try {
                        if (!isActive) return@launch
                        val alreadyFound = discoveredIps.contains(targetIp)
                        if (!alreadyFound) {
                            val start = System.currentTimeMillis()
                            val reachable = isHostReachable(targetIp, scanMode)
                            val latency = (System.currentTimeMillis() - start).coerceAtLeast(1L)
                            if (reachable) {
                                val mac = arpTable[targetIp]
                                if (discoveredIps.add(targetIp)) {
                                    foundHosts[targetIp] = Pair(mac, latency)
                                    send(DeviceEvent.OnHostFound(targetIp, mac, latency))
                                }
                            }
                        }
                    } finally {
                        semaphore.release()
                        val current = scannedCounter.incrementAndGet()
                        if (current % 16 == 0 || current == totalIps) {
                            send(DeviceEvent.OnProgress(current, totalIps))
                        }
                    }
                }
            }
            probeJobs.joinAll()
        }

        send(DeviceEvent.OnProgress(totalIps, totalIps))

        // Stage 3: Non-blocking Name Resolution for discovered hosts
        try {
            val bindAddress = try {
                InetAddress.getByName(networkInfo.localIp)
            } catch (_: Exception) {
                null
            }

            val mdnsMap = if (bindAddress != null) {
                try {
                    MdnsResolver.resolveAll(context, bindAddress)
                } catch (e: Exception) {
                    emptyMap()
                }
            } else {
                emptyMap()
            }

            val netbiosSemaphore = Semaphore(16)
            supervisorScope {
                val resolveJobs = foundHosts.map { (ip, hostInfo) ->
                    val mac = hostInfo.first
                    launch(Dispatchers.IO) {
                        var resolvedName: String? = mdnsMap[ip]
                        var source = if (resolvedName != null) "mDNS" else null

                        if (resolvedName == null) {
                            netbiosSemaphore.acquire()
                            try {
                                resolvedName = NetbiosResolver.resolve(ip)
                                if (resolvedName != null) source = "NetBIOS"
                            } finally {
                                netbiosSemaphore.release()
                            }
                        }

                        if (resolvedName == null) {
                            resolvedName = ReverseDnsResolver.resolve(ip)
                            if (resolvedName != null) source = "Reverse DNS"
                        }

                        if (resolvedName == null) {
                            val vendor = getVendor(mac ?: "")
                            val macSuffix = if (!mac.isNullOrBlank()) {
                                mac.replace(":", "").replace("-", "").takeLast(4).uppercase()
                            } else {
                                ip.substringAfterLast('.')
                            }
                            resolvedName = if (vendor != "Generic" && vendor != "Unknown") {
                                "$vendor device • $macSuffix"
                            } else {
                                "LAN Device • $macSuffix"
                            }
                            source = "Fallback"
                        }

                        send(DeviceEvent.OnNameResolved(ip, resolvedName ?: "LAN Device", source ?: "Unknown"))
                    }
                }
                resolveJobs.joinAll()
            }
        } catch (e: Exception) {
            // Non-fatal, keep found hosts intact
        }

        val totalFound = discoveredIps.size
        val summaryText = if (totalFound <= 1 && gateway.isNotBlank()) {
            "Scan complete: $totalFound device found (Client isolation may be enabled on this router)"
        } else {
            "Scan complete: Found $totalFound devices."
        }
        send(DeviceEvent.OnDone(summaryText))

        awaitClose {
            // Flow cancelled or completed - child jobs inside supervisorScope terminate cleanly
        }
    }.flowOn(Dispatchers.IO)

    private fun isHostReachable(ip: String, scanMode: ScanMode = ScanMode.FAST): Boolean {
        // Fast ICMP ping probe
        try {
            val address = InetAddress.getByName(ip)
            if (address.isReachable(160)) return true
        } catch (_: Exception) {}

        // TCP socket probe on standard ports with short timeouts
        val portsToProbe = if (scanMode == ScanMode.DEEP) {
            listOf(53, 80, 443, 554, 8080, 8443, 22, 139, 445)
        } else {
            listOf(53, 80, 443, 554, 8080, 8443)
        }

        for (port in portsToProbe) {
            try {
                Socket().use { socket ->
                    socket.connect(InetSocketAddress(ip, port), 160)
                    return true
                }
            } catch (_: Exception) {}
        }
        return false
    }

    private fun readArpTable(): Map<String, String> {
        val arpMap = mutableMapOf<String, String>()
        try {
            BufferedReader(FileReader("/proc/net/arp")).use { reader ->
                reader.readLine() // skip header line
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    val tokens = line?.split(Regex("\\s+"))
                    if (tokens != null && tokens.size >= 4) {
                        val ip = tokens[0]
                        val mac = tokens[3]
                        if (mac != "00:00:00:00:00:00" && mac.length >= 11) {
                            arpMap[ip] = mac
                        }
                    }
                }
            }
        } catch (_: Exception) {}
        return arpMap
    }

    fun getVendor(mac: String): String {
        if (mac.isBlank()) return "Unknown"
        val clean = mac.replace("-", ":").uppercase()
        val prefix = clean.split(":").take(3).joinToString(":")
        return ouiMap[prefix] ?: "Generic"
    }
}
