package com.example.feature.system.shell

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.utils.SoundEffectManager
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShellCommandSheet(
    viewModel: ShellCommandViewModel,
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val status by viewModel.status.collectAsStateWithLifecycle()
    val commandText by viewModel.commandText.collectAsStateWithLifecycle()
    val isRunning by viewModel.isRunning.collectAsStateWithLifecycle()
    val lastResult by viewModel.lastResult.collectAsStateWithLifecycle()
    val historyList by viewModel.historyList.collectAsStateWithLifecycle()
    val showDangerousConfirmation by viewModel.showDangerousConfirmation.collectAsStateWithLifecycle()
    val pendingDangerousCommand by viewModel.pendingDangerousCommand.collectAsStateWithLifecycle()
    val showHelpDialog by viewModel.showHelpDialog.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.refreshStatus()
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // ==================== 1. HEADER SECTION ====================
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
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Terminal,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Shell Command",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        val badgeColor = when (status) {
                            ShellStatus.READY -> Color(0xFF10B981)
                            ShellStatus.SHIZUKU_REQUIRED -> MaterialTheme.colorScheme.error
                            ShellStatus.PERMISSION_DENIED -> MaterialTheme.colorScheme.error
                            ShellStatus.STOPPED_BY_YOU -> MaterialTheme.colorScheme.outline
                        }
                        Text(
                            text = status.label,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = badgeColor
                        )
                    }
                }

                IconButton(
                    onClick = {
                        SoundEffectManager.playClick()
                        viewModel.showHelp()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Outlined.HelpOutline,
                        contentDescription = "Shizuku Help",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Execute advanced shell commands on your device using Shizuku.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Warning note
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.WarningAmber,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Use only commands you understand. Some commands may change system settings.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Shizuku not ready banner
            if (status != ShellStatus.READY) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = if (status == ShellStatus.PERMISSION_DENIED) "Shizuku Permission Needed" else "Shizuku Service Required",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (status == ShellStatus.PERMISSION_DENIED)
                                "Shizuku is running, but this app needs permission to execute commands."
                            else
                                "Shizuku is not running on this device. Start Shizuku via Wireless Debugging or ADB to run shell commands.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (status == ShellStatus.PERMISSION_DENIED) {
                                Button(
                                    onClick = {
                                        SoundEffectManager.playClick()
                                        viewModel.requestShizukuPermission()
                                    },
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Grant Permission")
                                }
                            }
                            OutlinedButton(
                                onClick = {
                                    SoundEffectManager.playClick()
                                    viewModel.showHelp()
                                },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Setup Guide")
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ==================== 2. COMMAND EDITOR ====================
            Text(
                text = "Command",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = commandText,
                onValueChange = { viewModel.updateCommand(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 90.dp, max = 150.dp),
                placeholder = {
                    Text(
                        "getprop ro.build.version.release",
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                },
                textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                shape = RoundedCornerShape(14.dp),
                trailingIcon = {
                    Row(modifier = Modifier.padding(end = 4.dp)) {
                        if (commandText.isNotEmpty()) {
                            IconButton(
                                onClick = {
                                    SoundEffectManager.playClick()
                                    viewModel.clearCommand()
                                }
                            ) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear command")
                            }
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Quick command actions (Paste / Clear)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AssistChip(
                    onClick = {
                        SoundEffectManager.playClick()
                        val clip = clipboardManager.getText()?.text
                        if (!clip.isNullOrBlank()) {
                            viewModel.updateCommand(clip)
                            Toast.makeText(context, "Command pasted from clipboard", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Clipboard is empty", Toast.LENGTH_SHORT).show()
                        }
                    },
                    label = { Text("Paste", fontSize = 12.sp) },
                    leadingIcon = { Icon(Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    shape = RoundedCornerShape(8.dp)
                )

                AssistChip(
                    onClick = {
                        SoundEffectManager.playClick()
                        viewModel.clearCommand()
                    },
                    label = { Text("Clear", fontSize = 12.sp) },
                    leadingIcon = { Icon(Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    shape = RoundedCornerShape(8.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ==================== 3. PRESET QUICK COMMANDS ====================
            Text(
                text = "Preset Quick Commands",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(6.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(ShellPresets.list) { preset ->
                    FilterChip(
                        selected = commandText == preset.command,
                        onClick = {
                            SoundEffectManager.playClick()
                            viewModel.selectPreset(preset)
                        },
                        label = {
                            Column {
                                Text(preset.name, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text(preset.command, fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        },
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ==================== 4. ACTION BUTTONS ====================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        SoundEffectManager.playClick()
                        viewModel.runCurrentCommand()
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isRunning && commandText.isNotBlank()
                ) {
                    if (isRunning) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Running...")
                    } else {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Run Command", fontWeight = FontWeight.Bold)
                    }
                }

                if (lastResult != null) {
                    OutlinedButton(
                        onClick = {
                            SoundEffectManager.playClick()
                            if (viewModel.copyOutputToClipboard(context)) {
                                Toast.makeText(context, "Output copied to clipboard", Toast.LENGTH_SHORT).show()
                            }
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copy Output")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ==================== 5. OUTPUT PANEL ====================
            Text(
                text = "Output",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(6.dp))

            val currentResult = lastResult
            if (currentResult != null) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF0F172A), // Dark slate terminal background
                    border = BorderStroke(1.dp, if (currentResult.isSuccess) Color(0xFF10B981).copy(alpha = 0.5f) else Color(0xFFEF4444).copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (currentResult.isSuccess) Color(0xFF065F46) else Color(0xFF991B1B)
                            ) {
                                Text(
                                    text = if (currentResult.isSuccess) "Exit Code: 0 (Success)" else "Exit Code: ${currentResult.exitCode}",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }

                            Text(
                                text = "${currentResult.executionTimeMs} ms",
                                color = Color.LightGray,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        SelectionContainer {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState())
                            ) {
                                if (currentResult.stdout.isNotEmpty()) {
                                    Text(
                                        text = currentResult.stdout,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 12.sp,
                                        color = Color(0xFFE2E8F0),
                                        lineHeight = 18.sp
                                    )
                                }
                                if (currentResult.stderr.isNotEmpty()) {
                                    if (currentResult.stdout.isNotEmpty()) Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "[STDERR]\n" + currentResult.stderr,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 12.sp,
                                        color = Color(0xFFFCA5A5),
                                        lineHeight = 18.sp
                                    )
                                }
                                if (currentResult.stdout.isEmpty() && currentResult.stderr.isEmpty()) {
                                    Text(
                                        text = "(Command executed with empty output)",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No command executed yet. Enter a shell command and tap 'Run Command'.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ==================== 6. RECENT HISTORY (LAST 5 COMMANDS) ====================
            val recentHistory = remember(historyList) { historyList.take(5) }
            val dateFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Recent History",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (recentHistory.isNotEmpty()) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "${recentHistory.size}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                            )
                        }
                    }
                }

                if (historyList.isNotEmpty()) {
                    TextButton(
                        onClick = {
                            SoundEffectManager.playClick()
                            viewModel.clearAllHistory()
                        },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                    ) {
                        Text("Clear", fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (recentHistory.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "No previous commands executed yet.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    recentHistory.forEach { item ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    SoundEffectManager.playClick()
                                    viewModel.updateCommand(item.command)
                                    Toast.makeText(context, "Loaded: ${item.command}", Toast.LENGTH_SHORT).show()
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Terminal,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Column {
                                        Text(
                                            text = item.command,
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "${dateFormat.format(Date(item.timestamp))} • ${item.executionTimeMs} ms • exit ${item.exitCode}",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = {
                                        SoundEffectManager.playClick()
                                        viewModel.deleteHistoryItem(item.id)
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Delete item",
                                        modifier = Modifier.size(14.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }

    // ==================== DANGEROUS COMMAND CONFIRMATION ====================
    if (showDangerousConfirmation) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissDangerousConfirmation() },
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "Potentially Dangerous Command",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "The command you are attempting to execute matches potentially destructive patterns:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = pendingDangerousCommand.orEmpty(),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                    Text(
                        text = "Are you sure you want to proceed with executing this command?",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        SoundEffectManager.playClick()
                        viewModel.confirmDangerousExecution()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Execute Anyway")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        SoundEffectManager.playClick()
                        viewModel.dismissDangerousConfirmation()
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // ==================== SHIZUKU SETUP HELP DIALOG ====================
    if (showHelpDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissHelp() },
            icon = {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = {
                Text(
                    text = "Shizuku Shell Authorization",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "This tool executes shell commands with device-level system permissions via Shizuku.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text("1. Install and open the **Shizuku** app.", style = MaterialTheme.typography.bodySmall)
                    Text("2. Start Shizuku via **Wireless Debugging** (Android 11+) or **ADB** from a PC.", style = MaterialTheme.typography.bodySmall)
                    Text("3. Grant permission to **Arw Hyper Toolkit** in Shizuku's authorized apps.", style = MaterialTheme.typography.bodySmall)
                    Text("4. Return here to run commands.", style = MaterialTheme.typography.bodySmall)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        SoundEffectManager.playClick()
                        viewModel.requestShizukuPermission()
                        viewModel.dismissHelp()
                    }
                ) {
                    Text("Request Permission")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        SoundEffectManager.playClick()
                        viewModel.dismissHelp()
                    }
                ) {
                    Text("Close")
                }
            }
        )
    }
}
