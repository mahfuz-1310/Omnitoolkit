package com.example.feature.system.apps.revert

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.example.feature.system.apps.model.AppItem
import com.example.utils.SoundEffectManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RevertSheet(
    disabledApps: List<AppItem>,
    isReverting: Boolean,
    revertProgressCount: Int,
    revertTotalCount: Int,
    lastRevertResult: Pair<Int, Int>?, // successCount to failureCount
    onEnableAll: () -> Unit,
    onEnableSelected: (List<String>) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    val selectedPackages = remember { mutableStateListOf<String>() }

    val filteredDisabledApps = remember(disabledApps, searchQuery) {
        if (searchQuery.isBlank()) disabledApps
        else disabledApps.filter {
            it.appName.contains(searchQuery, ignoreCase = true) ||
            it.packageName.contains(searchQuery, ignoreCase = true)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Restore,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Revert App Settings",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${disabledApps.size} disabled apps found",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Tab Selector
            PrimaryTabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = {
                        SoundEffectManager.playClick()
                        selectedTab = 0
                    },
                    text = { Text("Enable All Disabled") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = {
                        SoundEffectManager.playClick()
                        selectedTab = 1
                    },
                    text = { Text("Select to Enable") }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Progress indicator when actively reverting
            AnimatedVisibility(visible = isReverting) {
                Column(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                    val progressFraction = if (revertTotalCount > 0) revertProgressCount.toFloat() / revertTotalCount else 0f
                    LinearProgressIndicator(
                        progress = { progressFraction },
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp))
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Enabling applications ($revertProgressCount / $revertTotalCount)...",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Result summary banner if finished
            lastRevertResult?.let { (successes, failures) ->
                Surface(
                    color = if (failures == 0) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = if (failures == 0) Icons.Default.CheckCircle else Icons.Default.Error,
                            contentDescription = null,
                            tint = if (failures == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = if (failures == 0) "Successfully enabled $successes apps." else "Enabled $successes apps. $failures failed (privileged permission or system policy).",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (failures == 0) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            if (selectedTab == 0) {
                // Tab 0: Enable All
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp).padding(top = 2.dp)
                            )
                            Column {
                                Text(
                                    text = "One-Tap Re-enable",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "This will restore all disabled applications to their normal enabled state. System-critical apps are automatically protected and skipped.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        SoundEffectManager.playClick()
                        onEnableAll()
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    enabled = disabledApps.isNotEmpty() && !isReverting
                ) {
                    Icon(Icons.Default.Restore, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        if (disabledApps.isEmpty()) "No Disabled Apps" else "Enable All (${disabledApps.size} Apps)",
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

            } else {
                // Tab 1: Select to Enable
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search disabled apps...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {
                            if (selectedPackages.size == filteredDisabledApps.size) {
                                selectedPackages.clear()
                            } else {
                                selectedPackages.clear()
                                selectedPackages.addAll(filteredDisabledApps.map { it.packageName })
                            }
                        }
                    ) {
                        Checkbox(
                            checked = filteredDisabledApps.isNotEmpty() && selectedPackages.size == filteredDisabledApps.size,
                            onCheckedChange = { isChecked ->
                                if (isChecked) {
                                    selectedPackages.clear()
                                    selectedPackages.addAll(filteredDisabledApps.map { it.packageName })
                                } else {
                                    selectedPackages.clear()
                                }
                            }
                        )
                        Text(
                            text = "Select All (${filteredDisabledApps.size})",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }

                    Text(
                        text = "${selectedPackages.size} selected",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .heightIn(max = 280.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (filteredDisabledApps.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (searchQuery.isBlank()) "No disabled applications found." else "No disabled apps matching search.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        items(filteredDisabledApps, key = { it.packageName }) { app ->
                            val isChecked = selectedPackages.contains(app.packageName)
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isChecked) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (isChecked) selectedPackages.remove(app.packageName)
                                        else selectedPackages.add(app.packageName)
                                    }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (app.icon != null) {
                                        Image(
                                            bitmap = app.icon.toBitmap(96, 96).asImageBitmap(),
                                            contentDescription = null,
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
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

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = app.appName,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = app.packageName,
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Checkbox(
                                        checked = isChecked,
                                        onCheckedChange = { checked ->
                                            if (checked) selectedPackages.add(app.packageName)
                                            else selectedPackages.remove(app.packageName)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        SoundEffectManager.playClick()
                        onEnableSelected(selectedPackages.toList())
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    enabled = selectedPackages.isNotEmpty() && !isReverting
                ) {
                    Icon(Icons.Default.Restore, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Enable Selected (${selectedPackages.size})", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}
