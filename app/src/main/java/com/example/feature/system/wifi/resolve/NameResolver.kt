package com.example.feature.system.wifi.resolve

import android.content.Context
import com.example.feature.system.wifi.DeviceInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import java.net.InetAddress

object NameResolver {

    suspend fun resolve(
        context: Context,
        bindAddress: InetAddress,
        devices: List<DeviceInfo>
    ): Map<String, Pair<String, String>> = withContext(Dispatchers.IO) {
        // Pair<Name, Source>
        val finalMap = mutableMapOf<String, Pair<String, String>>()
        
        // 1. mDNS resolve all
        val mdnsResults = MdnsResolver.resolveAll(context, bindAddress)
        
        // 2. Map IPs to names concurrently for NetBIOS and DNS
        devices.map { device ->
            async {
                val ip = device.ip
                if (mdnsResults.containsKey(ip)) {
                    finalMap[ip] = mdnsResults[ip]!! to "mDNS"
                    return@async
                }
                
                val netbiosName = NetbiosResolver.resolve(ip)
                if (netbiosName != null) {
                    finalMap[ip] = netbiosName to "NetBIOS"
                    return@async
                }
                
                val dnsName = ReverseDnsResolver.resolve(ip)
                if (dnsName != null) {
                    finalMap[ip] = dnsName to "Reverse DNS"
                    return@async
                }
                
                val fallback = "${device.vendor} • ${device.mac.replace(":", "").takeLast(4)}"
                finalMap[ip] = fallback to "Fallback"
            }
        }.awaitAll()
        
        finalMap
    }
}
