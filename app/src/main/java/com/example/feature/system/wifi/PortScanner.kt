package com.example.feature.system.wifi

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import java.net.InetSocketAddress
import java.net.Socket

object PortScanner {
    val commonPorts = listOf(7, 9, 20, 21, 22, 23, 53, 80, 110, 139, 143, 443, 445, 554, 631, 853, 3389, 5353, 8080, 8443)

    suspend fun scanPorts(ip: String): List<Int> = withContext(Dispatchers.IO) {
        commonPorts.map { port ->
            async {
                try {
                    Socket().use { socket ->
                        socket.connect(InetSocketAddress(ip, port), 200)
                        port
                    }
                } catch (e: Exception) {
                    null
                }
            }
        }.awaitAll().filterNotNull()
    }
}
