package com.example.feature.system.wifi

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NetworkPing
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.feature.system.wifi.resolve.MulticastHelper
import com.example.feature.system.wifi.speedtest.SpeedTestSection
import com.example.ui.screens.SystemFeatureCard
import com.example.utils.SoundEffectManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WifiControllerCard(
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    viewModel: WifiControllerViewModel = viewModel()
) {
    val networkInfo by viewModel.networkInfo.collectAsStateWithLifecycle()
    val devices by viewModel.devices.collectAsStateWithLifecycle()
    val isScanning by viewModel.isScanning.collectAsStateWithLifecycle()
    val scanMode by viewModel.scanMode.collectAsStateWithLifecycle()
    val scanProgress by viewModel.scanProgress.collectAsStateWithLifecycle()
    val statusText by viewModel.statusText.collectAsStateWithLifecycle()
    val isStale by viewModel.isStale.collectAsStateWithLifecycle()
    val speedTestState by viewModel.speedTestState.collectAsStateWithLifecycle()

    val context = LocalContext.current
    var selectedDevice by remember { mutableStateOf<DeviceInfo?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var hasPermissions by remember { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        hasPermissions = results.values.all { it }
        if (hasPermissions) viewModel.refreshNetworkInfo()
    }

    LaunchedEffect(viewModel.snackbarMessage) {
        viewModel.snackbarMessage.collectLatest { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    LaunchedEffect(isExpanded) {
        if (isExpanded) {
            val perms = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.NEARBY_WIFI_DEVICES)
            } else {
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
            }
            val missing = perms.any { ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED }
            if (missing) {
                permissionLauncher.launch(perms)
            } else {
                hasPermissions = true
                viewModel.refreshNetworkInfo()
            }
        } else {
            viewModel.stopScan()
            MulticastHelper.releaseMdnsLock()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopScan()
            MulticastHelper.releaseMdnsLock()
        }
    }

    fun copyToClipboard(label: String, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText(label, text))
        coroutineScope.launch {
            snackbarHostState.showSnackbar("Copied $label: $text")
        }
    }

    SystemFeatureCard(
        title = "Wi-Fi Controller",
        subtitle = if (networkInfo.isConnected) "${networkInfo.ssid} | ${networkInfo.gateway} | Devices: ${devices.size} | Connected" else "Not connected to Wi-Fi",
        icon = if (networkInfo.isConnected) Icons.Default.Wifi else Icons.Default.WifiOff,
        badge = if (networkInfo.isConnected) "Connected" else "No Wi-Fi",
        badgeColor = if (networkInfo.isConnected) Color(0xFF10B981) else MaterialTheme.colorScheme.error,
        isExpanded = isExpanded,
        onToggleExpand = onToggleExpand
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (!networkInfo.isConnected) {
                Text(
                    text = "Please connect to a Wi-Fi network to use these features.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                if (networkInfo.ssid == "<unknown ssid>" || !hasPermissions) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                "Location must be ON and permissions granted to read SSID/BSSID and discover devices.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    context.startActivity(Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                                }
                            ) {
                                Text("Open Location Settings")
                            }
                        }
                    }
                }

                if (isStale) {
                    Surface(
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        Text(
                            text = "Stale • Tap Scan to refresh",
                            modifier = Modifier.padding(10.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }

                Text(
                    text = "Network Summary",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))

                Text(text = "SSID: ${networkInfo.ssid}", style = MaterialTheme.typography.bodySmall)
                Text(text = "BSSID: ${networkInfo.bssid}", style = MaterialTheme.typography.bodySmall)
                Text(text = "Local IP: ${networkInfo.localIp}", style = MaterialTheme.typography.bodySmall)
                Text(text = "Gateway: ${networkInfo.gateway}", style = MaterialTheme.typography.bodySmall)
                Text(text = "Subnet CIDR: /${networkInfo.subnetCidr}", style = MaterialTheme.typography.bodySmall)
                Text(text = "DNS Servers: ${networkInfo.dnsServers.joinToString().ifEmpty { "Default" }}", style = MaterialTheme.typography.bodySmall)
                Text(text = "Link Speed: ${networkInfo.linkSpeedMbps} Mbps", style = MaterialTheme.typography.bodySmall)

                Spacer(modifier = Modifier.height(16.dp))

                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        selected = scanMode == ScanMode.FAST,
                        onClick = { viewModel.setScanMode(ScanMode.FAST) },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                    ) {
                        Text("Fast Scan")
                    }
                    SegmentedButton(
                        selected = scanMode == ScanMode.DEEP,
                        onClick = { viewModel.setScanMode(ScanMode.DEEP) },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                    ) {
                        Text("Deep Scan")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            SoundEffectManager.playClick()
                            if (isScanning) viewModel.stopScan() else viewModel.startScan()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isScanning) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(if (isScanning) "Stop" else "Scan")
                    }

                    OutlinedButton(
                        onClick = {
                            SoundEffectManager.playClick()
                            val csvData = buildString {
                                appendLine("IP,MAC,Vendor,FriendlyName,Latency,OpenPorts")
                                devices.forEach {
                                    appendLine("${it.ip},${it.mac},${it.vendor},${it.friendlyName ?: "Unknown"},${it.latencyMs},${it.openPorts.joinToString("|")}")
                                }
                            }
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, csvData)
                                type = "text/csv"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Export Devices CSV"))
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Export CSV")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                SpeedTestSection(
                    speedTestState = speedTestState,
                    onStartTest = { viewModel.startSpeedTest() },
                    onCancelTest = { viewModel.cancelSpeedTest() }
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (isScanning) {
                    val progress = scanProgress
                    if (progress != null) {
                        LinearProgressIndicator(
                            progress = { progress.first.toFloat() / progress.second.toFloat() },
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }

                Text(
                    text = statusText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Discovered Devices (${devices.size})",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = "Streaming LAN scan",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                devices.forEach { device ->
                    var showItemMenu by remember { mutableStateOf(false) }

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { selectedDevice = device },
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = device.friendlyName ?: device.hostname ?: "${device.vendor} (${device.mac.takeLast(5)})",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = "${device.ip} • ${device.mac} • ${device.vendor}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Box {
                                    IconButton(onClick = { showItemMenu = true }) {
                                        Icon(
                                            Icons.Default.MoreVert,
                                            contentDescription = "Device actions",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    DropdownMenu(
                                        expanded = showItemMenu,
                                        onDismissRequest = { showItemMenu = false }
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("Copy IP") },
                                            leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp)) },
                                            onClick = {
                                                copyToClipboard("IP", device.ip)
                                                showItemMenu = false
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Copy MAC") },
                                            leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp)) },
                                            onClick = {
                                                copyToClipboard("MAC", device.mac)
                                                showItemMenu = false
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Ping") },
                                            leadingIcon = { Icon(Icons.Default.NetworkPing, contentDescription = null, modifier = Modifier.size(18.dp)) },
                                            onClick = {
                                                viewModel.pingDevice(device.ip)
                                                showItemMenu = false
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("View Details") },
                                            leadingIcon = { Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(18.dp)) },
                                            onClick = {
                                                selectedDevice = device
                                                showItemMenu = false
                                            }
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                device.latencyMs?.let { latency ->
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer
                                    ) {
                                        Text(
                                            text = "$latency ms",
                                            style = MaterialTheme.typography.labelSmall,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                if (device.openPorts.isNotEmpty()) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = MaterialTheme.colorScheme.tertiaryContainer
                                    ) {
                                        Text(
                                            text = "${device.openPorts.size} open ports",
                                            style = MaterialTheme.typography.labelSmall,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                device.nameSource?.let { source ->
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant
                                    ) {
                                        Text(
                                            text = source,
                                            style = MaterialTheme.typography.labelSmall,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))
    }

    selectedDevice?.let { device ->
        ModalBottomSheet(onDismissRequest = { selectedDevice = null }) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = device.friendlyName ?: device.hostname ?: "${device.vendor} Device",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(12.dp))

                Text(text = "IP Address: ${device.ip}", style = MaterialTheme.typography.bodyMedium)
                Text(text = "MAC Address: ${device.mac}", style = MaterialTheme.typography.bodyMedium)
                Text(text = "Vendor: ${device.vendor}", style = MaterialTheme.typography.bodyMedium)
                device.latencyMs?.let {
                    Text(text = "Latency: $it ms", style = MaterialTheme.typography.bodyMedium)
                }
                if (device.nameSource != null) {
                    Text(
                        text = "Name Source: ${device.nameSource}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { copyToClipboard("IP", device.ip) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Copy IP")
                    }
                    OutlinedButton(
                        onClick = { copyToClipboard("MAC", device.mac) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Copy MAC")
                    }
                    Button(
                        onClick = { viewModel.pingDevice(device.ip) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.NetworkPing, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Ping")
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
