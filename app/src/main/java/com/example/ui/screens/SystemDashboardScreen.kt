package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import android.os.SystemClock
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.example.service.MockLocationService
import com.example.ui.AppManagerViewModel
import com.example.ui.navigation.Screen
import com.example.ui.components.FakeDeviceDialog
import com.example.utils.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SystemDashboardScreen(
    navController: NavController,
    appManagerViewModel: AppManagerViewModel
) {
    val t0 = remember { SystemClock.uptimeMillis() }
    SideEffect {
        Log.d("Tabs", "SystemDashboardScreen switched in ${SystemClock.uptimeMillis() - t0}ms")
    }

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    // Expanded section states
    var isDnsExpanded by remember { mutableStateOf(false) }
    var isWifiControllerExpanded by remember { mutableStateOf(false) }
    var isWirelessDebuggingExpanded by remember { mutableStateOf(false) }
    var isAnimationExpanded by remember { mutableStateOf(false) }
    var isFakeGpsExpanded by remember { mutableStateOf(true) } // Open by default for discovery
    var isFakeDeviceExpanded by remember { mutableStateOf(false) }
    var isAppManagerExpanded by remember { mutableStateOf(false) }

    // DNS state
    var selectedDnsProvider by remember { mutableStateOf(DnsManager.getSelectedDns(context)) }

    // Animation & Refresh state
    var animScale by remember { mutableStateOf(AnimationTweakManager.getCurrentAnimationScale(context)) }
    var isShizukuAvailable by remember { mutableStateOf(false) }
    var hasShizukuPerm by remember { mutableStateOf(false) }

    // Fake Device State
    val prefs = remember { context.getSharedPreferences("fake_device_prefs", Context.MODE_PRIVATE) }
    var currentSpoof by remember { mutableStateOf("Samsung Galaxy S25 Ultra") }
    var showFakeDeviceDialog by remember { mutableStateOf(false) }
    var deviceSearchQuery by remember { mutableStateOf("") }

    // Fake GPS State
    val isMockActive by FakeGpsManager.isMockingActive.collectAsStateWithLifecycle()
    val savedLat by FakeGpsManager.currentLat.collectAsStateWithLifecycle()
    val savedLng by FakeGpsManager.currentLng.collectAsStateWithLifecycle()
    val mockUpdateCount by FakeGpsManager.updateCounter.collectAsStateWithLifecycle()
    val mockError by FakeGpsManager.lastError.collectAsStateWithLifecycle()

    var inputLat by remember(savedLat) { mutableStateOf(savedLat.toString()) }
    var inputLng by remember(savedLng) { mutableStateOf(savedLng.toString()) }
    var currentPresetName by remember { mutableStateOf("Custom Coordinate") }
    var showMockHelpDialog by remember { mutableStateOf(false) }

    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(Unit) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            appManagerViewModel.onVisible()
            withContext(Dispatchers.IO) {
                FakeGpsManager.init(context)
                val dns = DnsManager.getSelectedDns(context)
                val scale = AnimationTweakManager.getCurrentAnimationScale(context)
                val shizukuAvail = AnimationTweakManager.isShizukuAvailable()
                val shizukuP = AnimationTweakManager.hasShizukuPermission()
                val preset = FakeGpsManager.getSavedPresetName(context)
                val spoof = prefs.getString("selected_device", "Samsung Galaxy S25 Ultra") ?: "Samsung Galaxy S25 Ultra"
                withContext(Dispatchers.Main) {
                    selectedDnsProvider = dns
                    animScale = scale
                    isShizukuAvailable = shizukuAvail
                    hasShizukuPerm = shizukuP
                    currentPresetName = preset
                    currentSpoof = spoof
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Dns,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("System Dashboard", fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    ) { paddingValues ->
        val listState = rememberSaveable(saver = LazyListState.Saver) { LazyListState() }
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Device & System Control Hub",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Manage Private DNS, Mock Location (Fake GPS), animation scales, device model spoofing, and installed package history.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
            }

            // 1. FAKE GPS / LOCATION SPOOFER CARD
            item {
                SystemFeatureCard(
                    title = "Fake GPS / Location Spoofer",
                    subtitle = if (isMockActive) "Broadcasting: $currentPresetName ($mockUpdateCount fixes)" else "Ready (Lat: $savedLat, Lng: $savedLng)",
                    icon = Icons.Default.LocationOn,
                    badge = if (isMockActive) "BROADCASTING" else "IDLE",
                    badgeColor = if (isMockActive) Color(0xFF10B981) else MaterialTheme.colorScheme.outline,
                    isExpanded = isFakeGpsExpanded,
                    onToggleExpand = {
                        SoundEffectManager.playClick()
                        isFakeGpsExpanded = !isFakeGpsExpanded
                    }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Mock Location Controller",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            IconButton(
                                onClick = {
                                    SoundEffectManager.playClick()
                                    showMockHelpDialog = true
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    Icons.Outlined.HelpOutline,
                                    contentDescription = "Setup Help",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // INTERACTIVE MAP PICKER PROMINENT BUTTON
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    SoundEffectManager.playClick()
                                    navController.navigate(Screen.FakeGps.route)
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(42.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                Icons.Default.Map,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onPrimary,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                    }
                                    Column {
                                        Text(
                                            text = "Interactive Map Picker",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.titleSmall,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "Full-screen map with draggable pin & live broadcast",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Icon(
                                    Icons.Default.ChevronRight,
                                    contentDescription = "Open Map",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        if (mockError != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.errorContainer,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = mockError ?: "",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Worldwide Popular Presets",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(FakeGpsManager.popularPresets) { preset ->
                                val isSelected = currentPresetName == preset.name
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        SoundEffectManager.playClick()
                                        currentPresetName = preset.name
                                        inputLat = preset.latitude.toString()
                                        inputLng = preset.longitude.toString()
                                        FakeGpsManager.saveCoordinates(context, preset.latitude, preset.longitude, preset.name)
                                        if (isMockActive) {
                                            MockLocationService.start(context, preset.latitude, preset.longitude)
                                        }
                                    },
                                    label = { Text("${preset.name} (${preset.country})", fontSize = 12.sp) },
                                    leadingIcon = if (isSelected) {
                                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                                    } else null
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = inputLat,
                                onValueChange = { inputLat = it },
                                label = { Text("Latitude") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp)
                            )
                            OutlinedTextField(
                                value = inputLng,
                                onValueChange = { inputLng = it },
                                label = { Text("Longitude") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Controls Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (!isMockActive) {
                                Button(
                                    onClick = {
                                        SoundEffectManager.playClick()
                                        val lat = inputLat.toDoubleOrNull()
                                        val lng = inputLng.toDoubleOrNull()
                                        if (lat == null || lng == null || lat < -90.0 || lat > 90.0 || lng < -180.0 || lng > 180.0) {
                                            Toast.makeText(context, "Please enter valid Latitude (-90..90) and Longitude (-180..180)", Toast.LENGTH_SHORT).show()
                                            return@Button
                                        }
                                        FakeGpsManager.saveCoordinates(context, lat, lng, currentPresetName)
                                        val started = FakeGpsManager.startMockLocation(context, lat, lng)
                                        if (started) {
                                            MockLocationService.start(context, lat, lng)
                                            Toast.makeText(context, "Mock Location started at ($lat, $lng)", Toast.LENGTH_SHORT).show()
                                        } else {
                                            showMockHelpDialog = true
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Start Mock GPS")
                                }
                            } else {
                                Button(
                                    onClick = {
                                        SoundEffectManager.playClick()
                                        MockLocationService.stop(context)
                                        FakeGpsManager.stopMockLocation(context)
                                        Toast.makeText(context, "Mock Location stopped.", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                ) {
                                    Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Stop Mock GPS")
                                }
                            }

                            OutlinedButton(
                                onClick = {
                                    SoundEffectManager.playClick()
                                    val lat = inputLat.toDoubleOrNull() ?: savedLat
                                    val lng = inputLng.toDoubleOrNull() ?: savedLng
                                    try {
                                        val geoUri = Uri.parse("geo:$lat,$lng?q=$lat,$lng(Spoofed+Location)")
                                        val mapIntent = Intent(Intent.ACTION_VIEW, geoUri)
                                        context.startActivity(mapIntent)
                                    } catch (e: Exception) {
                                        clipboardManager.setText(AnnotatedString("$lat, $lng"))
                                        Toast.makeText(context, "Coordinates copied to clipboard: $lat, $lng", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Map")
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Developer Options Setup:",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            TextButton(
                                onClick = {
                                    SoundEffectManager.playClick()
                                    FakeGpsManager.openDeveloperSettings(context)
                                }
                            ) {
                                Text("Open Developer Settings", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // 2. DNS CHANGER CARD
            item {
                SystemFeatureCard(
                    title = "DNS Changer (Private DNS)",
                    subtitle = "Current: ${selectedDnsProvider.name}",
                    icon = Icons.Default.Security,
                    badge = "Active",
                    badgeColor = Color(0xFF10B981),
                    isExpanded = isDnsExpanded,
                    onToggleExpand = {
                        SoundEffectManager.playClick()
                        isDnsExpanded = !isDnsExpanded
                    }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Quick DNS Presets",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        val quickPresets = listOf(
                            DnsManager.providers.firstOrNull { it.name.contains("Cloudflare", ignoreCase = true) } ?: DnsManager.providers[0],
                            DnsManager.providers.firstOrNull { it.name.contains("Google", ignoreCase = true) } ?: DnsManager.providers[1],
                            DnsManager.providers.firstOrNull { it.name.contains("AdGuard", ignoreCase = true) } ?: DnsManager.providers[2]
                        )

                        quickPresets.forEach { provider ->
                            val isSelected = selectedDnsProvider.id == provider.id
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable {
                                        SoundEffectManager.playClick()
                                        DnsManager.saveSelectedDns(context, provider)
                                        selectedDnsProvider = provider
                                        val applied = DnsSecureSettingsHelper.setPrivateDns(context, "hostname", provider.dotHost)
                                        if (applied) {
                                            Toast.makeText(context, "${provider.name} applied!", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, "Selected ${provider.name}. Tap below to apply via Settings or Shizuku", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                shape = RoundedCornerShape(14.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                                border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(provider.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                        Text(provider.dotHost, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    if (isSelected) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = {
                                SoundEffectManager.playClick()
                                navController.navigate(Screen.DnsChanger.route)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Open Full DNS Selector & VPN")
                        }
                    }
                }
            }

            // Wi-Fi CONTROLLER CARD
            item {
                com.example.feature.system.wifi.WifiControllerCard(
                    isExpanded = isWifiControllerExpanded,
                    onToggleExpand = {
                        SoundEffectManager.playClick()
                        isWifiControllerExpanded = !isWifiControllerExpanded
                    }
                )
            }

            // SHIZUKU ACCESS CARD
            item {
                val adbPrefs = remember { context.getSharedPreferences("adb_access_prefs", Context.MODE_PRIVATE) }
                var isUserStopped by remember { mutableStateOf(adbPrefs.getBoolean("is_user_stopped", false)) }
                var isShizukuStopped by remember { mutableStateOf(adbPrefs.getBoolean("is_shizuku_stopped", false)) }
                var showShizukuInfoDialog by remember { mutableStateOf(false) }

                var shizukuAvailable by remember { mutableStateOf(false) }
                var shizukuPermGranted by remember { mutableStateOf(false) }
                var shizukuServiceVersion by remember { mutableStateOf(0) }

                val updateShizukuStatus = {
                    val avail = try { rikka.shizuku.Shizuku.pingBinder() } catch (e: Exception) { false }
                    val perm = try { avail && rikka.shizuku.Shizuku.checkSelfPermission() == android.content.pm.PackageManager.PERMISSION_GRANTED } catch (e: Exception) { false }
                    val ver = try { if (avail) rikka.shizuku.Shizuku.getLatestServiceVersion() else 0 } catch (e: Exception) { 0 }
                    shizukuAvailable = avail
                    shizukuPermGranted = perm
                    shizukuServiceVersion = ver
                }

                DisposableEffect(Unit) {
                    updateShizukuStatus()
                    val binderListener = rikka.shizuku.Shizuku.OnBinderReceivedListener { updateShizukuStatus() }
                    val binderDeadListener = rikka.shizuku.Shizuku.OnBinderDeadListener { updateShizukuStatus() }
                    val permListener = rikka.shizuku.Shizuku.OnRequestPermissionResultListener { requestCode, grantResult ->
                        if (requestCode == 1001) {
                            shizukuPermGranted = (grantResult == android.content.pm.PackageManager.PERMISSION_GRANTED)
                            if (shizukuPermGranted) {
                                Toast.makeText(context, "Shizuku Permission Granted", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Shizuku Permission Denied", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    try {
                        rikka.shizuku.Shizuku.addBinderReceivedListener(binderListener)
                        rikka.shizuku.Shizuku.addBinderDeadListener(binderDeadListener)
                        rikka.shizuku.Shizuku.addRequestPermissionResultListener(permListener)
                    } catch (e: Exception) {}

                    onDispose {
                        try {
                            rikka.shizuku.Shizuku.removeBinderReceivedListener(binderListener)
                            rikka.shizuku.Shizuku.removeBinderDeadListener(binderDeadListener)
                            rikka.shizuku.Shizuku.removeRequestPermissionResultListener(permListener)
                        } catch (e: Exception) {}
                    }
                }

                val activeProvider = when {
                    isUserStopped || isShizukuStopped -> "None (Stopped by you)"
                    shizukuPermGranted && shizukuAvailable -> "Shizuku"
                    else -> "None"
                }

                val statusTextStr = when {
                    isUserStopped || isShizukuStopped -> "App access stopped by you"
                    !shizukuAvailable -> {
                        val installed = try {
                            context.packageManager.getPackageInfo("rikka.shizuku.manager", 0)
                            true
                        } catch (e: Exception) {
                            false
                        }
                        if (!installed) "Shizuku not installed" else "Shizuku service not running"
                    }
                    !shizukuPermGranted -> "Permission required"
                    else -> "Ready & Active"
                }

                val badgeColor = when {
                    isUserStopped || isShizukuStopped -> MaterialTheme.colorScheme.error
                    shizukuPermGranted && shizukuAvailable -> Color(0xFF10B981)
                    else -> Color(0xFFFF9800)
                }

                val statusDotColor = badgeColor

                SystemFeatureCard(
                    title = "Shizuku Access",
                    subtitle = "Active: $activeProvider • Status: $statusTextStr",
                    icon = Icons.Default.AdminPanelSettings,
                    badge = if (isUserStopped || isShizukuStopped) "Stopped" else if (shizukuPermGranted && shizukuAvailable) "Active" else "Setup Needed",
                    badgeColor = badgeColor,
                    statusDotColor = statusDotColor,
                    isExpanded = isWirelessDebuggingExpanded,
                    onToggleExpand = {
                        SoundEffectManager.playClick()
                        isWirelessDebuggingExpanded = !isWirelessDebuggingExpanded
                    }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Shizuku Access & Status",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("• Active Provider: $activeProvider", style = MaterialTheme.typography.bodySmall)
                        Text("• Status: $statusTextStr", style = MaterialTheme.typography.bodySmall)

                        Spacer(modifier = Modifier.height(12.dp))

                        if (!isUserStopped && !isShizukuStopped) {
                            Button(
                                onClick = {
                                    SoundEffectManager.playClick()
                                    isUserStopped = true
                                    isShizukuStopped = true
                                    adbPrefs.edit()
                                        .putBoolean("is_user_stopped", true)
                                        .putBoolean("is_shizuku_stopped", true)
                                        .apply()
                                    showShizukuInfoDialog = true
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                            ) {
                                Icon(Icons.Default.Block, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Stop App Access")
                            }
                        } else {
                            Button(
                                onClick = {
                                    SoundEffectManager.playClick()
                                    isUserStopped = false
                                    isShizukuStopped = false
                                    adbPrefs.edit()
                                        .putBoolean("is_user_stopped", false)
                                        .putBoolean("is_shizuku_stopped", false)
                                        .apply()
                                    Toast.makeText(context, "App access resumed.", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Resume Access")
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Shizuku Management",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        SoundEffectManager.playClick()
                                        try {
                                            val intent = context.packageManager.getLaunchIntentForPackage("rikka.shizuku.manager")
                                                ?: Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=rikka.shizuku.manager"))
                                            context.startActivity(intent)
                                        } catch (e: Exception) {
                                            try {
                                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://shizuku.rikka.app/")))
                                            } catch (ex: Exception) {
                                                Toast.makeText(context, "Unable to open Shizuku manager", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.Launch, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Open Shizuku", fontSize = 11.sp)
                                }

                                if (!shizukuPermGranted && shizukuAvailable) {
                                    Button(
                                        onClick = {
                                            SoundEffectManager.playClick()
                                            try {
                                                rikka.shizuku.Shizuku.requestPermission(1001)
                                                Toast.makeText(context, "Requested Shizuku permission...", Toast.LENGTH_SHORT).show()
                                            } catch (e: Throwable) {
                                                Toast.makeText(context, "Failed to request Shizuku permission: ${e.message}", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                                    ) {
                                        Text("Authorize Shizuku", fontSize = 11.sp)
                                    }
                                } else {
                                    OutlinedButton(
                                        onClick = {
                                            SoundEffectManager.playClick()
                                            showShizukuInfoDialog = true
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text("Manage / Revoke", fontSize = 11.sp)
                                    }
                                }
                            }
                        }

                        if (showShizukuInfoDialog) {
                            AlertDialog(
                                onDismissRequest = { showShizukuInfoDialog = false },
                                title = { Text("Shizuku is Inactive") },
                                text = {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text("Privileged actions are currently blocked because Shizuku is not active or authorized.", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                        Text("1. Install and open the Shizuku app.", style = MaterialTheme.typography.bodySmall)
                                        Text("2. Start the Shizuku service (via Wireless Debugging or root).", style = MaterialTheme.typography.bodySmall)
                                        Text("3. Return here and tap 'Authorize Shizuku' or 'Resume Access' to enable system utilities.", style = MaterialTheme.typography.bodySmall)
                                    }
                                },
                                confirmButton = {
                                    Button(
                                        onClick = {
                                            showShizukuInfoDialog = false
                                            try {
                                                val intent = context.packageManager.getLaunchIntentForPackage("rikka.shizuku.manager")
                                                if (intent != null) {
                                                    context.startActivity(intent)
                                                } else {
                                                    Toast.makeText(context, "Shizuku app not found", Toast.LENGTH_SHORT).show()
                                                }
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "Could not open Shizuku", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    ) {
                                        Text("Open Shizuku")
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showShizukuInfoDialog = false }) {
                                        Text("Dismiss")
                                    }
                                }
                            )
                        }
                    }
                }
            }

            // 3. ANIMATION SPEED CARD
            item {
                SystemFeatureCard(
                    title = "Animation Speed & Scales",
                    subtitle = "Window/Transition/Animator scales: ${animScale}x",
                    icon = Icons.Default.Speed,
                    badge = if (hasShizukuPerm) "ADB Ready" else "Shizuku Required",
                    badgeColor = if (hasShizukuPerm) Color(0xFF10B981) else MaterialTheme.colorScheme.error,
                    isExpanded = isAnimationExpanded,
                    onToggleExpand = {
                        SoundEffectManager.playClick()
                        isAnimationExpanded = !isAnimationExpanded
                    }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Quick Animation Scale Preset",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(0.0f, 0.25f, 0.5f, 1.0f).forEach { scale ->
                                val isSelected = animScale == scale
                                Button(
                                    onClick = {
                                        SoundEffectManager.playClick()
                                        AnimationTweakManager.setAnimationScales(context, scale)
                                        animScale = scale
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = if (isSelected) ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary) else ButtonDefaults.outlinedButtonColors()
                                ) {
                                    Text(if (scale == 0.0f) "Off" else "${scale}x", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = {
                                SoundEffectManager.playClick()
                                navController.navigate(Screen.AnimationSettings.route)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.DisplaySettings, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Open Advanced Animation & Display Settings")
                        }
                    }
                }
            }

            // 4. FAKE DEVICE SWITCHER CARD
            item {
                SystemFeatureCard(
                    title = "Fake Device Switcher",
                    subtitle = "Active Spoof: $currentSpoof",
                    icon = Icons.Default.PhoneAndroid,
                    badge = "Device Model",
                    badgeColor = MaterialTheme.colorScheme.primary,
                    isExpanded = isFakeDeviceExpanded,
                    onToggleExpand = {
                        SoundEffectManager.playClick()
                        isFakeDeviceExpanded = !isFakeDeviceExpanded
                    }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Spoof system model to unlock gaming 120 FPS or device-specific capabilities.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Selected Model:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                                Text(currentSpoof, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            }

                            Button(
                                onClick = {
                                    SoundEffectManager.playClick()
                                    showFakeDeviceDialog = true
                                },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Shuffle, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Choose Device")
                            }
                        }
                    }
                }
            }

            // 5. APP MANAGER & HISTORY CARD
            item {
                com.example.feature.system.apps.AppManagerCard(
                    isExpanded = isAppManagerExpanded,
                    onToggleExpand = {
                        SoundEffectManager.playClick()
                        isAppManagerExpanded = !isAppManagerExpanded
                    },
                    navController = navController
                )
            }

            // 6. OTHER UTILITIES
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Other System Utilities",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(100.dp)
                            .clickable {
                                SoundEffectManager.playClick()
                                navController.navigate(Screen.Modules.route)
                            },
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(12.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.SportsEsports, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Game Booster", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }

                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(100.dp)
                            .clickable {
                                SoundEffectManager.playClick()
                                navController.navigate(Screen.ImageMetadataEditor.route)
                            },
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(12.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Tune, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Image Metadata", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }

    if (showMockHelpDialog) {
        AlertDialog(
            onDismissRequest = { showMockHelpDialog = false },
            icon = { Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text("Enable Mock Location in Android", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "To allow this app to inject fake GPS coordinates system-wide:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text("1. Open Android **Developer Options**.", fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodySmall)
                    Text("2. Scroll to **Select mock location app**.", fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodySmall)
                    Text("3. Choose **${context.applicationInfo.loadLabel(context.packageManager)}**.", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
                    Text("4. Return here and tap **Start Mock GPS**.", fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodySmall)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showMockHelpDialog = false
                        FakeGpsManager.openDeveloperSettings(context)
                    }
                ) {
                    Text("Open Developer Options")
                }
            },
            dismissButton = {
                TextButton(onClick = { showMockHelpDialog = false }) {
                    Text("Got it")
                }
            }
        )
    }

    if (showFakeDeviceDialog) {
        FakeDeviceDialog(
            onDismissRequest = { showFakeDeviceDialog = false },
            currentSelectedDevice = currentSpoof,
            onSelectDevice = { profile, isShortName ->
                val chosenName = if (isShortName) profile.shortName else profile.fullName
                prefs.edit().putString("selected_device", chosenName).apply()
                currentSpoof = chosenName
                showFakeDeviceDialog = false
                FakeDeviceManager.applyDeviceProfile(context, profile, isShortName)
            }
        )
    }
}

@Composable
fun SystemFeatureCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    badge: String,
    badgeColor: Color,
    statusDotColor: Color? = null,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpand() }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                        .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (statusDotColor != null) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(statusDotColor)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = badgeColor.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.35f))
                ) {
                    Text(
                        text = badge,
                        style = MaterialTheme.typography.labelSmall,
                        color = badgeColor,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                    content()
                }
            }
        }
    }
}
