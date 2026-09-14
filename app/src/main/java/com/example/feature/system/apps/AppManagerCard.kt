package com.example.feature.system.apps

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.feature.system.apps.capability.AppManagerCapability
import com.example.feature.system.apps.model.AppItem
import com.example.feature.system.apps.revert.RevertSheet
import com.example.ui.navigation.Screen
import com.example.ui.screens.SystemFeatureCard
import com.example.utils.SoundEffectManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

enum class AppFilter {
    ALL,
    USER,
    DISABLED,
    SYSTEM
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppManagerCard(
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: AppManagerViewModel = viewModel()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val capability by viewModel.capability.collectAsStateWithLifecycle()
    val installedApps by viewModel.installedApps.collectAsStateWithLifecycle()
    val disabledApps by viewModel.disabledApps.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val isReverting by viewModel.isReverting.collectAsStateWithLifecycle()
    val revertProgressCount by viewModel.revertProgressCount.collectAsStateWithLifecycle()
    val revertTotalCount by viewModel.revertTotalCount.collectAsStateWithLifecycle()
    val lastRevertResult by viewModel.lastRevertResult.collectAsStateWithLifecycle()
    val historyCount by viewModel.historyCount.collectAsStateWithLifecycle()

    var showRevertSheet by remember { mutableStateOf(false) }
    var showPrivilegeDialog by remember { mutableStateOf(false) }
    var privilegeDialogPackage by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(AppFilter.ALL) }

