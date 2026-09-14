package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.utils.DnsManager
import com.example.utils.DnsProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetworkSettingsScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var selectedProvider by remember { mutableStateOf(DnsManager.getSelectedDns(context)) }
    var customHostInput by remember { mutableStateOf(if (selectedProvider.id == "custom") selectedProvider.dotHost else "") }
    
    var expanded by remember { mutableStateOf(false) }

    // Group providers by category for the dropdown
    val groupedProviders = remember { DnsManager.providers.groupBy { it.category } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Private DNS Changer", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        try {
                            val intent = Intent(Settings.ACTION_NETWORK_OPERATOR_SETTINGS).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            try {
                                val intent = Intent(Settings.ACTION_WIRELESS_SETTINGS).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(intent)
                            } catch (ex: Exception) {
                                Toast.makeText(context, "Unable to open system network settings", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }) {
                        Icon(Icons.Default.Settings, contentDescription = "System Private DNS Settings")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Text(
                text = "Select a DNS provider to automatically configure your secure connection hostname.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // DNS Provider Category Dropdown
            Text(
                text = "DNS Provider Selection",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it }
            ) {
                OutlinedTextField(
                    value = if (selectedProvider.id == "custom") "✏️ ${selectedProvider.name}" else "[${selectedProvider.category}] ${selectedProvider.name}",
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    shape = RoundedCornerShape(12.dp)
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    groupedProviders.forEach { (category, providers) ->
                        // Category Header
                        DropdownMenuItem(
                            text = { 
                                Text(
                                    text = category,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            onClick = {},
                            enabled = false
                        )
                        // Category Items
                        providers.forEach { provider ->
                            DropdownMenuItem(
                                text = { 
                                    Text(
                                        text = if (provider.id == "custom") "✏️ ${provider.name}" else provider.name,
                                        style = MaterialTheme.typography.bodyMedium
                                    ) 
                                },
                                onClick = {
                                    selectedProvider = provider
                                    if (provider.id != "custom") {
                                        customHostInput = provider.dotHost
                                    }
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Hostname Input Field
            Text(
                text = "DNS Hostname",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = if (selectedProvider.id == "custom") customHostInput else selectedProvider.dotHost,
                onValueChange = { if (selectedProvider.id == "custom") customHostInput = it },
                readOnly = selectedProvider.id != "custom",
                placeholder = { Text("Enter custom DoT/DoH hostname") },
                leadingIcon = {
                    Icon(
                        imageVector = if (selectedProvider.id == "custom") Icons.Default.Edit else Icons.Default.Lock,
                        contentDescription = null,
                        tint = if (selectedProvider.id == "custom") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                supportingText = {
                    Text(if (selectedProvider.id == "custom") "Enter your custom DoT hostname." else "Managed by ${selectedProvider.name}.")
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = if (selectedProvider.id == "custom") Color.Transparent else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    focusedContainerColor = if (selectedProvider.id == "custom") Color.Transparent else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Action Buttons
            Button(
                onClick = {
                    val finalHost = if (selectedProvider.id == "custom") customHostInput.trim() else selectedProvider.dotHost
                    if (finalHost.isEmpty()) {
                        Toast.makeText(context, "Hostname cannot be empty", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    
                    com.example.utils.SoundEffectManager.playClick(context)
                    DnsManager.saveSelectedDns(context, selectedProvider, finalHost)
                    
                    // Copy to clipboard for easy pasting in Android Settings
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("DNS Hostname", finalHost)
                    clipboard.setPrimaryClip(clip)
                    
                    Toast.makeText(context, "Saved & Copied: $finalHost", Toast.LENGTH_SHORT).show()

                    // Optionally try to open Private DNS settings
                    try {
                        val intent = Intent(Settings.ACTION_NETWORK_OPERATOR_SETTINGS).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        try {
                            context.startActivity(Intent(Settings.ACTION_WIRELESS_SETTINGS).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            })
                        } catch (ex: Exception) {
                            // Fallback
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save & Apply DNS Settings", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Description Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("About this Provider", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = selectedProvider.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (selectedProvider.primaryIp.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "IPv4: ${selectedProvider.primaryIp} ${if (selectedProvider.secondaryIp.isNotEmpty()) "/ ${selectedProvider.secondaryIp}" else ""}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }
    }
}
