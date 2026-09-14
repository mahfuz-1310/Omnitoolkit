package com.example.feature.system.wifi.resolve

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.net.InetAddress

object ReverseDnsResolver {
    suspend fun resolve(ip: String): String? = withContext(Dispatchers.IO) {
        withTimeoutOrNull(600) {
            try {
                val address = InetAddress.getByName(ip)
                val hostName = address.canonicalHostName
                if (hostName != ip) hostName else null
            } catch (e: Exception) {
                null
            }
        }
    }
}