    LaunchedEffect(Unit) {
        viewModel.snackbarMessage.collectLatest { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    LaunchedEffect(isExpanded) {
        if (isExpanded) {
            viewModel.onVisible()
        }
    }

    val filteredApps = remember(installedApps, searchQuery, selectedFilter) {
        var list = when (selectedFilter) {
            AppFilter.ALL -> installedApps
            AppFilter.USER -> installedApps.filter { !it.isSystemApp }
            AppFilter.DISABLED -> installedApps.filter { !it.isEnabled }
            AppFilter.SYSTEM -> installedApps.filter { it.isSystemApp }
        }
        if (searchQuery.isNotBlank()) {
            list = list.filter {
                it.appName.contains(searchQuery, ignoreCase = true) ||
                it.packageName.contains(searchQuery, ignoreCase = true)
            }
        }
        list
    }

    SystemFeatureCard(
        title = "App Manager & History",
        subtitle = "${installedApps.size} Installed Apps • $historyCount History Logs",
        icon = Icons.Default.Apps,
        badge = if (disabledApps.isNotEmpty()) "${disabledApps.size} Disabled" else "${installedApps.size} Apps",
        badgeColor = if (disabledApps.isNotEmpty()) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
        isExpanded = isExpanded,
        onToggleExpand = onToggleExpand
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            SnackbarHost(hostState = snackbarHostState)

            // Capability Banner
            Surface(
                color = when (capability) {
                    AppManagerCapability.SHIZUKU -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    AppManagerCapability.DEVICE_OWNER -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
                    AppManagerCapability.NONE -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                },
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = when (capability) {
                                AppManagerCapability.SHIZUKU, AppManagerCapability.DEVICE_OWNER -> Icons.Default.VerifiedUser
                                AppManagerCapability.NONE -> Icons.Default.Info
                            },
                            contentDescription = null,
                            tint = when (capability) {
                                AppManagerCapability.SHIZUKU -> MaterialTheme.colorScheme.primary
                                AppManagerCapability.DEVICE_OWNER -> MaterialTheme.colorScheme.tertiary
                                AppManagerCapability.NONE -> MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = when (capability) {
                                    AppManagerCapability.SHIZUKU -> "Privilege: Shizuku Active"
                                    AppManagerCapability.DEVICE_OWNER -> "Privilege: Device Owner"
                                    AppManagerCapability.NONE -> "Privilege: Standard Mode"
                                },
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = when (capability) {
                                    AppManagerCapability.SHIZUKU -> "Silent app enable/disable via PM shell"
                                    AppManagerCapability.DEVICE_OWNER -> "Direct policy hide/unhide control"
                                    AppManagerCapability.NONE -> "Requires Shizuku or Device Owner to toggle"
                                },
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (capability == AppManagerCapability.NONE) {
                        FilledTonalButton(
                            onClick = {
                                SoundEffectManager.playClick()
                                viewModel.requestShizukuPermission()
                            },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text("Connect", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Row: Revert App Settings & Refresh
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        SoundEffectManager.playClick()
                        showRevertSheet = true
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Revert App Settings", style = MaterialTheme.typography.labelMedium)
                }

                OutlinedButton(
                    onClick = {
                        SoundEffectManager.playClick()
                        viewModel.loadApps()
                    },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh", modifier = Modifier.size(18.dp))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search apps by name or package...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Filter Chips Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = selectedFilter == AppFilter.ALL,
                    onClick = { selectedFilter = AppFilter.ALL },
                    label = { Text("All (${installedApps.size})", fontSize = 11.sp) }
                )
                FilterChip(
                    selected = selectedFilter == AppFilter.USER,
                    onClick = { selectedFilter = AppFilter.USER },
                    label = { Text("User", fontSize = 11.sp) }
                )
                FilterChip(
                    selected = selectedFilter == AppFilter.DISABLED,
                    onClick = { selectedFilter = AppFilter.DISABLED },
                    label = { Text("Disabled (${disabledApps.size})", fontSize = 11.sp) }
                )
                FilterChip(
                    selected = selectedFilter == AppFilter.SYSTEM,
                    onClick = { selectedFilter = AppFilter.SYSTEM },
                    label = { Text("System", fontSize = 11.sp) }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(12.dp))
            }

            // App list preview (show top 5 or filtered list)
            val displayList = filteredApps.take(15)

            if (displayList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (searchQuery.isBlank()) "No apps found." else "No apps matching \"$searchQuery\"",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    displayList.forEach { app ->
                        AppRowItem(
                            app = app,
                            capability = capability,
                            onToggle = {
                                if (capability == AppManagerCapability.NONE) {
                                    privilegeDialogPackage = app.packageName
                                    showPrivilegeDialog = true
                                } else {
                                    viewModel.toggleAppEnabled(app)
                                }
                            },
                            onUninstall = { viewModel.uninstallApp(app) },
                            onAppInfo = {
                                com.example.feature.system.apps.actions.AppActions.openAppInfo(app.packageName, context)
                            },
                            onLaunch = {
                                val launched = com.example.feature.system.apps.actions.AppActions.launchApp(app.packageName, context)
                                if (!launched) {
                                    Toast.makeText(context, "Cannot launch ${app.appName}", Toast.LENGTH_SHORT).show()
                                }
                            },
                            onCopyPackage = {
                                val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                cm.setPrimaryClip(ClipData.newPlainText("Package Name", app.packageName))
                                Toast.makeText(context, "Package copied: ${app.packageName}", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }

                if (filteredApps.size > displayList.size) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "+ ${filteredApps.size - displayList.size} more apps (open full manager below)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = {
                    SoundEffectManager.playClick()
                    navController.navigate(Screen.AppList.route)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Open Complete App Manager & History")
            }
        }
    }

    // Revert Sheet
    if (showRevertSheet) {
        RevertSheet(
            disabledApps = disabledApps,
            isReverting = isReverting,
            revertProgressCount = revertProgressCount,
            revertTotalCount = revertTotalCount,
            lastRevertResult = lastRevertResult,
            onEnableAll = { viewModel.revertAllDisabled() },
            onEnableSelected = { selected -> viewModel.revertSelected(selected) },
            onDismiss = { showRevertSheet = false }
        )
    }

    // Privilege Required Dialog
    if (showPrivilegeDialog) {
        AlertDialog(
            onDismissRequest = { showPrivilegeDialog = false },
            title = { Text("Privilege Required", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Android does not permit third-party applications to silently enable or disable other packages without elevated permissions.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        "You can enable Shizuku (adb) or configure Device Owner for 1-tap silent control, or open System App Info to toggle manually.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showPrivilegeDialog = false
                        privilegeDialogPackage?.let {
                            com.example.feature.system.apps.actions.AppActions.openAppInfo(it, context)
                        }
                    }
                ) {
                    Text("Open App Info")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showPrivilegeDialog = false
                        viewModel.requestShizukuPermission()
                    }
                ) {
                    Text("Connect Shizuku")
                }
            }
        )
    }
}

@Composable
private fun AppRowItem(
    app: AppItem,
    capability: AppManagerCapability,
    onToggle: () -> Unit,
    onUninstall: () -> Unit,
    onAppInfo: () -> Unit,
    onLaunch: () -> Unit,
    onCopyPackage: () -> Unit
) {
    var showOverflowMenu by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable { onCopyPackage() },
        shape = RoundedCornerShape(10.dp),
        color = if (!app.isEnabled) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f)
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        border = BorderStroke(
            1.dp,
            if (!app.isEnabled) MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
            else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            if (app.icon != null) {
                Image(
                    bitmap = app.icon.toBitmap(96, 96).asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.secondaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = app.appName.take(1).uppercase(),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = app.appName,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1
                    )
                    if (app.isSafeBlocklisted) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = "Protected",
                            modifier = Modifier.size(13.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Text(
                    text = app.packageName,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(3.dp))

                // Badges
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BadgeChip(
                        text = "v${app.versionName} (${app.versionCode})",
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                    if (app.isSystemApp) {
                        BadgeChip(
                            text = "System",
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            textColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                    if (!app.isEnabled) {
                        BadgeChip(
                            text = "Disabled",
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            textColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Trailing actions: Toggle + Overflow
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (app.isSafeBlocklisted) {
                    // Safe blocklisted apps cannot be disabled
                    IconButton(onClick = {}, enabled = false, modifier = Modifier.size(36.dp)) {
                        Icon(
                            Icons.Default.Security,
                            contentDescription = "Protected from disabling",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                } else {
                    Switch(
                        checked = app.isEnabled,
                        onCheckedChange = { onToggle() },
                        thumbContent = if (!app.isEnabled) {
                            { Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(12.dp)) }
                        } else null,
                        modifier = Modifier.scale(0.85f)
                    )
                }

                Box {
                    IconButton(
                        onClick = {
                            SoundEffectManager.playClick()
                            showOverflowMenu = true
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "Actions",
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showOverflowMenu,
                        onDismissRequest = { showOverflowMenu = false }
                    ) {
                        if (app.isLaunchable) {
                            DropdownMenuItem(
                                text = { Text("Launch") },
                                leadingIcon = { Icon(Icons.Default.PlayArrow, contentDescription = null) },
                                onClick = {
                                    showOverflowMenu = false
                                    onLaunch()
                                }
                            )
                        }

                        DropdownMenuItem(
                            text = { Text("App Info") },
                            leadingIcon = { Icon(Icons.Default.Info, contentDescription = null) },
                            onClick = {
                                showOverflowMenu = false
                                onAppInfo()
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Copy Package Name") },
                            leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null) },
                            onClick = {
                                showOverflowMenu = false
                                onCopyPackage()
                            }
                        )

                        HorizontalDivider()

                        DropdownMenuItem(
                            text = {
                                Text(
                                    "Uninstall",
                                    color = if (app.isSystemApp && app.isSafeBlocklisted) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                            else MaterialTheme.colorScheme.error
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = if (app.isSystemApp && app.isSafeBlocklisted) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                           else MaterialTheme.colorScheme.error
                                )
                            },
                            onClick = {
                                showOverflowMenu = false
                                onUninstall()
                            },
                            enabled = !(app.isSystemApp && app.isSafeBlocklisted)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BadgeChip(
    text: String,
    containerColor: Color,
    textColor: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    Surface(
        color = containerColor,
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
            color = textColor,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
        )
    }
}
