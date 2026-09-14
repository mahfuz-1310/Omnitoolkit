package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import android.os.SystemClock
import android.util.Log
import com.example.ui.MainViewModel
import com.example.ui.navigation.Screen
import com.example.utils.HighRiskApp
import com.example.utils.SoundEffectManager
import com.example.utils.SystemScanResult
import com.example.utils.SystemScannerManager

data class CompactFeature(
    val title: String,
    val icon: ImageVector,
    val route: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    navController: NavController
) {
    val context = LocalContext.current
    val resourceId = remember { context.resources.getIdentifier("namegen_premium_logo_1787339369777", "drawable", context.packageName) }
    val smartSuggestions by viewModel.smartSuggestions.collectAsStateWithLifecycle()

    // Deep All-In-One Scanner states
    val isScanning by SystemScannerManager.isScanning.collectAsStateWithLifecycle()
    val scanProgress by SystemScannerManager.scanProgress.collectAsStateWithLifecycle()
    val scanStatusText by SystemScannerManager.statusText.collectAsStateWithLifecycle()
    val currentPhase by SystemScannerManager.currentPhase.collectAsStateWithLifecycle()
    val scanResult by SystemScannerManager.scanResult.collectAsStateWithLifecycle()

    var showReportDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val suggestedFeatures = remember(smartSuggestions) {
        val features = mutableListOf<CompactFeature>()
        smartSuggestions.forEach { suggestion ->
            when (suggestion) {
                "Smart Assistant" -> features.add(CompactFeature("Assistant", Icons.Default.AutoAwesome, Screen.SmartAssistant.route))
                "App Manager & Backup" -> features.add(CompactFeature("App Manager", Icons.Default.Apps, Screen.AppList.route))
                "Password Generator" -> features.add(CompactFeature("Passwords", Icons.Default.Password, Screen.PasswordGen.route))
                "System Diagnostics" -> features.add(CompactFeature("System Stats", Icons.Default.Dashboard, Screen.SystemDashboard.route))
                "DNS & Network" -> features.add(CompactFeature("DNS Changer", Icons.Default.VpnKey, Screen.DnsChanger.route))
                "Animation Settings" -> features.add(CompactFeature("Animations", Icons.Default.Animation, Screen.AnimationSettings.route))
            }
        }
        
        val allFeatures = listOf(
            CompactFeature("Security Scan", Icons.Default.Security, Screen.DeepScanner.route),
            CompactFeature("Fake GPS", Icons.Default.LocationOn, Screen.FakeGps.route),
            CompactFeature("App Manager", Icons.Default.Apps, Screen.AppList.route),
            CompactFeature("System Stats", Icons.Default.Dashboard, Screen.SystemDashboard.route),
            CompactFeature("DNS Changer", Icons.Default.VpnKey, Screen.DnsChanger.route),
            CompactFeature("Animations", Icons.Default.Animation, Screen.AnimationSettings.route),
            CompactFeature("Calculator", Icons.Default.Calculate, Screen.Calculator.route),
            CompactFeature("Coin Toss", Icons.Default.Casino, Screen.CoinToss.route),
            CompactFeature("Passwords", Icons.Default.Password, Screen.PasswordGen.route),
            CompactFeature("Text Saver", Icons.Default.Save, Screen.TextSaver.route)
        )
        
        allFeatures.forEach { feature ->
            if (features.none { it.route == feature.route }) {
                features.add(feature)
            }
        }
        features.take(6)
    }

    // Dynamic icon representing the current scan phase
    val phaseIcon = when (currentPhase) {
        SystemScannerManager.ScanPhase.SCANNING_APPS -> Icons.Default.Apps
        SystemScannerManager.ScanPhase.SCANNING_FILES -> Icons.Default.FolderOpen
        SystemScannerManager.ScanPhase.SCANNING_NETWORK -> Icons.Default.Wifi
        SystemScannerManager.ScanPhase.SCANNING_BLUETOOTH -> Icons.Default.Bluetooth
        SystemScannerManager.ScanPhase.SCANNING_BATTERY_THERMAL -> Icons.Default.BatteryChargingFull
        SystemScannerManager.ScanPhase.SCANNING_HARDWARE_RAM_CPU -> Icons.Default.Memory
        SystemScannerManager.ScanPhase.CHECKING_INTEGRITY -> Icons.Default.Shield
        SystemScannerManager.ScanPhase.COMPLETED -> Icons.Default.Verified
        else -> Icons.Default.Security
    }

    val t0 = remember { SystemClock.uptimeMillis() }
    SideEffect {
        Log.d("Tabs", "HomeScreen switched in ${SystemClock.uptimeMillis() - t0}ms")
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(Unit) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.onHomeVisible()
        }
    }

    val listState = rememberSaveable(saver = LazyListState.Saver) { LazyListState() }

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "System Diagnostics & Toolkit",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = com.example.utils.AppConstants.APP_NAME,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                if (resourceId != 0) {
                    androidx.compose.foundation.Image(
                        painter = painterResource(id = resourceId),
                        contentDescription = "App Logo",
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Deep hardware diagnostics, Wi-Fi security, battery thermals, junk cleaner & system utilities.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // ==================== SECURITY SCAN ====================
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .clickable {
                        SoundEffectManager.playClick()
                        navController.navigate(Screen.DeepScanner.route)
                    },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "System Security Scan",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Tap to run a deep security scan",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.ArrowForwardIos,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // ==================== FEATURED BANNER ====================
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .clickable {
                        SoundEffectManager.playClick()
                        navController.navigate(Screen.SmartAssistant.route)
                    },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primaryContainer,
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                                )
                            )
                        )
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Smart Assistant",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Describe what you want, e.g. \"Generate a secure Wi-Fi password with phonetic keys\"",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text(
                                text = "Try it now",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        // ==================== SEARCH & SMART TOOLS GRID ====================
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search tools, diagnostics & generators...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Quick Tools & Utilities",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "All Tools",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable {
                        SoundEffectManager.playClick()
                        navController.navigate(Screen.GenerateDashboard.route)
                    }
                )
            }
        }

        val filteredFeatures = suggestedFeatures.filter { it.title.contains(searchQuery, ignoreCase = true) }
        val featurePairs = filteredFeatures.chunked(2)

        items(featurePairs) { pair ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                pair.forEach { feature ->
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(18.dp))
                            .clickable {
                                SoundEffectManager.playClick()
                                navController.navigate(feature.route)
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.Start
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = feature.icon,
                                    contentDescription = feature.title,
                                    modifier = Modifier.size(22.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = feature.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
                if (pair.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    // ==================== DETAILED HARDWARE & SYSTEM REPORT DIALOG ====================
    if (showReportDialog && scanResult != null) {
        val result = scanResult!!
        var showHighRiskList by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showReportDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Assessment,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(30.dp)
                )
            },
            title = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("All-In-One Diagnostic Report", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Device Health Score: ${result.healthScore}/100",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (result.healthScore >= 80) Color(0xFF10B981) else Color(0xFFF59E0B),
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 480.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 1. Wi-Fi & Network
                    item {
                        ReportSectionCard(
                            title = "Wi-Fi & Network Security",
                            icon = Icons.Default.Wifi,
                            statusText = if (result.wifiInfo.isConnected) "Connected • ${result.wifiInfo.securityType}" else "Disconnected",
                            isPass = result.wifiInfo.isConnected,
                            actionLabel = "Wi-Fi Settings",
                            onAction = {
                                context.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
                            }
                        ) {
                            Text("• SSID: ${result.wifiInfo.ssid}", style = MaterialTheme.typography.bodySmall)
                            Text("• Signal Strength (RSSI): ${result.wifiInfo.rssi} dBm", style = MaterialTheme.typography.bodySmall)
                            Text("• Link Speed: ${result.wifiInfo.linkSpeedMbps} Mbps", style = MaterialTheme.typography.bodySmall)
                            Text("• Local IP: ${result.wifiInfo.localIp}", style = MaterialTheme.typography.bodySmall)
                        }
                    }

                    // 2. Bluetooth Security
                    item {
                        ReportSectionCard(
                            title = "Bluetooth Security",
                            icon = Icons.Default.Bluetooth,
                            statusText = if (result.bluetoothInfo.isEnabled) "${result.bluetoothInfo.pairedDevicesCount} Paired Devices (${result.bluetoothInfo.scanMode})" else "Bluetooth Disabled",
                            isPass = result.bluetoothInfo.isSecure,
                            actionLabel = "Bluetooth",
                            onAction = {
                                context.startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS))
                            }
                        ) {
                            Text("• Adapter State: ${if (result.bluetoothInfo.isEnabled) "Active" else "Disabled"}", style = MaterialTheme.typography.bodySmall)
                            Text("• Visibility Mode: ${result.bluetoothInfo.scanMode}", style = MaterialTheme.typography.bodySmall)
                            if (result.bluetoothInfo.pairedDeviceNames.isNotEmpty()) {
                                Text("• Paired: ${result.bluetoothInfo.pairedDeviceNames.take(3).joinToString(", ")}", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }

                    // 3. Battery & Thermal Health
                    item {
                        ReportSectionCard(
                            title = "Battery & Thermal Health",
                            icon = Icons.Default.BatteryChargingFull,
                            statusText = "${result.batteryInfo.levelPercentage}% • ${result.batteryInfo.temperatureCelsius}°C • ${result.batteryInfo.health}",
                            isPass = !result.batteryInfo.isOverheating,
                            actionLabel = "Battery Settings",
                            onAction = {
                                context.startActivity(Intent(Intent.ACTION_POWER_USAGE_SUMMARY))
                            }
                        ) {
                            Text("• Temperature: ${result.batteryInfo.temperatureCelsius}°C (${if (result.batteryInfo.isOverheating) "Warning: High Temp" else "Optimal Range"})", style = MaterialTheme.typography.bodySmall)
                            Text("• Voltage: ${result.batteryInfo.voltageMv} mV", style = MaterialTheme.typography.bodySmall)
                            Text("• Power Source: ${result.batteryInfo.plugType}", style = MaterialTheme.typography.bodySmall)
                            Text("• Health Condition: ${result.batteryInfo.health}", style = MaterialTheme.typography.bodySmall)
                        }
                    }

                    // 4. Hardware: RAM & CPU Performance
                    item {
                        ReportSectionCard(
                            title = "RAM Memory & Processor",
                            icon = Icons.Default.Memory,
                            statusText = "${result.hardwareInfo.usedRamPercentage}% RAM used • ${result.hardwareInfo.cpuCoreCount} Cores (${result.hardwareInfo.cpuArchitecture})",
                            isPass = result.hardwareInfo.usedRamPercentage < 85
                        ) {
                            Text("• Total RAM: ${result.hardwareInfo.formatBytes(result.hardwareInfo.totalRamBytes)}", style = MaterialTheme.typography.bodySmall)
                            Text("• Free RAM: ${result.hardwareInfo.formatBytes(result.hardwareInfo.availableRamBytes)}", style = MaterialTheme.typography.bodySmall)
                            Text("• Storage: ${result.hardwareInfo.usedStoragePercentage}% used (${result.hardwareInfo.formatBytes(result.hardwareInfo.freeStorageBytes)} free)", style = MaterialTheme.typography.bodySmall)
                        }
                    }

                    // 5. Installed Apps & Permissions
                    item {
                        ReportSectionCard(
                            title = "Installed Apps & Risk Analysis",
                            icon = Icons.Default.Apps,
                            statusText = "${result.totalAppsChecked} Apps (${result.highRiskApps.size} with high-risk permissions)",
                            isPass = result.highRiskApps.size <= 5,
                            actionLabel = if (result.highRiskApps.isNotEmpty()) (if (showHighRiskList) "Hide Details" else "View Risky Apps") else null,
                            onAction = { showHighRiskList = !showHighRiskList }
                        ) {
                            Text("• User Apps: ${result.userAppsCount} | System Apps: ${result.systemAppsCount}", style = MaterialTheme.typography.bodySmall)
                            Text("• High Risk Apps: ${result.highRiskApps.size}", style = MaterialTheme.typography.bodySmall)

                            if (showHighRiskList && result.highRiskApps.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                                Spacer(modifier = Modifier.height(4.dp))
                                result.highRiskApps.take(8).forEach { app ->
                                    Text("⚠️ ${app.appName}: ${app.sensitivePermissions.size} permissions", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }

                    // 6. Temporary Files & Junk Cache
                    item {
                        ReportSectionCard(
                            title = "Files & Storage Junk",
                            icon = Icons.Default.DeleteOutline,
                            statusText = "${result.totalFilesAnalyzed} files checked • ${result.junkFilesCount} junk items (${result.formattedJunkSize})",
                            isPass = result.junkFilesCount == 0,
                            actionLabel = if (result.junkFilesCount > 0) "Clean Junk" else null,
                            onAction = {
                                SoundEffectManager.playClick()
                                val (deletedCount, deletedBytes) = SystemScannerManager.cleanJunkFiles(context)
                                Toast.makeText(context, "Cleaned $deletedCount temporary junk files!", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Text("• Temporary / Log / Cache: ${result.junkFilesCount} files", style = MaterialTheme.typography.bodySmall)
                            Text("• Space Reclaimable: ${result.formattedJunkSize}", style = MaterialTheme.typography.bodySmall)
                        }
                    }

                    // 7. System Integrity (Root, ADB, Mock GPS)
                    item {
                        ReportSectionCard(
                            title = "System Integrity & Debugging",
                            icon = Icons.Default.Shield,
                            statusText = if (!result.isRooted && !result.isAdbEnabled) "System Secure • No Root Detected" else "Developer/Root Flags Detected",
                            isPass = !result.isRooted,
                            actionLabel = "Dev Options",
                            onAction = {
                                context.startActivity(Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS))
                            }
                        ) {
                            Text("• Root Binary (Su): ${if (result.isRooted) "Root Found ⚠️" else "Safe / Clean ✅"}", style = MaterialTheme.typography.bodySmall)
                            Text("• ADB Debugging: ${if (result.isAdbEnabled) "Enabled (Dev Mode)" else "Disabled ✅"}", style = MaterialTheme.typography.bodySmall)
                            Text("• Mock GPS Provider: ${if (result.isMockLocationEnabled) "Active" else "Inactive"}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showReportDialog = false }) {
                    Text("Close Diagnostic")
                }
            }
        )
    }
}

@Composable
private fun DiagnosticMiniChip(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isPass: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(68.dp)
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(if (isPass) Color(0xFF10B981).copy(alpha = 0.15f) else Color(0xFFF59E0B).copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isPass) Color(0xFF10B981) else Color(0xFFF59E0B),
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = title, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
        Text(text = subtitle, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
    }
}

@Composable
private fun ReportSectionCard(
    title: String,
    icon: ImageVector,
    statusText: String,
    isPass: Boolean,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        border = BorderStroke(1.dp, if (isPass) MaterialTheme.colorScheme.outline.copy(alpha = 0.15f) else Color(0xFFF59E0B).copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isPass) Color(0xFF10B981) else Color(0xFFF59E0B),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (isPass) Color(0xFF10B981).copy(alpha = 0.15f) else Color(0xFFF59E0B).copy(alpha = 0.15f)
                ) {
                    Text(
                        text = if (isPass) "PASS" else "CHECK",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isPass) Color(0xFF10B981) else Color(0xFFF59E0B),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = statusText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(6.dp))
            content()

            if (actionLabel != null && onAction != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    FilledTonalButton(
                        onClick = onAction,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(actionLabel, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
