package com.example.feature.system.wifi.resolve

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.InetAddress
import javax.jmdns.JmDNS

object MdnsResolver {
    private val serviceTypes = listOf(
        "_workstation._tcp.local.",
        "_smb._tcp.local.",
        "_googlecast._tcp.local.",
        "_ssh._tcp.local.",
        "_http._tcp.local.",
        "_airplay._tcp.local.",
        "_printer._tcp.local.",
        "_ipp._tcp.local.",
        "_hap._tcp.local.",
        "_services._dns-sd._udp.local."
    )

    suspend fun resolveAll(context: Context, bindAddress: InetAddress): Map<String, String> = withContext(Dispatchers.IO) {
        val results = mutableMapOf<String, String>()
        var jmdns: JmDNS? = null
        try {
            MulticastHelper.acquireMdnsLock(context)
            jmdns = JmDNS.create(bindAddress)
            
            for (type in serviceTypes) {
                val infos = jmdns.list(type, 1200)
                for (info in infos) {
                    val addresses = info.inetAddresses
                    for (addr in addresses) {
                        results[addr.hostAddress] = info.name
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                jmdns?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            MulticastHelper.releaseMdnsLock()
        }
        results
    }
}
