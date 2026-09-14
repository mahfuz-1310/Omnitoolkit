package com.example.feature.system.wifi.resolve

import jcifs.context.SingletonContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object NetbiosResolver {
    suspend fun resolve(ip: String): String? = withContext(Dispatchers.IO) {
        try {
            val context = SingletonContext.getInstance()
            val nameServiceClient = context.nameServiceClient
            val nbtAddress = nameServiceClient.getNbtByName(ip)
            val addresses = nameServiceClient.getNodeStatus(nbtAddress)
            
            // Prefer nameType 0x00 non-group entries
            val best = addresses.firstOrNull { it.nameType == 0x00 && !it.isGroupAddress(context) }
                ?: addresses.firstOrNull()
                
            best?.name?.name?.trim()
        } catch (e: Exception) {
            null
        }
    }
}
