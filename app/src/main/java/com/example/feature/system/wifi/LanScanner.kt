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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.BufferedReader
import java.io.FileReader
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import java.util.concurrent.Semaphore

object LanScanner {
    private val ouiMap = mapOf(
        "00:03:93" to "Apple", "00:05:02" to "Apple", "00:0a:27" to "Apple", "00:10:fa" to "Apple",
        "00:11:24" to "Apple", "00:14:51" to "Apple", "00:16:cb" to "Apple", "00:17:f2" to "Apple",
        "40:6c:8f" to "Apple", "bc:92:6b" to "Apple", "f8:ff:c2" to "Apple",
        "00:12:3f" to "Samsung", "00:15:99" to "Samsung", "00:16:32" to "Samsung", "00:17:c4" to "Samsung",
        "28:ba:b2" to "Samsung", "3c:5a:f4" to "Samsung", "8c:73:4b" to "Samsung",
        "00:90:4c" to "TP-Link", "14:cf:92" to "TP-Link", "50:3f:2d" to "TP-Link", "60:e3:27" to "TP-Link",
        "28:6c:07" to "Xiaomi", "34:80:b3" to "Xiaomi", "50:ec:50" to "Xiaomi", "64:09:80" to "Xiaomi",
        "00:1e:67" to "Intel", "00:27:10" to "Intel", "34:13:e8" to "Intel", "80:86:f2" to "Intel",
        "00:10:18" to "Broadcom", "00:1b:dc" to "Broadcom", "20:a6:8d" to "Broadcom"
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

    fun scanNetworkFlow(context: Context, scanMode: ScanMode): Flow<DeviceEvent> = flow {
        val prefix = getNetworkPrefix(context)
        if (prefix.isBlank()) {
            emit(DeviceEvent.OnDone("Not connected to Wi-Fi"))
            return@flow
        }

        val networkInfo = getNetworkInfo(context)
        val gateway = networkInfo.gateway

        // Stage 1: Seed ARP table and gateway
        val arpTable = readArpTable()
        val foundHosts = mutableListOf<Triple<String, String?, Long>>()

        // Always check gateway first if present
        if (gateway.isNotBlank() && gateway != "0.0.0.0") {
            val start = System.currentTimeMillis()
            if (isHostReachable(gateway)) {
                val latency = System.currentTimeMillis() - start
                val mac = arpTable[gateway] ?: ""
                foundHosts.add(Triple(gateway, mac, latency))
                emit(DeviceEvent.OnHostFound(gateway, mac.takeIf { it.isNotBlank() }, latency))
            }
        }

        // Add any pre-cached ARP entries on this subnet
        arpTable.forEach { (ip, mac) ->
            if (ip.startsWith(prefix) && ip != gateway && foundHosts.none { it.first == ip }) {
                val start = System.currentTimeMillis()
                if (isHostReachable(ip)) {
                    val latency = System.currentTimeMillis() - start
                    foundHosts.add(Triple(ip, mac, latency))
                    emit(DeviceEvent.OnHostFound(ip, mac.takeIf { it.isNotBlank() }, latency))
                }
            }
        }

        // Stage 2: Live host discovery (Fast sweep)
        val semaphore = Semaphore(64)
        val totalIps = 254
        var scannedCount = 0

        val jobs = (1..254).map { i ->
            val ip = "$prefix$i"
            if (ip == gateway || foundHosts.any { it.first == ip }) {
                scannedCount++
                null
            } else {
                coroutineScope {
                    async(Dispatchers.IO) {
                        semaphore.acquire()
                        try {
                            if (!currentCoroutineContext().isActive) return@async null
                            val start = System.currentTimeMillis()
                            val reachable = isHostReachable(ip)
                            val latency = System.currentTimeMillis() - start
                            if (reachable) {
                                val mac = arpTable[ip] ?: ""
                                Triple(ip, mac, latency)
                            } else {
                                null
                            }
                        } finally {
                            semaphore.release()
                            scannedCount++
                            if (scannedCount % 16 == 0 || scannedCount == totalIps) {
                                emit(DeviceEvent.OnProgress(scannedCount, totalIps))
                            }
                        }
                    }
                }
            }
        }.filterNotNull()

        val results = jobs.awaitAll().filterNotNull()
        results.forEach { (ip, mac, latency) ->
            if (foundHosts.none { it.first == ip }) {
                foundHosts.add(Triple(ip, mac, latency))
                emit(DeviceEvent.OnHostFound(ip, mac.takeIf { it.isNotBlank() }, latency))
            }
        }

        emit(DeviceEvent.OnProgress(totalIps, totalIps))

        // Stage 3: Name resolution (non-blocking) for found hosts
        try {
            val bindAddress = InetAddress.getByName(networkInfo.localIp)
            val mdnsMap = try {
                MdnsResolver.resolveAll(context, bindAddress)
            } catch (e: Exception) {
                emptyMap<String, String>()
            }

            val netbiosSemaphore = Semaphore(16)
            supervisorScope {
                foundHosts.map { (ip, mac, _) ->
                    async(Dispatchers.IO) {
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
                            val macSuffix = if (!mac.isNullOrBlank()) mac.replace(":", "").takeLast(4) else ip.substringAfterLast('.')
                            resolvedName = "$vendor • $macSuffix"
                            source = "Fallback"
                        }

                        emit(DeviceEvent.OnNameResolved(ip, resolvedName!!, source ?: "Unknown"))
                    }
                }.awaitAll()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        emit(DeviceEvent.OnDone("Scan complete. Found ${foundHosts.size} devices."))
    }.flowOn(Dispatchers.IO)

    private fun isHostReachable(ip: String): Boolean {
        try {
            val address = InetAddress.getByName(ip)
            if (address.isReachable(150)) return true
        } catch (e: Exception) {}

        val portsToProbe = listOf(53, 80, 443)
        for (port in portsToProbe) {
            try {
                Socket().use { socket ->
                    socket.connect(InetSocketAddress(ip, port), 160)
                    return true
                }
            } catch (e: Exception) {}
        }
        return false
    }

    private fun readArpTable(): Map<String, String> {
        val arpMap = mutableMapOf<String, String>()
        try {
            BufferedReader(FileReader("/proc/net/arp")).use { reader ->
                reader.readLine() // skip header
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    val tokens = line?.split(Regex("\\s+"))
                    if (tokens != null && tokens.size >= 4) {
                        val ip = tokens[0]
                        val mac = tokens[3]
                        if (mac != "00:00:00:00:00:00") {
                            arpMap[ip] = mac
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return arpMap
    }

    fun getVendor(mac: String): String {
        if (mac.isBlank()) return "Unknown"
        val clean = mac.replace("-", ":").uppercase()
        val prefix = clean.split(":").take(3).joinToString(":")
        return ouiMap[prefix] ?: "Generic"
    }
}
