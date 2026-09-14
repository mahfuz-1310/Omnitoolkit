package com.example.feature.system.wifi

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.feature.system.wifi.resolve.MulticastHelper
import com.example.feature.system.wifi.speedtest.SpeedTestManager
import com.example.feature.system.wifi.speedtest.SpeedTestPhase
import com.example.feature.system.wifi.speedtest.SpeedTestState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.net.InetAddress

class WifiControllerViewModel(application: Application) : AndroidViewModel(application) {
    private val context = application.applicationContext

    private val _networkInfo = MutableStateFlow(NetworkInfo("", "", "", "", 0, emptyList(), 0, false))
    val networkInfo = _networkInfo.asStateFlow()

    private val _devices = MutableStateFlow<List<DeviceInfo>>(emptyList())
    val devices = _devices.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning = _isScanning.asStateFlow()

    private val _scanMode = MutableStateFlow(ScanMode.FAST)
    val scanMode = _scanMode.asStateFlow()

    private val _scanProgress = MutableStateFlow<Pair<Int, Int>?>(null)
    val scanProgress = _scanProgress.asStateFlow()

    private val _statusText = MutableStateFlow("Tap Scan to discover devices")
    val statusText = _statusText.asStateFlow()

    private val _isStale = MutableStateFlow(false)
    val isStale = _isStale.asStateFlow()

    private val _snackbarMessage = MutableSharedFlow<String>()
    val snackbarMessage = _snackbarMessage.asSharedFlow()

    private val speedTestManager = SpeedTestManager()
    private val _speedTestState = MutableStateFlow(SpeedTestState())
    val speedTestState = _speedTestState.asStateFlow()

    private var scanJob: Job? = null
    private var speedTestJob: Job? = null

    // Simple in-memory cache by bssid + gateway
    private val cache = mutableMapOf<String, List<DeviceInfo>>()

    fun setScanMode(mode: ScanMode) {
        _scanMode.value = mode
    }

    fun refreshNetworkInfo() {
        val info = LanScanner.getNetworkInfo(context)
        _networkInfo.value = info
        val cacheKey = "${info.bssid}_${info.gateway}"
        if (cache.containsKey(cacheKey)) {
            _devices.value = cache[cacheKey]!!
            _isStale.value = true
            _statusText.value = "Stale results shown. Tap Scan to refresh."
        }
    }

    fun startScan() {
        if (_isScanning.value) return

        refreshNetworkInfo()
        if (!_networkInfo.value.isConnected) {
            viewModelScope.launch { _snackbarMessage.emit("Not connected to Wi-Fi") }
            return
        }

        _isScanning.value = true
        _devices.value = emptyList()
        _isStale.value = false
        _scanProgress.value = null
        _statusText.value = "Scanning network..."

        val mode = _scanMode.value
        scanJob = viewModelScope.launch {
            try {
                MulticastHelper.acquireMdnsLock(context)
                LanScanner.scanNetworkFlow(context, mode).collectLatest { event ->
                    when (event) {
                        is DeviceEvent.OnHostFound -> {
                            val vendor = LanScanner.getVendor(event.mac ?: "")
                            val newDevice = DeviceInfo(
                                ip = event.ip,
                                mac = event.mac ?: "Unknown",
                                vendor = vendor,
                                friendlyName = null,
                                hostname = null,
                                nameSource = null,
                                latencyMs = event.latencyMs,
                                openPorts = emptyList()
                            )
                            val current = _devices.value.toMutableList()
                            if (current.none { it.ip == event.ip }) {
                                current.add(newDevice)
                                _devices.value = current.sortedBy { ipToLong(it.ip) }
                                _statusText.value = "Found ${current.size} devices • Names resolving..."
                            }
                        }
                        is DeviceEvent.OnNameResolved -> {
                            val current = _devices.value.map { device ->
                                if (device.ip == event.ip) {
                                    device.copy(
                                        friendlyName = event.friendlyName,
                                        nameSource = event.source
                                    )
                                } else {
                                    device
                                }
                            }
                            _devices.value = current
                        }
                        is DeviceEvent.OnProgress -> {
                            _scanProgress.value = event.scanned to event.total
                        }
                        is DeviceEvent.OnDone -> {
                            _statusText.value = event.summary
                            _isScanning.value = false
                            MulticastHelper.releaseMdnsLock()
                            val info = _networkInfo.value
                            if (info.isConnected) {
                                cache["${info.bssid}_${info.gateway}"] = _devices.value
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                _statusText.value = "Scan error: ${e.localizedMessage}"
                _isScanning.value = false
                MulticastHelper.releaseMdnsLock()
            }
        }
    }

    fun stopScan() {
        scanJob?.cancel()
        _isScanning.value = false
        _scanProgress.value = null
        _statusText.value = "Scan stopped. Found ${_devices.value.size} devices."
        MulticastHelper.releaseMdnsLock()
    }

    private fun ipToLong(ip: String): Long {
        try {
            val parts = ip.split(".")
            if (parts.size == 4) {
                var result = 0L
                for (part in parts) {
                    result = result shl 8 or part.toLong()
                }
                return result
            }
        } catch (_: Exception) {}
        return 0L
    }

    fun startSpeedTest() {
        if (_speedTestState.value.phase in listOf(SpeedTestPhase.PING, SpeedTestPhase.DOWNLOAD, SpeedTestPhase.UPLOAD)) {
            return
        }
        speedTestJob?.cancel()
        _speedTestState.value = SpeedTestState(phase = SpeedTestPhase.PING, progress = 0.05f)

        speedTestJob = viewModelScope.launch {
            speedTestManager.runTest().collect { state ->
                _speedTestState.value = state
                if (state.phase == SpeedTestPhase.COMPLETED) {
                    _snackbarMessage.emit("Speed test complete: ↓${state.downloadMbps} Mbps  ↑${state.uploadMbps} Mbps")
                }
            }
        }
    }

    fun cancelSpeedTest() {
        speedTestJob?.cancel()
        _speedTestState.value = SpeedTestState(phase = SpeedTestPhase.IDLE)
    }

    fun pingDevice(ip: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val start = System.currentTimeMillis()
            try {
                val address = InetAddress.getByName(ip)
                val reachable = address.isReachable(1500)
                val elapsed = System.currentTimeMillis() - start
                if (reachable) {
                    _snackbarMessage.emit("Ping to $ip: ${elapsed}ms")
                } else {
                    _snackbarMessage.emit("Ping to $ip: unreachable (timeout)")
                }
            } catch (e: Exception) {
                _snackbarMessage.emit("Ping to $ip failed: ${e.message}")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopScan()
        cancelSpeedTest()
        MulticastHelper.releaseMdnsLock()
    }
}
