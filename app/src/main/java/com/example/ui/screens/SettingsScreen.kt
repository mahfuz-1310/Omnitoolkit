package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.toArgb
import kotlin.math.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.ui.MainViewModel
import com.example.ui.components.FakeDeviceDialog
import androidx.compose.animation.AnimatedVisibility
import com.example.ui.navigation.Screen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import android.os.SystemClock
import android.util.Log

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController, viewModel: MainViewModel) {
    val t0 = remember { SystemClock.uptimeMillis() }
    SideEffect {
        Log.d("Tabs", "SettingsScreen switched in ${SystemClock.uptimeMillis() - t0}ms")
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(Unit) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.onSettingsVisible()
        }
    }

    val darkMode by viewModel.darkMode.collectAsStateWithLifecycle()
    val hapticFeedback by viewModel.hapticFeedback.collectAsStateWithLifecycle()
    val soundEffects by viewModel.soundEffects.collectAsStateWithLifecycle()
    val appIcon by viewModel.appIcon.collectAsStateWithLifecycle()
    val animations by viewModel.animationsEnabled.collectAsStateWithLifecycle()
    val floatingMode by viewModel.floatingModeEnabled.collectAsStateWithLifecycle()
    val rememberPos by viewModel.rememberFloatingPosition.collectAsStateWithLifecycle()
    val defaultSize by viewModel.defaultFloatingSize.collectAsStateWithLifecycle()
    val uiColorLong by viewModel.uiColor.collectAsStateWithLifecycle()
    val buttonColorLong by viewModel.buttonColor.collectAsStateWithLifecycle()
    
    val history by viewModel.allHistory.collectAsStateWithLifecycle()
    val favorites by viewModel.allFavorites.collectAsStateWithLifecycle()
    
    val context = LocalContext.current

    var showClearHistoryDialog by remember { mutableStateOf(false) }
    var showClearFavDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showResetColorsDialog by remember { mutableStateOf(false) }
    var showPermissionDialog by remember { mutableStateOf(false) }
    var showDnsDialog by remember { mutableStateOf(false) }
    var showInstructionsDialog by remember { mutableStateOf(false) }
    var showColorPickerDialog by remember { mutableStateOf(false) }
    var originalUiColor by remember { mutableStateOf(uiColorLong) }
    var originalButtonColor by remember { mutableStateOf(buttonColorLong) }
    var selectedOptionValue by remember { mutableStateOf("automatic") }
    var customDnsInput by remember { mutableStateOf("") }
    var selectedCategoryIndex by remember { mutableStateOf(0) }
    var showFakeDeviceDialog by remember { mutableStateOf(false) }
    var fakeDeviceSearchQuery by remember { mutableStateOf("") }
    
    // Performance, Game, Display, and Thermal states
    var refreshRate by remember { mutableStateOf("120Hz") }
    var frameRateControl by remember { mutableStateOf(true) }
    var optimizedThermal by remember { mutableStateOf(true) }
    var gameSetting by remember { mutableStateOf("Performance Mode") }
    var performanceSettings by remember { mutableStateOf(true) }
    var renderingSettings by remember { mutableStateOf("Vulkan") }
    var touchscreenOptimized by remember { mutableStateOf(true) }
    var selectedFakeDevice by remember { mutableStateOf("Default") }
    var liveDnsMode by remember { mutableStateOf("automatic") }
    var liveDnsSpecifier by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        try {
            selectedFakeDevice = Settings.Global.getString(context.contentResolver, "device_name") ?: "Default"
        } catch (e: Throwable) {
            selectedFakeDevice = "Default"
        }
        try {
            liveDnsMode = Settings.Global.getString(context.contentResolver, "private_dns_mode") ?: "automatic"
        } catch (e: Throwable) {
            liveDnsMode = "automatic"
        }
        try {
            liveDnsSpecifier = Settings.Global.getString(context.contentResolver, "private_dns_specifier") ?: ""
        } catch (e: Throwable) {
            liveDnsSpecifier = ""
        }
    }

    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = { Text("Display Over Other Apps") },
            text = { Text("Display over other apps permission is required to use Floating Mode over other apps.") },
            confirmButton = {
                TextButton(onClick = {
                    showPermissionDialog = false
                    val intent = Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:${context.packageName}")
                    )
                    context.startActivity(intent)
                }) { Text("Allow Permission") }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showResetColorsDialog) {
        AlertDialog(
            onDismissRequest = { showResetColorsDialog = false },
            title = { Text("Reset Colors") },
            text = { Text("Are you sure you want to reset UI and Button colors to default?") },
            confirmButton = {
                TextButton(onClick = { viewModel.resetColors(); showResetColorsDialog = false }) { Text("Reset", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showResetColorsDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showClearHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showClearHistoryDialog = false },
            title = { Text("Clear History") },
            text = { Text("Are you sure you want to clear all history?") },
            confirmButton = {
                TextButton(onClick = { viewModel.clearHistory(); showClearHistoryDialog = false }) { Text("Clear", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showClearHistoryDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showClearFavDialog) {
        AlertDialog(
            onDismissRequest = { showClearFavDialog = false },
            title = { Text("Clear Favorites") },
            text = { Text("Are you sure you want to clear all favorites?") },
            confirmButton = {
                TextButton(onClick = { viewModel.clearFavorites(); showClearFavDialog = false }) { Text("Clear", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showClearFavDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showDnsDialog) {
        AlertDialog(
            onDismissRequest = { showDnsDialog = false },
            title = { Text("Select DNS Server") },
            text = {
                val categories = listOf("Fastest", "Ad-Blocking", "Private")
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TabRow(
                        selectedTabIndex = selectedCategoryIndex,
                        containerColor = Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                    ) {
                        categories.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedCategoryIndex == index,
                                onClick = { selectedCategoryIndex = index },
                                text = { Text(title, style = MaterialTheme.typography.labelLarge) }
                            )
                        }
                    }

                    val dnsOptions = when (selectedCategoryIndex) {
                        0 -> listOf(
                            Pair("Cloudflare DNS (Fastest)", "one.one.one.one"),
                            Pair("Google DNS (Stable)", "dns.google"),
                            Pair("Auto DNS (Default)", "automatic")
                        )
                        1 -> listOf(
                            Pair("AdGuard DNS (Block Ads)", "dns.adguard-dns.com"),
                            Pair("NextDNS (Gaming Optimized)", "dns.nextdns.io")
                        )
                        else -> listOf(
                            Pair("Quad9 DNS (Security)", "dns.quad9.net"),
                            Pair("Custom DNS", "custom")
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 280.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        dnsOptions.forEach { option ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        selectedOptionValue = option.second
                                    }
                                    .padding(vertical = 4.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = (selectedOptionValue == option.second),
                                    onClick = { selectedOptionValue = option.second }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(option.first, style = MaterialTheme.typography.bodyLarge)
                                    if (option.second != "custom" && option.second != "automatic") {
                                        Text(option.second, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                        }

                        if (selectedOptionValue == "custom") {
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = customDnsInput,
                                onValueChange = { customDnsInput = it },
                                label = { Text("Custom Hostname") },
                                placeholder = { Text("e.g. private.dns.com") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val modeToApply = if (selectedOptionValue == "automatic") "opportunistic" else "hostname"
                        val specifierToApply = if (selectedOptionValue == "automatic") null else if (selectedOptionValue == "custom") customDnsInput.trim() else selectedOptionValue

                        if (modeToApply == "hostname" && specifierToApply.isNullOrBlank()) {
                            Toast.makeText(context, "Please enter a valid hostname", Toast.LENGTH_SHORT).show()
                        } else {
                            if (com.example.utils.DnsSecureSettingsHelper.isShizukuAvailable() && !com.example.utils.DnsSecureSettingsHelper.hasShizukuPermission()) {
                                com.example.utils.DnsSecureSettingsHelper.setPendingAction {
                                    val success = com.example.utils.DnsSecureSettingsHelper.setPrivateDns(context, modeToApply, specifierToApply)
                                    if (success) {
                                        liveDnsMode = modeToApply
                                        liveDnsSpecifier = specifierToApply ?: ""
                                        Toast.makeText(context, "System Private DNS updated successfully!", Toast.LENGTH_LONG).show()
                                    }
                                }
                                com.example.utils.DnsSecureSettingsHelper.requestShizukuPermission(1001)
                                Toast.makeText(context, "Authorizing Shizuku access...", Toast.LENGTH_SHORT).show()
                                showDnsDialog = false
                            } else {
                                val success = com.example.utils.DnsSecureSettingsHelper.setPrivateDns(context, modeToApply, specifierToApply)
                                if (success) {
                                    liveDnsMode = modeToApply
                                    liveDnsSpecifier = specifierToApply ?: ""
                                    Toast.makeText(context, "System Private DNS updated successfully!", Toast.LENGTH_LONG).show()
                                    showDnsDialog = false
                                } else {
                                    showDnsDialog = false
                                    showInstructionsDialog = true
                                }
                            }
                        }
                    }
                ) {
                    Text("Apply")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDnsDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showInstructionsDialog) {
        AlertDialog(
            onDismissRequest = { showInstructionsDialog = false },
            title = { Text("Permissions Required") },
            text = {
                Text(
                    "We could not apply the Private DNS system-wide automatically because root access, Shizuku privileges, or secure write permissions are currently unavailable.\n\n" +
                    "To fix this, please make sure Shizuku is running and authorized, or grant secure write permission manually using ADB:\n\n" +
                    "adb shell pm grant com.aistudio.namegenpro.kqxzlz android.permission.WRITE_SECURE_SETTINGS\n\n" +
                    "Alternatively, you can configure it manually in Android's native settings."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showInstructionsDialog = false
                        com.example.utils.DnsSecureSettingsHelper.openPrivateDnsSettings(context)
                    }
                ) {
                    Text("Configure Manually")
                }
            },
            dismissButton = {
                TextButton(onClick = { showInstructionsDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
    
    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("Export Data") },
            text = { Text("Choose a format to export your History and Favorites.") },
            confirmButton = {},
            dismissButton = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = { exportData(context, history, favorites, "TXT"); showExportDialog = false }, modifier = Modifier.fillMaxWidth()) { Text("Export as TXT") }
                    TextButton(onClick = { exportData(context, history, favorites, "CSV"); showExportDialog = false }, modifier = Modifier.fillMaxWidth()) { Text("Export as CSV") }
                    TextButton(onClick = { exportData(context, history, favorites, "JSON"); showExportDialog = false }, modifier = Modifier.fillMaxWidth()) { Text("Export as JSON") }
                    Spacer(modifier = Modifier.height(16.dp))
                    TextButton(onClick = { showExportDialog = false }, modifier = Modifier.fillMaxWidth()) { Text("Cancel", color = MaterialTheme.colorScheme.error) }
                }
            }
        )
    }

    if (showFakeDeviceDialog) {
        FakeDeviceDialog(
            onDismissRequest = { showFakeDeviceDialog = false },
            currentSelectedDevice = selectedFakeDevice,
            onSelectDevice = { profile, isShortName ->
                val targetName = if (isShortName) profile.shortName else profile.fullName
                selectedFakeDevice = targetName
                showFakeDeviceDialog = false

                if (com.example.utils.DnsSecureSettingsHelper.isShizukuAvailable() && !com.example.utils.DnsSecureSettingsHelper.hasShizukuPermission()) {
                    com.example.utils.DnsSecureSettingsHelper.setPendingAction {
                        val success = com.example.utils.FakeDeviceManager.applyDeviceProfile(context, profile, isShortName)
                        if (success) {
                            Toast.makeText(context, "$targetName active", Toast.LENGTH_SHORT).show()
                        }
                    }
                    com.example.utils.DnsSecureSettingsHelper.requestShizukuPermission(1001)
                    Toast.makeText(context, "Authorizing Shizuku access...", Toast.LENGTH_SHORT).show()
                } else {
                    val success = com.example.utils.FakeDeviceManager.applyDeviceProfile(context, profile, isShortName)
                    if (success) {
                        Toast.makeText(context, "$targetName active", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            ListItem(
                headlineContent = { Text("Theme Mode", style = MaterialTheme.typography.bodyLarge) },
                supportingContent = { 
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 4.dp)) {
                        val whiteTheme by viewModel.whiteTheme.collectAsStateWithLifecycle()
                        listOf("System", "Light", "Dark", "White").forEach { mode ->
                            val isSelected = when (mode) {
                                "System" -> darkMode == null && !whiteTheme
                                "Light" -> darkMode == false && !whiteTheme
                                "Dark" -> darkMode == true && !whiteTheme
                                "White" -> whiteTheme == true
                                else -> false
                            }
                            FilterChip(
                                selected = isSelected,
                                onClick = { 
                                    when (mode) {
                                        "System" -> { viewModel.setDarkMode(null); viewModel.setWhiteTheme(false) }
                                        "Light" -> { viewModel.setDarkMode(false); viewModel.setWhiteTheme(false) }
                                        "Dark" -> { viewModel.setDarkMode(true); viewModel.setWhiteTheme(false) }
                                        "White" -> { viewModel.setWhiteTheme(true) }
                                    }
                                },
                                label = { Text(mode) }
                            )
                        }
                    }
                },
                colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.background)
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

            // Appearance Section
            Text("Appearance", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(16.dp))

            // Theme presets
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Theme / Accent Color Presets",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Instantly change background highlights, button accents, and active UI tints simultaneously.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val presets = listOf(
                            Pair("Purple", 0xFF6C63FFL),
                            Pair("Blue", 0xFF1E88E5L),
                            Pair("Emerald", 0xFF00C853L),
                            Pair("Crimson", 0xFFD50000L),
                            Pair("Amber", 0xFFFFAB00L),
                            Pair("Cyan", 0xFF00838FL)
                        )
                        presets.forEach { (name, colorValue) ->
                            val isSelected = uiColorLong == colorValue && buttonColorLong == colorValue
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(Color(colorValue.toInt()))
                                    .clickable {
                                        com.example.utils.SoundEffectManager.playClick()
                                        viewModel.setUiColor(colorValue)
                                        viewModel.setButtonColor(colorValue)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected $name",
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedButton(
                        onClick = {
                            com.example.utils.SoundEffectManager.playClick()
                            originalUiColor = uiColorLong
                            originalButtonColor = buttonColorLong
                            showColorPickerDialog = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(imageVector = Icons.Default.Palette, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Custom Color Wheel Picker")
                    }
                }
            }

            ColorSliderSetting(
                title = "UI Color",
                description = "Drag to change the app accent color",
                currentColorLong = uiColorLong,
                onColorChanged = { viewModel.setUiColor(it) }
            )

            ColorSliderSetting(
                title = "Button Color",
                description = "Drag to change primary button color",
                currentColorLong = buttonColorLong,
                onColorChanged = { viewModel.setButtonColor(it) }
            )

            Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                Button(
                    onClick = {},
                    colors = ButtonDefaults.buttonColors(containerColor = Color(buttonColorLong.toInt())),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("✨ Generate Name Preview")
                }
            }

            ListItem(
                headlineContent = { Text("Reset Colors", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyLarge) },
                modifier = Modifier.clickable { showResetColorsDialog = true },
                colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.background)
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            
            ListItem(
                headlineContent = { Text("Sound Effects", style = MaterialTheme.typography.bodyLarge) },
                supportingContent = { Text("Play click & audio chime effects", style = MaterialTheme.typography.bodyMedium) },
                trailingContent = {
                    Switch(
                        checked = soundEffects,
                        onCheckedChange = { viewModel.setSoundEffects(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary, checkedTrackColor = MaterialTheme.colorScheme.primaryContainer)
                    )
                },
                colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.background)
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

            ListItem(
                headlineContent = { Text("Haptic Feedback / Vibration", style = MaterialTheme.typography.bodyLarge) },
                supportingContent = { Text("Vibration on button presses and actions", style = MaterialTheme.typography.bodyMedium) },
                trailingContent = {
                    Switch(
                        checked = hapticFeedback,
                        onCheckedChange = { viewModel.setHapticFeedback(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary, checkedTrackColor = MaterialTheme.colorScheme.primaryContainer)
                    )
                },
                colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.background)
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

            // Custom App Icon Switcher Section
            AppIconSelectorSection(viewModel = viewModel, context = context)
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            
            ListItem(
                headlineContent = { Text("Floating Mode", style = MaterialTheme.typography.bodyLarge) },
                supportingContent = { Text("Show system-wide floating overlay bubble", style = MaterialTheme.typography.bodyMedium) },
                trailingContent = {
                    Switch(
                        checked = floatingMode,
                        onCheckedChange = { enabled ->
                            if (enabled) {
                                val canOverlay = try {
                                    Settings.canDrawOverlays(context)
                                } catch (e: Throwable) {
                                    false
                                }
                                if (canOverlay) {
                                    viewModel.setFloatingMode(true)
                                    try {
                                        if (!com.example.service.FloatingOverlayService.isRunning) {
                                            context.startService(Intent(context, com.example.service.FloatingOverlayService::class.java))
                                        }
                                    } catch (e: Throwable) {
                                        Toast.makeText(context, "Could not start floating service", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    showPermissionDialog = true
                                }
                            } else {
                                viewModel.setFloatingMode(false)
                                try {
                                    context.stopService(Intent(context, com.example.service.FloatingOverlayService::class.java))
                                } catch (e: Throwable) {
                                    // ignore
                                }
                            }
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary, checkedTrackColor = MaterialTheme.colorScheme.primaryContainer)
                    )
                },
                colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.background)
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

            ListItem(
                headlineContent = { Text("Remember Floating Position", style = MaterialTheme.typography.bodyLarge) },
                supportingContent = { Text("Save dragged window location", style = MaterialTheme.typography.bodyMedium) },
                trailingContent = {
                    Switch(
                        checked = rememberPos,
                        onCheckedChange = { viewModel.setRememberFloatingPosition(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary, checkedTrackColor = MaterialTheme.colorScheme.primaryContainer)
                    )
                },
                colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.background)
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

            ListItem(
                headlineContent = { Text("Default Floating Size", style = MaterialTheme.typography.bodyLarge) },
                supportingContent = { 
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 4.dp)) {
                        listOf("Compact", "Medium", "Large").forEach { size ->
                            FilterChip(
                                selected = defaultSize == size,
                                onClick = { viewModel.setDefaultFloatingSize(size) },
                                label = { Text(size) }
                            )
                        }
                    }
                },
                colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.background)
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

            ListItem(
                headlineContent = { Text("Animations", style = MaterialTheme.typography.bodyLarge) },
                supportingContent = { Text("Enable smooth transitions", style = MaterialTheme.typography.bodyMedium) },
                trailingContent = {
                    Switch(
                        checked = animations,
                        onCheckedChange = { viewModel.setAnimations(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary, checkedTrackColor = MaterialTheme.colorScheme.primaryContainer)
                    )
                },
                colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.background)
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

            // Performance & Display Settings Section
            Text(
                text = "Performance & Display Settings",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(16.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
            ) {
                Column {
                    // 1. Refresh Rate
                    ListItem(
                        headlineContent = { Text("Refresh Rate", style = MaterialTheme.typography.bodyLarge) },
                        supportingContent = {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 4.dp)) {
                                listOf("60Hz", "90Hz", "120Hz").forEach { rate ->
                                    FilterChip(
                                        selected = refreshRate == rate,
                                        onClick = { refreshRate = rate },
                                        label = { Text(rate) }
                                    )
                                }
                            }
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))

                    // 2. Frame Rate Control
                    ListItem(
                        headlineContent = { Text("Frame Rate Control", style = MaterialTheme.typography.bodyLarge) },
                        supportingContent = { Text("Dynamically adjust FPS during gameplay", style = MaterialTheme.typography.bodyMedium) },
                        trailingContent = {
                            Switch(
                                checked = frameRateControl,
                                onCheckedChange = { frameRateControl = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary, checkedTrackColor = MaterialTheme.colorScheme.primaryContainer)
                            )
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))

                    // 3. Optimized Thermal
                    ListItem(
                        headlineContent = { Text("Optimized Thermal", style = MaterialTheme.typography.bodyLarge) },
                        supportingContent = { Text("Cool down device to avoid thermal throttling", style = MaterialTheme.typography.bodyMedium) },
                        trailingContent = {
                            Switch(
                                checked = optimizedThermal,
                                onCheckedChange = { optimizedThermal = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary, checkedTrackColor = MaterialTheme.colorScheme.primaryContainer)
                            )
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))

                    // 4. Game Setting
                    ListItem(
                        headlineContent = { Text("Game Setting Mode", style = MaterialTheme.typography.bodyLarge) },
                        supportingContent = {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 4.dp)) {
                                listOf("Battery Saver", "Balanced", "Performance").forEach { mode ->
                                    FilterChip(
                                        selected = gameSetting.startsWith(mode),
                                        onClick = { gameSetting = "$mode Mode" },
                                        label = { Text(mode) }
                                    )
                                }
                            }
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))

                    // 5. Performance Settings
                    ListItem(
                        headlineContent = { Text("Performance Settings", style = MaterialTheme.typography.bodyLarge) },
                        supportingContent = { Text("Maximize hardware utilization", style = MaterialTheme.typography.bodyMedium) },
                        trailingContent = {
                            Switch(
                                checked = performanceSettings,
                                onCheckedChange = { performanceSettings = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary, checkedTrackColor = MaterialTheme.colorScheme.primaryContainer)
                            )
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))

                    // 6. Rendering Settings
                    ListItem(
                        headlineContent = { Text("Rendering Settings API", style = MaterialTheme.typography.bodyLarge) },
                        supportingContent = {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 4.dp)) {
                                listOf("OpenGL", "Vulkan", "Software").forEach { api ->
                                    FilterChip(
                                        selected = renderingSettings == api,
                                        onClick = { renderingSettings = api },
                                        label = { Text(api) }
                                    )
                                }
                            }
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))

                    // 7. Touchscreen Optimized
                    ListItem(
                        headlineContent = { Text("Touchscreen Optimized", style = MaterialTheme.typography.bodyLarge) },
                        supportingContent = { Text("Enhance touch response and reduce latency", style = MaterialTheme.typography.bodyMedium) },
                        trailingContent = {
                            Switch(
                                checked = touchscreenOptimized,
                                onCheckedChange = { touchscreenOptimized = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary, checkedTrackColor = MaterialTheme.colorScheme.primaryContainer)
                            )
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))

                    // Shortcut to dedicated System tab
                    ListItem(
                        headlineContent = { Text("System & Device Control", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold) },
                        supportingContent = { Text("Manage Private DNS, Animation Speeds, Fake Device & App Manager", style = MaterialTheme.typography.bodyMedium) },
                        leadingContent = { Icon(Icons.Default.Dns, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                        modifier = Modifier.clickable {
                            com.example.utils.SoundEffectManager.playClick()
                            navController.navigate(Screen.SystemDashboard.route)
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.padding(top = 16.dp))

            // Data & Storage Section
            Text("Data & Storage", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(16.dp))

            ListItem(
                headlineContent = { Text("Export Data", style = MaterialTheme.typography.bodyLarge) },
                supportingContent = { Text("Share history and favorites (TXT/CSV/JSON)", style = MaterialTheme.typography.bodyMedium) },
                leadingContent = { Icon(Icons.Default.Share, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                modifier = Modifier.clickable { showExportDialog = true },
                colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.background)
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            
            ListItem(
                headlineContent = { Text("Clear History", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyLarge) },
                modifier = Modifier.clickable { showClearHistoryDialog = true },
                colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.background)
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            
            ListItem(
                headlineContent = { Text("Clear Favorites", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyLarge) },
                modifier = Modifier.clickable { showClearFavDialog = true },
                colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.background)
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

            ListItem(
                headlineContent = { Text("About App", style = MaterialTheme.typography.bodyLarge) },
                leadingContent = { Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                modifier = Modifier.clickable { navController.navigate(Screen.About.route) },
                colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.background)
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

            var showPerformanceDetails by remember { mutableStateOf(false) }
            val startupData by com.example.feature.common.metrics.StartupMetrics.startupData.collectAsStateWithLifecycle()

            ListItem(
                headlineContent = { Text("Startup & UI Performance", style = MaterialTheme.typography.bodyLarge) },
                supportingContent = { 
                    Text(
                        if (startupData.ttidMs > 0) "TTID: ${startupData.ttidMs}ms • TTFF: ${startupData.ttffMs}ms" 
                        else "Cold start & render telemetry",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ) 
                },
                leadingContent = { Icon(Icons.Default.Speed, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary) },
                trailingContent = {
                    IconButton(onClick = { showPerformanceDetails = !showPerformanceDetails }) {
                        Icon(
                            if (showPerformanceDetails) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = "Expand performance"
                        )
                    }
                },
                modifier = Modifier.clickable { showPerformanceDetails = !showPerformanceDetails },
                colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.background)
            )

            AnimatedVisibility(visible = showPerformanceDetails) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "⚡ Cold Start & Render Telemetry",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Time to Initial Display (TTID):", style = MaterialTheme.typography.bodyMedium)
                            Text("${startupData.ttidMs} ms", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Time to Full Fidelity (TTFF):", style = MaterialTheme.typography.bodyMedium)
                            Text("${startupData.ttffMs} ms", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Activity Creation:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${startupData.activityCreateMs} ms", style = MaterialTheme.typography.bodySmall)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("First Compose Render:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${startupData.firstComposeMs} ms", style = MaterialTheme.typography.bodySmall)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Hardware Frame Draw:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${startupData.firstDrawMs} ms", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(16.dp))

            // Developer Credits & Branding Footer
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = com.example.utils.AppConstants.APP_NAME,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = "Version 1.0.1",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "App Developed by Mahfuz",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFF59E0B),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Designed by Mahfuz",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    if (showColorPickerDialog) {
        ColorWheelPickerDialog(
            initialColorLong = originalUiColor,
            onDismissRequest = {
                viewModel.setUiColor(originalUiColor)
                viewModel.setButtonColor(originalButtonColor)
                showColorPickerDialog = false
            },
            onConfirm = { finalColor ->
                viewModel.setUiColor(finalColor)
                viewModel.setButtonColor(finalColor)
                showColorPickerDialog = false
            },
            onLiveColorUpdate = { liveColor ->
                viewModel.setUiColor(liveColor)
                viewModel.setButtonColor(liveColor)
            }
        )
    }
}

@Composable
fun ColorSliderSetting(
    title: String,
    description: String,
    currentColorLong: Long,
    onColorChanged: (Long) -> Unit
) {
    val argb = currentColorLong.toInt()
    var hue by remember(currentColorLong) {
        val hsv = FloatArray(3)
        android.graphics.Color.colorToHSV(argb, hsv)
        mutableFloatStateOf(hsv[0])
    }
    
    val previewColorInt = remember(hue) {
        android.graphics.Color.HSVToColor(floatArrayOf(hue, 0.85f, 0.9f))
    }
    val previewColor = remember(previewColorInt) { Color(previewColorInt) }
    val hexString = remember(previewColorInt) { String.format("#%08X", previewColorInt) }

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Text(description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(previewColor, CircleShape)
                )
                Text(hexString, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Slider(
            value = hue,
            onValueChange = { newHue ->
                hue = newHue
            },
            onValueChangeFinished = {
                val newColorInt = android.graphics.Color.HSVToColor(floatArrayOf(hue, 0.85f, 0.9f))
                onColorChanged(newColorInt.toLong() and 0xFFFFFFFFL)
            },
            valueRange = 0f..360f,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

fun exportData(context: Context, history: List<com.example.data.db.HistoryItem>, favorites: List<com.example.data.db.FavoriteItem>, format: String) {
    if (history.isEmpty() && favorites.isEmpty()) {
        Toast.makeText(context, "Nothing to export", Toast.LENGTH_SHORT).show()
        return
    }
    
    val sb = StringBuilder()
    
    when (format) {
        "TXT" -> {
            sb.append("--- FAVORITES ---\n")
            favorites.forEach { sb.append("[${it.type}] ${it.content}\n") }
            sb.append("\n--- HISTORY ---\n")
            history.forEach { sb.append("[${it.type}] ${it.content}\n") }
        }
        "CSV" -> {
            sb.append("Category,Type,Content\n")
            favorites.forEach { sb.append("Favorite,${it.type},\"${it.content.replace("\"", "\"\"")}\"\n") }
            history.forEach { sb.append("History,${it.type},\"${it.content.replace("\"", "\"\"")}\"\n") }
        }
        "JSON" -> {
            sb.append("{\n  \"favorites\": [\n")
            favorites.forEachIndexed { idx, it ->
                sb.append("    { \"type\": \"${it.type}\", \"content\": \"${it.content.replace("\"", "\\\"").replace("\n", "\\n")}\" }")
                if (idx < favorites.size - 1) sb.append(",")
                sb.append("\n")
            }
            sb.append("  ],\n  \"history\": [\n")
            history.forEachIndexed { idx, it ->
                sb.append("    { \"type\": \"${it.type}\", \"content\": \"${it.content.replace("\"", "\\\"").replace("\n", "\\n")}\" }")
                if (idx < history.size - 1) sb.append(",")
                sb.append("\n")
            }
            sb.append("  ]\n}")
        }
    }
    
    val data = sb.toString()
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "${com.example.utils.AppConstants.APP_NAME} Export ($format)")
        putExtra(Intent.EXTRA_TEXT, data)
    }
    context.startActivity(Intent.createChooser(intent, "Export Data"))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorWheelPickerDialog(
    initialColorLong: Long,
    onDismissRequest: () -> Unit,
    onConfirm: (Long) -> Unit,
    onLiveColorUpdate: (Long) -> Unit
) {
    val initialColorInt = initialColorLong.toInt()
    val initialHsv = FloatArray(3)
    android.graphics.Color.colorToHSV(initialColorInt, initialHsv)
    val initialAlpha = (android.graphics.Color.alpha(initialColorInt) / 255f)

    var hue by remember { mutableStateOf(initialHsv[0]) }
    var saturation by remember { mutableStateOf(initialHsv[1]) }
    var value by remember { mutableStateOf(initialHsv[2]) }
    var alpha by remember { mutableStateOf(initialAlpha) }

    val currentSelectedColor = remember(hue, saturation, value, alpha) {
        val alphaInt = (alpha * 255f).toInt().coerceIn(0, 255)
        val colorInt = android.graphics.Color.HSVToColor(alphaInt, floatArrayOf(hue, saturation, value))
        Color(colorInt)
    }

    LaunchedEffect(currentSelectedColor) {
        onLiveColorUpdate(currentSelectedColor.toArgb().toLong() and 0xFFFFFFFFL)
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Dynamic Color Wheel",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                IconButton(
                    onClick = {
                        com.example.utils.SoundEffectManager.playClick()
                        onConfirm(currentSelectedColor.toArgb().toLong() and 0xFFFFFFFFL)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Confirm Theme Color",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Drag on the wheel to pick custom Hue. Adjust Saturation, Brightness, and Opacity below.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(220.dp)
                        .padding(8.dp)
                ) {
                    val colors = listOf(
                        Color.Red,
                        Color.Yellow,
                        Color.Green,
                        Color.Cyan,
                        Color.Blue,
                        Color.Magenta,
                        Color.Red
                    )

                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(Unit) {
                                detectDragGestures(
                                    onDragStart = { offset ->
                                        val centerX = size.width / 2f
                                        val centerY = size.height / 2f
                                        val dx = offset.x - centerX
                                        val dy = offset.y - centerY
                                        val angleRad = atan2(dy, dx)
                                        var degrees = Math.toDegrees(angleRad.toDouble()).toFloat()
                                        if (degrees < 0f) degrees += 360f
                                        hue = degrees
                                    },
                                    onDrag = { change, _ ->
                                        change.consume()
                                        val offset = change.position
                                        val centerX = size.width / 2f
                                        val centerY = size.height / 2f
                                        val dx = offset.x - centerX
                                        val dy = offset.y - centerY
                                        val angleRad = atan2(dy, dx)
                                        var degrees = Math.toDegrees(angleRad.toDouble()).toFloat()
                                        if (degrees < 0f) degrees += 360f
                                        hue = degrees
                                    }
                                )
                            }
                            .pointerInput(Unit) {
                                detectTapGestures { offset ->
                                    val centerX = size.width / 2f
                                    val centerY = size.height / 2f
                                    val dx = offset.x - centerX
                                    val dy = offset.y - centerY
                                    val angleRad = atan2(dy, dx)
                                    var degrees = Math.toDegrees(angleRad.toDouble()).toFloat()
                                    if (degrees < 0f) degrees += 360f
                                    hue = degrees
                                }
                            }
                    ) {
                        val strokeWidth = 24.dp.toPx()
                        val outerRadius = size.width / 2f
                        val wheelRadius = outerRadius - strokeWidth / 2f

                        drawCircle(
                            brush = Brush.sweepGradient(colors),
                            radius = wheelRadius,
                            style = Stroke(width = strokeWidth)
                        )

                        val angleRad = Math.toRadians(hue.toDouble())
                        val handleX = (size.width / 2f) + cos(angleRad).toFloat() * wheelRadius
                        val handleY = (size.height / 2f) + sin(angleRad).toFloat() * wheelRadius

                        drawCircle(
                            color = Color.White,
                            radius = 12.dp.toPx(),
                            center = Offset(handleX, handleY),
                            style = Stroke(width = 3.dp.toPx())
                        )
                        drawCircle(
                            color = Color.Black,
                            radius = 10.dp.toPx(),
                            center = Offset(handleX, handleY),
                            style = Stroke(width = 1.dp.toPx())
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(currentSelectedColor)
                                .border(2.dp, Color.White, CircleShape)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${hue.toInt()}°",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = String.format("#%08X", currentSelectedColor.toArgb()),
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                val saturationBrush = remember(hue, value) {
                    val alphaInt = (alpha * 255f).toInt().coerceIn(0, 255)
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(android.graphics.Color.HSVToColor(alphaInt, floatArrayOf(hue, 0f, value))),
                            Color(android.graphics.Color.HSVToColor(alphaInt, floatArrayOf(hue, 1f, value)))
                        )
                    )
                }
                GradientSlider(
                    value = saturation,
                    onValueChange = { saturation = it },
                    trackBrush = saturationBrush,
                    label = "Saturation",
                    valueText = "${(saturation * 100).toInt()}%"
                )

                val valueBrush = remember(hue, saturation) {
                    val alphaInt = (alpha * 255f).toInt().coerceIn(0, 255)
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(android.graphics.Color.HSVToColor(alphaInt, floatArrayOf(hue, saturation, 0f))),
                            Color(android.graphics.Color.HSVToColor(alphaInt, floatArrayOf(hue, saturation, 1f)))
                        )
                    )
                }
                GradientSlider(
                    value = value,
                    onValueChange = { value = it },
                    trackBrush = valueBrush,
                    label = "Brightness (Value)",
                    valueText = "${(value * 100).toInt()}%"
                )

                val alphaBrush = remember(hue, saturation, value) {
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(android.graphics.Color.HSVToColor(0, floatArrayOf(hue, saturation, value))),
                            Color(android.graphics.Color.HSVToColor(255, floatArrayOf(hue, saturation, value)))
                        )
                    )
                }
                GradientSlider(
                    value = alpha,
                    onValueChange = { alpha = it },
                    trackBrush = alphaBrush,
                    label = "Opacity (Alpha)",
                    valueText = "${(alpha * 100).toInt()}%"
                )

                Text(
                    text = "Preset Swatches",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Start).padding(top = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val quickPresets = listOf(
                        Pair("Purple", 0xFF6C63FFL),
                        Pair("Blue", 0xFF1E88E5L),
                        Pair("Emerald", 0xFF00C853L),
                        Pair("Crimson", 0xFFD50000L),
                        Pair("Amber", 0xFFFFAB00L),
                        Pair("Cyan", 0xFF00838FL)
                    )
                    quickPresets.forEach { (name, colorValue) ->
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(colorValue.toInt()))
                                .clickable {
                                    com.example.utils.SoundEffectManager.playClick()
                                    val presetColorInt = colorValue.toInt()
                                    val presetHsv = FloatArray(3)
                                    android.graphics.Color.colorToHSV(presetColorInt, presetHsv)
                                    hue = presetHsv[0]
                                    saturation = presetHsv[1]
                                    value = presetHsv[2]
                                    alpha = (android.graphics.Color.alpha(presetColorInt) / 255f)
                                }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    com.example.utils.SoundEffectManager.playClick()
                    onConfirm(currentSelectedColor.toArgb().toLong() and 0xFFFFFFFFL)
                }
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    com.example.utils.SoundEffectManager.playClick()
                    onDismissRequest()
                }
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun GradientSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    trackBrush: Brush,
    label: String,
    valueText: String
) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Text(valueText, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(modifier = Modifier.height(4.dp))
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(16.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(trackBrush)
        ) {
            val maxW = constraints.maxWidth.toFloat()
            val density = LocalDensity.current
            val thumbSizePx = with(density) { 16.dp.toPx() }
            val thumbOffset = value * (maxW - thumbSizePx)

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            val newValue = (offset.x / maxW).coerceIn(0f, 1f)
                            onValueChange(newValue)
                        }
                    }
                    .pointerInput(Unit) {
                        detectDragGestures { change, _ ->
                            change.consume()
                            val newValue = (change.position.x / maxW).coerceIn(0f, 1f)
                            onValueChange(newValue)
                        }
                    }
            ) {
                Box(
                    modifier = Modifier
                        .offset(x = with(density) { thumbOffset.toDp() })
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                )
            }
        }
    }
}
