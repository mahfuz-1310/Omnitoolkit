package com.example.utils

import android.content.Context
import android.content.SharedPreferences

data class DnsProvider(
    val id: String,
    val name: String,
    val category: String,
    val primaryIp: String,
    val secondaryIp: String,
    val dotHost: String,
    val description: String
)

object DnsManager {
    private const val PREF_NAME = "dns_manager_prefs"
    private const val KEY_SELECTED_ID = "selected_dns_id"
    private const val KEY_SELECTED_NAME = "selected_dns_name"
    private const val KEY_SELECTED_HOST = "selected_dns_host"
    private const val KEY_SELECTED_PRIMARY = "selected_dns_primary"
    private const val KEY_SELECTED_SECONDARY = "selected_dns_secondary"

    val providers = listOf(
        // Speed & Performance
        DnsProvider("cloudflare", "Cloudflare", "Speed & Performance", "1.1.1.1", "1.0.0.1", "one.one.one.one", "Fastest general DNS with built-in privacy protection."),
        DnsProvider("google", "Google", "Speed & Performance", "8.8.8.8", "8.8.4.4", "dns.google", "Highly reliable, secure, and globally distributed DNS."),
        DnsProvider("alidns", "AliDNS", "Speed & Performance", "223.5.5.5", "223.6.6.6", "dns.alidns.com", "Alibaba's public DNS for speed and reliability."),
        DnsProvider("baidu", "Baidu", "Speed & Performance", "180.76.76.76", "", "dns.baidu.com", "Baidu's fast public DNS service."),

        // Ad & Tracker Blockers
        DnsProvider("adguard", "AdGuard", "Ad & Tracker Blockers", "94.140.14.14", "94.140.15.15", "dns.adguard-dns.com", "Blocks ads, trackers, and phishing domains dynamically."),
        DnsProvider("mullvad", "Mullvad", "Ad & Tracker Blockers", "194.242.2.2", "194.242.2.3", "adblock.dns.mullvad.net", "Privacy-focused tracker blocking from Mullvad VPN."),
        DnsProvider("controld", "Control D", "Ad & Tracker Blockers", "76.76.2.0", "76.76.10.0", "p2.freedns.controld.com", "Advanced filtering engine blocking ads and common trackers."),
        DnsProvider("alternate_dns", "Alternate DNS", "Ad & Tracker Blockers", "76.76.19.19", "76.223.122.150", "dns.alternate-dns.com", "Blocks ads and malicious websites globally."),
        DnsProvider("rethinkdns", "RethinkDNS", "Ad & Tracker Blockers", "", "", "max.rethinkdns.com", "Privacy-first DNS that blocks malware and ads."),

        // Security & Malware Protection
        DnsProvider("quad9", "Quad9", "Security & Malware Protection", "9.9.9.9", "149.112.112.112", "dns.quad9.net", "Blocks known malicious domains and phishing sites in real-time."),
        DnsProvider("comodo", "Comodo", "Security & Malware Protection", "8.26.56.26", "8.20.247.20", "dns.comodo.com", "Focuses on threat protection and security."),
        DnsProvider("opendns", "OpenDNS", "Security & Malware Protection", "208.67.222.222", "208.67.220.220", "dns.opendns.com", "Enterprise-grade web protection and threat filtering."),
        DnsProvider("safesurfer", "SafeSurfer", "Security & Malware Protection", "104.197.28.121", "104.155.201.52", "dns.safesurfer.io", "Cyber-security focused protection."),

        // Family Safety & SafeSearch
        DnsProvider("adguard_family", "AdGuard Family", "Family Safety & SafeSearch", "94.140.14.15", "94.140.15.16", "family.adguard-dns.com", "Blocks adult content and enforces safe search."),
        DnsProvider("cleanbrowsing_family", "CleanBrowsing Family", "Family Safety & SafeSearch", "185.228.168.168", "185.228.169.168", "family-filter-dns.cleanbrowsing.org", "Blocks proxy networks, adult sites, and malicious domains."),
        DnsProvider("opendns_family", "OpenDNS FamilyShield", "Family Safety & SafeSearch", "208.67.222.123", "208.67.220.123", "familyshield.opendns.com", "Blocks adult content out of the box."),
        DnsProvider("cloudflare_family", "Cloudflare Family", "Family Safety & SafeSearch", "1.1.1.3", "1.0.0.3", "family.cloudflare-dns.com", "Blocks malware and adult content."),

        // Privacy & Uncensored
        DnsProvider("blahdns_jp", "BlahDNS Japan", "Privacy & Uncensored", "", "", "dot-jp.blahdns.com", "Small hobby uncensored DNS (Japan)."),
        DnsProvider("blahdns_de", "BlahDNS Germany", "Privacy & Uncensored", "", "", "dot-de.blahdns.com", "Small hobby uncensored DNS (Germany)."),
        DnsProvider("dns_watch", "DNS.WATCH", "Privacy & Uncensored", "84.200.69.80", "84.200.70.40", "resolver2.dns.watch", "Fast and uncensored internet without logging."),
        DnsProvider("njalla", "Njal.la", "Privacy & Uncensored", "95.215.19.53", "81.95.15.59", "dns.njal.la", "Privacy-first DNS provider."),
        DnsProvider("cira", "CIRA Canadian Shield", "Privacy & Uncensored", "149.112.121.20", "149.112.122.20", "shield.cira.ca", "Private DNS operated by CIRA in Canada."),

        // Custom Hostname
        DnsProvider("custom", "Custom Hostname", "Custom Hostname", "", "", "", "Manually enter a custom DoT/DoH hostname.")
    )

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun saveSelectedDns(context: Context, provider: DnsProvider, customHost: String = "") {
        getPrefs(context).edit().apply {
            putString(KEY_SELECTED_ID, provider.id)
            putString(KEY_SELECTED_NAME, provider.name)
            putString(KEY_SELECTED_HOST, if (provider.id == "custom") customHost else provider.dotHost)
            putString(KEY_SELECTED_PRIMARY, provider.primaryIp)
            putString(KEY_SELECTED_SECONDARY, provider.secondaryIp)
            apply()
        }
    }

    fun getSelectedDns(context: Context): DnsProvider {
        val prefs = getPrefs(context)
        val id = prefs.getString(KEY_SELECTED_ID, "cloudflare") ?: "cloudflare"
        val customHost = prefs.getString(KEY_SELECTED_HOST, "") ?: ""
        val provider = providers.find { it.id == id } ?: providers[0]
        if (provider.id == "custom") {
            return provider.copy(dotHost = customHost)
        }
        return provider
    }
}
