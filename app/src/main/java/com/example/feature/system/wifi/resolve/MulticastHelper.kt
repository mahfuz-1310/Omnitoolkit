package com.example.feature.system.wifi.resolve

import android.content.Context
import android.net.wifi.WifiManager

object MulticastHelper {
    private var multicastLock: WifiManager.MulticastLock? = null

    fun acquireMdnsLock(context: Context) {
        if (multicastLock?.isHeld == true) return
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        multicastLock = wifiManager.createMulticastLock("MdnsResolverLock").apply {
            setReferenceCounted(true)
            acquire()
        }
    }

    fun releaseMdnsLock() {
        if (multicastLock?.isHeld == true) {
            multicastLock?.release()
        }
        multicastLock = null
    }
}
