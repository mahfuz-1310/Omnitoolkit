package com.example.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Animation
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.utils.AnimationTweakManager
import com.example.utils.RefreshRateManager
import com.example.utils.SoundEffectManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimationSettingsScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var currentScale by remember { mutableStateOf(AnimationTweakManager.getCurrentAnimationScale(context)) }
    var currentPeakRate by remember { mutableStateOf(RefreshRateManager.getCurrentPeakRefreshRate(context)) }
    var currentMinRate by remember { mutableStateOf(RefreshRateManager.getCurrentMinRefreshRate(context)) }

    var isShizukuRunning by remember { mutableStateOf(AnimationTweakManager.isShizukuAvailable() || RefreshRateManager.isShizukuAvailable()) }
    var hasShizukuPerm by remember { mutableStateOf(AnimationTweakManager.hasShizukuPermission() || RefreshRateManager.hasShizukuPermission()) }
    var showPermissionDialog by remember { mutableStateOf(false) }

    // Custom peak & min slider states
    var customPeak by remember { mutableStateOf(currentPeakRate) }
    var customMin by remember { mutableStateOf(currentMinRate) }

    LaunchedEffect(Unit) {
        val shizAvailable = AnimationTweakManager.isShizukuAvailable()
        val shizPerm = AnimationTweakManager.hasShizukuPermission()
        isShizukuRunning = shizAvailable
        hasShizukuPerm = shizPerm
        currentScale = AnimationTweakManager.getCurrentAnimationScale(context)
        currentPeakRate = RefreshRateManager.getCurrentPeakRefreshRate(context)
        currentMinRate = RefreshRateManager.getCurrentMinRefreshRate(context)
        customPeak = currentPeakRate
        customMin = currentMinRate
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Animation & Refresh Rate Tweak", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Status Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "System Status",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                )
                                Text(
                                    text = "Anim Scale: ${String.format("%.1f", currentScale)}x | Peak: ${currentPeakRate.toInt()}Hz",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (isShizukuRunning && hasShizukuPerm) {
                            "Shizuku ADB permission active. Changes apply instantly to system settings."
                        } else {
                            "Shizuku permission required for global ADB settings tuning."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )

                    if (!isShizukuRunning || !hasShizukuPerm) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                if (!isShizukuRunning) {
                                    showPermissionDialog = true
                                } else {
                                    AnimationTweakManager.requestShizukuPermission(1002)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Grant Shizuku Permission")
                        }
                    }
                }
            }

            // Display Refresh Rate Manager Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Refresh, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Display Refresh Rate Manager",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "Current Peak Rate: ${currentPeakRate.toInt()} Hz | Min Rate: ${currentMinRate.toInt()} Hz",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Preset Chips Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = currentPeakRate == 60f && currentMinRate == 60f,
                            onClick = {
                                SoundEffectManager.playClick(context)
                                if (!isShizukuRunning || !hasShizukuPerm) {
                                    showPermissionDialog = true
                                    return@FilterChip
                                }
                                val success = RefreshRateManager.setRefreshRates(context, 60f, 60f)
                                if (success) {
                                    currentPeakRate = 60f
                                    currentMinRate = 60f
                                    customPeak = 60f
                                    customMin = 60f
                                    Toast.makeText(context, "Refresh Rate set to 60 Hz (Power Saving)", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Failed to apply refresh rate via Shizuku/ADB", Toast.LENGTH_SHORT).show()
                                }
                            },
                            label = { Text("60 Hz") },
                            modifier = Modifier.weight(1f)
                        )

                        FilterChip(
                            selected = currentPeakRate == 90f && currentMinRate == 90f,
                            onClick = {
                                SoundEffectManager.playClick(context)
                                if (!isShizukuRunning || !hasShizukuPerm) {
                                    showPermissionDialog = true
                                    return@FilterChip
                                }
                                val success = RefreshRateManager.setRefreshRates(context, 90f, 90f)
                                if (success) {
                                    currentPeakRate = 90f
                                    currentMinRate = 90f
                                    customPeak = 90f
                                    customMin = 90f
                                    Toast.makeText(context, "Refresh Rate set to 90 Hz (Smooth)", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Failed to apply refresh rate via Shizuku/ADB", Toast.LENGTH_SHORT).show()
                                }
                            },
                            label = { Text("90 Hz") },
                            modifier = Modifier.weight(1f)
                        )

                        FilterChip(
                            selected = currentPeakRate >= 120f,
                            onClick = {
                                SoundEffectManager.playClick(context)
                                if (!isShizukuRunning || !hasShizukuPerm) {
                                    showPermissionDialog = true
                                    return@FilterChip
                                }
                                val success = RefreshRateManager.setRefreshRates(context, 120f, 120f)
                                if (success) {
                                    currentPeakRate = 120f
                                    currentMinRate = 120f
                                    customPeak = 120f
                                    customMin = 120f
                                    Toast.makeText(context, "Refresh Rate set to 120 Hz (Ultra Smooth)", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Failed to apply refresh rate via Shizuku/ADB", Toast.LENGTH_SHORT).show()
                                }
                            },
                            label = { Text("120 Hz") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Auto / Dynamic Chip
                    OutlinedButton(
                        onClick = {
                            SoundEffectManager.playClick(context)
                            if (!isShizukuRunning || !hasShizukuPerm) {
                                showPermissionDialog = true
                                return@OutlinedButton
                            }
                            val success = RefreshRateManager.setRefreshRates(context, null, null)
                            if (success) {
                                currentPeakRate = RefreshRateManager.getCurrentPeakRefreshRate(context)
                                currentMinRate = RefreshRateManager.getCurrentMinRefreshRate(context)
                                customPeak = currentPeakRate
                                customMin = currentMinRate
                                Toast.makeText(context, "Restored Auto / Dynamic Stock Refresh Rate", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Failed to reset refresh rate", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Auto / Dynamic (Stock System Behavior)")
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Custom Peak Refresh Rate: ${customPeak.toInt()} Hz",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Slider(
                        value = customPeak,
                        onValueChange = { customPeak = it },
                        onValueChangeFinished = {
                            if (isShizukuRunning && hasShizukuPerm) {
                                RefreshRateManager.setRefreshRates(context, customPeak, customMin)
                                currentPeakRate = customPeak
                                Toast.makeText(context, "Peak Refresh Rate set to ${customPeak.toInt()} Hz", Toast.LENGTH_SHORT).show()
                            } else {
                                showPermissionDialog = true
                            }
                        },
                        valueRange = 60f..120f,
                        steps = 5
                    )
                }
            }

            // Animation Speed Presets Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Animation Speed Presets",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Current Scale: ${String.format("%.1f", currentScale)}x",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // 1. Super Fast (0.5x)
                    Button(
                        onClick = {
                            SoundEffectManager.playClick(context)
                            if (!isShizukuRunning || !hasShizukuPerm) {
                                showPermissionDialog = true
                                return@Button
                            }
                            val success = AnimationTweakManager.setAnimationScales(context, 0.5f)
                            if (success) {
                                currentScale = 0.5f
                                Toast.makeText(context, "Animations set to Super Fast (0.5x)", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Failed to apply animation scales.", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (currentScale == 0.5f) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.FlashOn, contentDescription = null, tint = if (currentScale == 0.5f) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Super Fast (0.5x)",
                            color = if (currentScale == 0.5f) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // 2. No Animation (0.0x)
                    Button(
                        onClick = {
                            SoundEffectManager.playClick(context)
                            if (!isShizukuRunning || !hasShizukuPerm) {
                                showPermissionDialog = true
                                return@Button
                            }
                            val success = AnimationTweakManager.setAnimationScales(context, 0.0f)
                            if (success) {
                                currentScale = 0.0f
                                Toast.makeText(context, "Animations disabled (0.0x)", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Failed to apply animation scales.", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (currentScale == 0.0f) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Animation, contentDescription = null, tint = if (currentScale == 0.0f) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "No Animation / Off (0.0x)",
                            color = if (currentScale == 0.0f) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // 3. Stock Default (1.0x)
                    Button(
                        onClick = {
                            SoundEffectManager.playClick(context)
                            if (!isShizukuRunning || !hasShizukuPerm) {
                                showPermissionDialog = true
                                return@Button
                            }
                            val success = AnimationTweakManager.setAnimationScales(context, 1.0f)
                            if (success) {
                                currentScale = 1.0f
                                Toast.makeText(context, "Animations reset to Stock Default (1.0x)", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Failed to apply animation scales.", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (currentScale == 1.0f) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = null, tint = if (currentScale == 1.0f) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Stock Default (1.0x)",
                            color = if (currentScale == 1.0f) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = { Text("Shizuku is Inactive") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Privileged actions are currently blocked because Shizuku is not active or authorized.", style = MaterialTheme.typography.bodyMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                    Text("1. Install and open the Shizuku app.", style = MaterialTheme.typography.bodySmall)
                    Text("2. Start the Shizuku service (via Wireless Debugging or root).", style = MaterialTheme.typography.bodySmall)
                    Text("3. Return here and tap 'Grant Permission'.", style = MaterialTheme.typography.bodySmall)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showPermissionDialog = false
                        try {
                            val intent = context.packageManager.getLaunchIntentForPackage("rikka.shizuku.manager")
                            if (intent != null) {
                                context.startActivity(intent)
                            } else {
                                android.widget.Toast.makeText(context, "Shizuku app not found", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            android.widget.Toast.makeText(context, "Could not open Shizuku", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text("Open Shizuku")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionDialog = false }) {
                    Text("Dismiss")
                }
            }
        )
    }
}
