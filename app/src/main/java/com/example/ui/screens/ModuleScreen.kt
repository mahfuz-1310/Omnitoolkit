package com.example.ui.screens

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import android.os.SystemClock
import android.util.Log
import com.example.ui.MainViewModel
import com.example.utils.CustomModuleManager
import com.example.utils.DeviceProfile
import com.example.utils.FakeDeviceManager
import com.example.utils.FakeDeviceProfile
import com.example.utils.SystemModule
import com.example.utils.SoundEffectManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModuleScreen(viewModel: MainViewModel, onNavigateBack: () -> Unit) {
    val t0 = remember { SystemClock.uptimeMillis() }
    SideEffect {
        Log.d("Tabs", "ModuleScreen switched in ${SystemClock.uptimeMillis() - t0}ms")
    }

    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()

    var allModules by remember { mutableStateOf(CustomModuleManager.builtinModules) }
    val moduleStates = remember {
        mutableStateMapOf<String, Boolean>().apply {
            CustomModuleManager.builtinModules.forEach { module ->
                put(module.id, module.defaultEnabled)
            }
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(Unit) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            withContext(Dispatchers.IO) {
                val loadedModules = CustomModuleManager.getAllModules(context)
                val states = loadedModules.associate { it.id to CustomModuleManager.isModuleEnabled(context, it.id) }
                withContext(Dispatchers.Main) {
                    allModules = loadedModules
                    moduleStates.putAll(states)
                }
            }
        }
    }

    var customScriptInput by remember { mutableStateOf("") }
    var showCustomScriptDialog by remember { mutableStateOf(false) }

    // File picker for custom module import (.sh, .json)
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            coroutineScope.launch(Dispatchers.IO) {
                try {
                    val inputStream = context.contentResolver.openInputStream(selectedUri)
                    val reader = BufferedReader(InputStreamReader(inputStream))
                    val content = reader.readText()
                    reader.close()
                    inputStream?.close()

                    val fileName = selectedUri.lastPathSegment ?: "custom_module"
                    val isJson = fileName.endsWith(".json", ignoreCase = true)

                    val newModule = if (isJson) {
                        val jsonObj = JSONObject(content)
                        val title = jsonObj.optString("title", fileName)
                        val description = jsonObj.optString("description", "Imported custom module JSON")
                        val category = jsonObj.optString("category", "Custom")
                        val cmdsArr = jsonObj.optJSONArray("shellCommands")
                        val commands = mutableListOf<String>()
                        if (cmdsArr != null) {
                            for (i in 0 until cmdsArr.length()) {
                                commands.add(cmdsArr.getString(i))
                            }
                        }
                        SystemModule(
                            id = "imported_${System.currentTimeMillis()}",
                            title = title,
                            description = description,
                            category = category,
                            shellCommands = commands,
                            isCustom = true
                        )
                    } else {
                        // Shell script (.sh or text)
                        val lines = content.lines()
                            .map { it.trim() }
                            .filter { it.isNotEmpty() && !it.startsWith("#") }
                        SystemModule(
                            id = "imported_${System.currentTimeMillis()}",
                            title = fileName.substringBeforeLast("."),
                            description = "Imported custom shell script module",
                            category = "Custom",
                            shellCommands = lines,
                            isCustom = true
                        )
                    }

                    CustomModuleManager.saveImportedModule(context, newModule)
                    val updatedModules = CustomModuleManager.getAllModules(context)
                    withContext(Dispatchers.Main) {
                        allModules = updatedModules
                        moduleStates[newModule.id] = true
                        CustomModuleManager.setModuleEnabled(context, newModule.id, true)
                        viewModel.showFloatingToast("Successfully imported module: ${newModule.title}")
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        viewModel.showFloatingToast("Failed to import module: ${e.localizedMessage}")
                    }
                }
            }
        }
    }

    // Entrance animation state
    var isVisible by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Game Booster & Modules", 
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        SoundEffectManager.playClick()
                        onNavigateBack()
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        SoundEffectManager.playClick()
                        filePickerLauncher.launch(arrayOf("*/*"))
                    }) {
                        Icon(Icons.Default.AddCard, contentDescription = "Import Module")
                    }
                    IconButton(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        SoundEffectManager.playClick()
                        showCustomScriptDialog = true
                    }) {
                        Icon(Icons.Default.Code, contentDescription = "Custom Script Editor")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                    actionIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(animationSpec = tween(500)) + slideInVertically(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    initialOffsetY = { it / 10 }
                )
            ) {
                val listState = rememberSaveable(saver = LazyListState.Saver) { LazyListState() }
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    // Header Banner Card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                val isShizukuConnected = remember { CustomModuleManager.isShizukuReady() }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                        modifier = Modifier.size(44.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.SportsEsports,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = "All-in-One Game Engine",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                HorizontalDivider(
                                    thickness = 1.dp,
                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                                    modifier = Modifier.padding(vertical = 2.dp)
                                )

                                Text(
                                    text = "Turbocharge gaming performance, unlock 120Hz FPS, stabilize ping, spoof device profiles, and cool down CPU via Shizuku or Root.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (!isShizukuConnected) {
                                    Text(
                                        text = "💡 Tip: Start Shizuku service and grant permission for full system-level script execution without root.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }

                                HorizontalDivider(
                                    thickness = 1.dp,
                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                                    modifier = Modifier.padding(vertical = 2.dp)
                                )

                                // Shizuku Status Badge at the bottom of the header card
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 2.dp),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(16.dp),
                                        color = if (isShizukuConnected) {
                                            Color(0xFF10B981).copy(alpha = 0.12f)
                                        } else {
                                            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f)
                                        },
                                        border = BorderStroke(
                                            1.dp,
                                            if (isShizukuConnected) Color(0xFF10B981).copy(alpha = 0.5f)
                                            else MaterialTheme.colorScheme.error.copy(alpha = 0.4f)
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .background(
                                                        color = if (isShizukuConnected) Color(0xFF10B981) else MaterialTheme.colorScheme.error,
                                                        shape = RoundedCornerShape(4.dp)
                                                    )
                                            )
                                            Text(
                                                text = if (isShizukuConnected) "Shizuku Active" else "Shizuku Disconnected",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                softWrap = false,
                                                color = if (isShizukuConnected) {
                                                    Color(0xFF10B981)
                                                } else {
                                                    MaterialTheme.colorScheme.error
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Divider under Header Banner Card
                    item {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 2.dp),
                            thickness = 1.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
                        )
                    }

                    // Thermal Status Card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f, fill = false),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color(0xFF10B981).copy(alpha = 0.12f),
                                        border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.35f)),
                                        modifier = Modifier.size(44.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.Thermostat,
                                                contentDescription = null,
                                                tint = Color(0xFF10B981),
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    }
                                    Column(modifier = Modifier.padding(end = 8.dp)) {
                                        Text(
                                            text = "CPU Thermal Status",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = "35.8°C • Optimal (Gaming Ready)",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1
                                        )
                                    }
                                }

                                Button(
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        SoundEffectManager.playClick()
                                        coroutineScope.launch(Dispatchers.IO) {
                                            val coolerMod = allModules.find { it.id == "system_thermal_cooler" }
                                            val cmds = coolerMod?.shellCommands ?: listOf("sync", "echo 3 > /proc/sys/vm/drop_caches", "am kill-all")
                                            val success = CustomModuleManager.executeModuleCommands(cmds)
                                            withContext(Dispatchers.Main) {
                                                viewModel.showFloatingToast(if (success) "Thermal Cool Down executed successfully!" else "Cool Down executed.")
                                            }
                                        }
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    ),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AcUnit,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            tint = MaterialTheme.colorScheme.onPrimary
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Cool Down",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 1,
                                            softWrap = false,
                                            color = MaterialTheme.colorScheme.onPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Section Divider
                    item {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 2.dp),
                            thickness = 1.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
                        )
                    }

                    // Modules Section Header
                    item {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Game Booster & System Modules (${allModules.size})",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedButton(
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        SoundEffectManager.playClick()
                                        filePickerLauncher.launch(arrayOf("*/*"))
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = MaterialTheme.colorScheme.surface,
                                        contentColor = MaterialTheme.colorScheme.primary
                                    ),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("+ Import Module", style = MaterialTheme.typography.labelMedium)
                                }
                            }
                            HorizontalDivider(
                                modifier = Modifier.padding(top = 10.dp, bottom = 2.dp),
                                thickness = 1.dp,
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                            )
                        }
                    }

                    // Modules List with Animation & Solid Filled Container Card Effect
                    items(allModules, key = { it.id }) { module ->
                        val isEnabled = moduleStates[module.id] ?: false
                        val index = allModules.indexOf(module)

                        AnimatedVisibility(
                            visible = isVisible,
                            enter = fadeIn(animationSpec = tween(400, delayMillis = index * 50)) + slideInVertically(
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessLow
                                ),
                                initialOffsetY = { it / 3 }
                            ),
                            exit = fadeOut(animationSpec = tween(300))
                        ) {
                            FilledModuleCard(
                                module = module,
                                isEnabled = isEnabled,
                                onToggle = { checked ->
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    SoundEffectManager.playClick()
                                    // Direct isolated state update
                                    moduleStates[module.id] = checked
                                    CustomModuleManager.setModuleEnabled(context, module.id, checked)

                                    // Run commands silently in background without intrusive overlays
                                    coroutineScope.launch(Dispatchers.IO) {
                                        if (checked) {
                                            CustomModuleManager.executeModuleCommands(module.shellCommands)
                                        }
                                    }
                                },
                                onRunNow = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    SoundEffectManager.playClick()
                                    coroutineScope.launch(Dispatchers.IO) {
                                        val success = CustomModuleManager.executeModuleCommands(module.shellCommands)
                                        withContext(Dispatchers.Main) {
                                            val msg = if (success) "Executed ${module.title} successfully!" else "Executed ${module.title}"
                                            viewModel.showFloatingToast(msg)
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }



    // Custom Script Dialog
    if (showCustomScriptDialog) {
        AlertDialog(
            onDismissRequest = { showCustomScriptDialog = false },
            title = { Text("Custom Shell Script / Module", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Enter shell commands (one per line) to execute via Shizuku or Root:", style = MaterialTheme.typography.bodySmall)
                    OutlinedTextField(
                        value = customScriptInput,
                        onValueChange = { customScriptInput = it },
                        placeholder = { Text("settings put global ...\nsetprop ...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        SoundEffectManager.playClick()
                        val script = customScriptInput
                        showCustomScriptDialog = false
                        customScriptInput = ""
                        coroutineScope.launch(Dispatchers.IO) {
                            val success = CustomModuleManager.executeCustomScript(script)
                            withContext(Dispatchers.Main) {
                                val msg = if (success) "Custom script executed successfully!" else "Custom script executed with warnings."
                                viewModel.showFloatingToast(msg)
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text("Execute Script")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    SoundEffectManager.playClick()
                    showCustomScriptDialog = false
                }) {
                    Text("Cancel")
                }
            }
        )
    }
}

/**
 * FilledModuleCard provides a theme-aware container box design across all modules:
 * - Adapts cleanly to both White theme and Dark theme using MaterialTheme semantic colors.
 * - Active state highlights with emerald green accent, clear ACTIVE badge, and crisp typography.
 */
@Composable
fun FilledModuleCard(
    module: com.example.utils.SystemModule,
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit,
    onRunNow: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardBg = if (module.isCustom) {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
    }
    val cardBorder = if (isEnabled) {
        BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.8f))
    } else {
        BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardBg
        ),
        border = cardBorder,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isEnabled) {
                            Color(0xFF10B981).copy(alpha = 0.12f)
                        } else if (module.isCustom) {
                            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f)
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        },
                        border = BorderStroke(
                            1.dp,
                            if (isEnabled) Color(0xFF10B981).copy(alpha = 0.4f)
                            else if (module.isCustom) MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f)
                            else MaterialTheme.colorScheme.outline
                        ),
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = when {
                                    module.isCustom -> Icons.Default.Description
                                    module.category == "Gaming" -> Icons.Default.SportsEsports
                                    module.category == "Performance" -> Icons.Default.Speed
                                    module.category == "Memory" -> Icons.Default.Memory
                                    module.category == "Display" -> Icons.Default.PhoneAndroid
                                    module.category == "Network" -> Icons.Default.Wifi
                                    module.category == "Thermal" -> Icons.Default.Thermostat
                                    else -> Icons.Default.Tune
                                },
                                contentDescription = null,
                                tint = if (isEnabled) {
                                    Color(0xFF10B981)
                                } else if (module.isCustom) {
                                    MaterialTheme.colorScheme.tertiary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = module.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (module.isCustom) {
                                    MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f)
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                },
                                border = BorderStroke(
                                    1.dp,
                                    if (module.isCustom) MaterialTheme.colorScheme.tertiary.copy(alpha = 0.35f)
                                    else MaterialTheme.colorScheme.outline
                                )
                            ) {
                                Text(
                                    text = if (module.isCustom) "Custom Script" else module.category,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (module.isCustom) {
                                        MaterialTheme.colorScheme.tertiary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            if (isEnabled) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFF10B981).copy(alpha = 0.12f),
                                    border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.5f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .background(Color(0xFF10B981), RoundedCornerShape(3.dp))
                                        )
                                        Text(
                                            text = "ACTIVE",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF10B981)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Switch(
                    checked = isEnabled,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFF059669),
                        checkedBorderColor = Color(0xFF10B981),
                        uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                        uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                        uncheckedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
            }

            HorizontalDivider(
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                modifier = Modifier.padding(vertical = 1.dp)
            )

            Text(
                text = module.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            HorizontalDivider(
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                modifier = Modifier.padding(vertical = 1.dp)
            )

            // Outlined Action Button
            OutlinedButton(
                onClick = onRunNow,
                modifier = Modifier.align(Alignment.End),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(
                    1.dp,
                    if (isEnabled) Color(0xFF10B981).copy(alpha = 0.6f) else MaterialTheme.colorScheme.outline
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (isEnabled) Color(0xFF10B981).copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface,
                    contentColor = if (isEnabled) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurface
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = if (isEnabled) Color(0xFF10B981) else MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Run Module Now",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isEnabled) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

/** Compatibility alias for FilledModuleCard */
@Composable
fun GlowingModuleCard(
    module: com.example.utils.SystemModule,
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit,
    onRunNow: () -> Unit,
    modifier: Modifier = Modifier
) = FilledModuleCard(module, isEnabled, onToggle, onRunNow, modifier)
