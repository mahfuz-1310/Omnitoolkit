
package com.example.ui.screens
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.*

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.MainViewModel

@Composable
fun SwitchRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Switch(
            checked = checked, 
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedTrackColor = MaterialTheme.colorScheme.primary)
        )
    }
}

fun copyToClipboard(context: Context, text: String, label: String = "Generated Item", viewModel: MainViewModel? = null) {
    try {
        com.example.utils.SoundEffectManager.vibrate(context, 35L)
        com.example.utils.SoundEffectManager.playClick(context)
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        if (viewModel != null) {
            viewModel.showFloatingToast("Copied!")
        } else {
            Toast.makeText(context, "Copied!", Toast.LENGTH_SHORT).show()
        }
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Failed to copy", Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun ResultRowItem(index: Int, name: String, viewModel: MainViewModel, context: Context, type: String = "NAME", onRegenerate: (() -> Unit)? = null) {
    val isFav by viewModel.isFavorite(type, name).collectAsStateWithLifecycle(initialValue = false)
    var showMenu by remember { mutableStateOf(false) }
    var showCheckerSheet by remember { mutableStateOf(false) }
    
    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = {
                            copyToClipboard(context, name, type, viewModel)
                        },
                        onLongPress = {
                            com.example.utils.SoundEffectManager.playClick()
                            showMenu = true
                        }
                    )
                }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = index.toString().padStart(2, '0'),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(32.dp)
            )
            Text(
                text = name,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            if (type == "USERNAME") {
                IconButton(
                    onClick = {
                        com.example.utils.SoundEffectManager.playClick(context)
                        showCheckerSheet = true
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.Public, contentDescription = "Check Availability", modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
                }
            }
            IconButton(
                onClick = {
                    copyToClipboard(context, name, type, viewModel)
                },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(
                onClick = {
                    com.example.utils.SoundEffectManager.playClick()
                    if (isFav) viewModel.removeFavorite(type, name)
                    else viewModel.addFavorite(type, name)
                },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorite",
                    modifier = Modifier.size(20.dp),
                    tint = if(isFav) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
        ) {
            DropdownMenuItem(
                text = { Text("Copy") },
                leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null) },
                onClick = {
                    showMenu = false
                    copyToClipboard(context, name, type, viewModel)
                }
            )
            if (type == "USERNAME") {
                DropdownMenuItem(
                    text = { Text("Check Online Availability") },
                    leadingIcon = { Icon(Icons.Default.Public, contentDescription = null) },
                    onClick = {
                        showMenu = false
                        com.example.utils.SoundEffectManager.playClick(context)
                        showCheckerSheet = true
                    }
                )
            }
            DropdownMenuItem(
                text = { Text("Share") },
                leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) },
                onClick = {
                    showMenu = false
                    com.example.utils.SoundEffectManager.playClick()
                    try {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            this.type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, name)
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share with"))
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            )
            if (onRegenerate != null) {
                DropdownMenuItem(
                    text = { Text("Regenerate") },
                    leadingIcon = { Icon(Icons.Default.Refresh, contentDescription = null) },
                    onClick = {
                        showMenu = false
                        com.example.utils.SoundEffectManager.playClick()
                        onRegenerate()
                    }
                )
            }
        }
    }

    com.example.ui.components.UsernameAvailabilityBottomSheet(
        visible = showCheckerSheet,
        username = name,
        onDismissRequest = { showCheckerSheet = false }
    )
}

@Composable
fun HeroActionButton(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, tint: Color = LocalContentColor.current, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable {
                com.example.utils.SoundEffectManager.playClick()
                onClick()
            }
            .padding(8.dp)
    ) {
        Icon(icon, contentDescription = label, tint = tint)
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = tint)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> SwipeToDeleteBox(
    item: T,
    onDelete: (T) -> Unit,
    content: @Composable (T) -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            if (it == SwipeToDismissBoxValue.EndToStart) {
                onDelete(item)
                true
            } else {
                false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val color = if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) {
                MaterialTheme.colorScheme.error
            } else {
                Color.Transparent
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color, RoundedCornerShape(20.dp))
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.onError
                )
            }
        },
        content = { content(item) }
    )
}

@Composable
fun SegmentedControl(
    options: List<String>,
    selected: String,
    onSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        options.forEach { option ->
            val isSelected = option == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                    .clickable {
                        com.example.utils.SoundEffectManager.playClick()
                        onSelected(option)
                    }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = option,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                )
            }
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun FontStyleBottomSheet(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    currentStyle: String,
    onStyleSelected: (String) -> Unit
) {
    if (visible) {
        ModalBottomSheet(
            onDismissRequest = onDismissRequest,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    "Font Style",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                androidx.compose.foundation.lazy.LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(com.example.utils.FontStyles.styles) { style ->
                        val isSelected = style == currentStyle
                        Surface(
                            onClick = { 
                                onStyleSelected(style)
                                onDismissRequest() 
                            },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        style, 
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        com.example.utils.FontStyles.apply("Zayen Ramirez", style),
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                if (isSelected) {
                                    Icon(
                                        androidx.compose.material.icons.Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }
                    }
                    item { Spacer(modifier = Modifier.height(32.dp)) }
                }
            }
        }
    }
}
