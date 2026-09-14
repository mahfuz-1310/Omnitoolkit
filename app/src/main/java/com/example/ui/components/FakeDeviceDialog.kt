package com.example.ui.components

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.utils.FakeDeviceManager
import com.example.utils.FakeDeviceProfile
import com.example.utils.SoundEffectManager

@Composable
fun FakeDeviceDialog(
    onDismissRequest: () -> Unit,
    currentSelectedDevice: String? = null,
    onSelectDevice: (FakeDeviceProfile, Boolean) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }

    // Toggle state: false = Full Name (Default), true = Short Name
    var isShortNameMode by remember { mutableStateOf(FakeDeviceManager.isShortNameMode(context)) }

    val filteredProfiles = remember(searchQuery) {
        FakeDeviceManager.searchDevices(searchQuery)
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        icon = {
            Icon(
                Icons.Default.Smartphone,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
        },
        title = {
            Text(
                text = "Select Fake Device",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Search Input Field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear search")
                            }
                        }
                    },
                    label = { Text("Search Devices (${FakeDeviceManager.devices.size}+ Models)") },
                    placeholder = { Text("e.g. S25 Ultra, Pixel 10, ROG") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                )

                // Device List
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .heightIn(max = 280.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 6.dp, vertical = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (filteredProfiles.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No matching devices found",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        } else {
                            items(filteredProfiles, key = { it.name }) { profile ->
                                val displayName = if (isShortNameMode) profile.shortName else profile.fullName
                                val isSelected = currentSelectedDevice != null && (
                                    currentSelectedDevice.equals(profile.fullName, ignoreCase = true) ||
                                    currentSelectedDevice.equals(profile.shortName, ignoreCase = true) ||
                                    currentSelectedDevice.equals(profile.name, ignoreCase = true)
                                )

                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            SoundEffectManager.vibrate(context, 35L)
                                            SoundEffectManager.playClick(context)
                                            onSelectDevice(profile, isShortNameMode)
                                        },
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = displayName,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = profile.displaySubtitle,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        if (isSelected) {
                                            Icon(
                                                Icons.Default.Check,
                                                contentDescription = "Selected",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Bottom Toggle / Radio Options: Short Name vs Full Name
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Name Display Format",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectableGroup(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Full Name (Default)
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .selectable(
                                        selected = !isShortNameMode,
                                        onClick = {
                                            SoundEffectManager.vibrate(context, 35L)
                                            SoundEffectManager.playClick(context)
                                            isShortNameMode = false
                                            FakeDeviceManager.setShortNameMode(context, false)
                                        },
                                        role = Role.RadioButton
                                    )
                                    .padding(vertical = 6.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = !isShortNameMode,
                                    onClick = null
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = "Full Name",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (!isShortNameMode) FontWeight.Bold else FontWeight.Normal
                                    )
                                    Text(
                                        text = "e.g. Samsung Galaxy S25 Ultra",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            // Short Name
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .selectable(
                                        selected = isShortNameMode,
                                        onClick = {
                                            SoundEffectManager.vibrate(context, 35L)
                                            SoundEffectManager.playClick(context)
                                            isShortNameMode = true
                                            FakeDeviceManager.setShortNameMode(context, true)
                                        },
                                        role = Role.RadioButton
                                    )
                                    .padding(vertical = 6.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isShortNameMode,
                                    onClick = null
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = "Short Name",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (isShortNameMode) FontWeight.Bold else FontWeight.Normal
                                    )
                                    Text(
                                        text = "e.g. Galaxy S25 Ultra",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    SoundEffectManager.playClick(context)
                    onDismissRequest()
                }
            ) {
                Text("Close")
            }
        }
    )
}
