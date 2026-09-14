package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.MainViewModel
import com.example.utils.*
import kotlin.math.roundToInt

enum class FloatingWindowState {
    EXPANDED, COMPACT, MINIMIZED
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FloatingWindowOverlay(viewModel: MainViewModel) {
    val context = LocalContext.current
    val appIconBitmap = remember(context) {
        try {
            val drawable = context.packageManager.getApplicationIcon(context.packageName)
            (drawable as? BitmapDrawable)?.bitmap?.asImageBitmap()
        } catch (e: Exception) {
            null
        }
    }
    val floatingEnabled by viewModel.floatingModeEnabled.collectAsStateWithLifecycle()
    val rememberPos by viewModel.rememberFloatingPosition.collectAsStateWithLifecycle()
    val defaultSize by viewModel.defaultFloatingSize.collectAsStateWithLifecycle()
    val fontStyle by viewModel.fontStyle.collectAsStateWithLifecycle()
    
    // Shared ViewModel States for full synchronization
    val activeTool by viewModel.activeTool.collectAsStateWithLifecycle()
    val selectedMode by viewModel.selectedMode.collectAsStateWithLifecycle()
    val selectedGender by viewModel.selectedGender.collectAsStateWithLifecycle()
    val selectedCountry by viewModel.selectedCountry.collectAsStateWithLifecycle()
    val selectedStyle by viewModel.selectedStyle.collectAsStateWithLifecycle()
    val selectedCount by viewModel.selectedCount.collectAsStateWithLifecycle()
    val selectedLength by viewModel.selectedNameLength.collectAsStateWithLifecycle()
    val noRepeat by viewModel.noRepeat.collectAsStateWithLifecycle()
    val customFirst by viewModel.customFirst.collectAsStateWithLifecycle()
    val customLast by viewModel.customLast.collectAsStateWithLifecycle()
    val currentResultText by viewModel.currentResultText.collectAsStateWithLifecycle()
    val generatedNames by viewModel.generatedNames.collectAsStateWithLifecycle()
    val isGenerating by viewModel.isGenerating.collectAsStateWithLifecycle()

    val usernameStyle by viewModel.usernameStyle.collectAsStateWithLifecycle()
    val usernameAddNum by viewModel.usernameAddNum.collectAsStateWithLifecycle()
    val usernameAddUnder by viewModel.usernameAddUnder.collectAsStateWithLifecycle()
    val usernameAddDot by viewModel.usernameAddDot.collectAsStateWithLifecycle()
    val usernameShort by viewModel.usernameShort.collectAsStateWithLifecycle()
    val usernamePrefix by viewModel.usernamePrefix.collectAsStateWithLifecycle()
    val usernameSuffix by viewModel.usernameSuffix.collectAsStateWithLifecycle()
    
    val passwordLength by viewModel.passwordLength.collectAsStateWithLifecycle()
    val passwordUpper by viewModel.passwordUpper.collectAsStateWithLifecycle()
    val passwordLower by viewModel.passwordLower.collectAsStateWithLifecycle()
    val passwordNums by viewModel.passwordNums.collectAsStateWithLifecycle()
    val passwordSyms by viewModel.passwordSyms.collectAsStateWithLifecycle()
    val passwordExcludeSimilar by viewModel.passwordExcludeSimilar.collectAsStateWithLifecycle()
    val passwordExcludeAmbiguous by viewModel.passwordExcludeAmbiguous.collectAsStateWithLifecycle()
    
    val stylishInput by viewModel.stylishTextInput.collectAsStateWithLifecycle()
    val stylishFont by viewModel.stylishFont.collectAsStateWithLifecycle()
    val nicknameCat by viewModel.nicknameCategory.collectAsStateWithLifecycle()
    val nicknameCount by viewModel.nicknameCount.collectAsStateWithLifecycle()
    val bioCat by viewModel.bioCategory.collectAsStateWithLifecycle()
    val bioLength by viewModel.bioLength.collectAsStateWithLifecycle()
    val bioEmoji by viewModel.bioEmoji.collectAsStateWithLifecycle()
    val smartQuery by viewModel.smartQuery.collectAsStateWithLifecycle()
    val mix1 by viewModel.mixerName1.collectAsStateWithLifecycle()
    val mix2 by viewModel.mixerName2.collectAsStateWithLifecycle()
    val mix3 by viewModel.mixerName3.collectAsStateWithLifecycle()
    val mixResults by viewModel.mixerResults.collectAsStateWithLifecycle()

    val lastNameCategory by viewModel.lastNameCategory.collectAsStateWithLifecycle()
    val customMiddle by viewModel.customMiddle.collectAsStateWithLifecycle()
    val initialsOnly by viewModel.initialsOnly.collectAsStateWithLifecycle()
    val includeMeaning by viewModel.includeMeaning.collectAsStateWithLifecycle()
    val excludeDuplicates by viewModel.excludeDuplicates.collectAsStateWithLifecycle()
    val useRareNames by viewModel.useRareNames.collectAsStateWithLifecycle()
    val includeMiddle by viewModel.includeMiddle.collectAsStateWithLifecycle()

    val savedX by viewModel.floatingPosX.collectAsStateWithLifecycle()
    val savedY by viewModel.floatingPosY.collectAsStateWithLifecycle()

    if (!floatingEnabled) return

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(1000f)
    ) {
        val density = LocalDensity.current
        val maxWidthPx = with(density) { maxWidth.toPx() }
        val maxHeightPx = with(density) { maxHeight.toPx() }

        var offsetX by remember { mutableFloatStateOf(savedX) }
        var offsetY by remember { mutableFloatStateOf(savedY) }

        var customWidthDp by remember { mutableStateOf(350.dp) }
        var customHeightDp by remember { mutableStateOf(480.dp) }

        // Sync local offset with saved position if it changes externally
        LaunchedEffect(savedX, savedY) {
            offsetX = savedX
            offsetY = savedY
        }

        var windowState by remember {
            mutableStateOf(
                when (defaultSize) {
                    "Large", "Medium" -> FloatingWindowState.EXPANDED
                    else -> FloatingWindowState.COMPACT
                }
            )
        }

        var showToolMenu by remember { mutableStateOf(false) }
        var showCountryDropdown by remember { mutableStateOf(false) }
        var showBatchList by remember { mutableStateOf(false) }

        val context = LocalContext.current
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        fun copyToClipboard(text: String, label: String = "Copied!") {
            try {
                com.example.utils.SoundEffectManager.vibrate(context, 35L)
                com.example.utils.SoundEffectManager.playClick(context)
                clipboard.setPrimaryClip(ClipData.newPlainText(com.example.utils.AppConstants.APP_NAME, text))
                viewModel.showFloatingToast("Copied!")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        val allTools = listOf(
            "Name", "First + Middle", "Middle Name", "Surname",
            "Username", "Nickname", "Password", "Bio",
            "Stylish Text", "Random Profile", "Smart Assistant", "Name Mixer"
        )

        val isHeroFavoriteState = viewModel.isFavorite("NAME", currentResultText).collectAsStateWithLifecycle()

        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
        ) {
            when (windowState) {
                // ==================== 1. MINIMIZED STATE ====================
                FloatingWindowState.MINIMIZED -> {
                    Surface(
                        modifier = Modifier
                            .size(64.dp)
                            .shadow(12.dp, CircleShape)
                            .pointerInput(maxWidthPx, maxHeightPx) {
                                detectDragGestures(
                                    onDragEnd = {
                                        viewModel.setFloatingPosition(offsetX, offsetY)
                                    },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        offsetX = (offsetX + dragAmount.x).coerceIn(0f, (maxWidthPx - 64f).coerceAtLeast(0f))
                                        offsetY = (offsetY + dragAmount.y).coerceIn(0f, (maxHeightPx - 64f).coerceAtLeast(0f))
                                    }
                                )
                            }
                            .clickable { windowState = FloatingWindowState.COMPACT },
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary,
                        tonalElevation = 8.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Restore Floating Window",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }

                // ==================== 2. COMPACT STATE ====================
                FloatingWindowState.COMPACT -> {
                    Card(
                        modifier = Modifier
                            .width(340.dp)
                            .shadow(12.dp, RoundedCornerShape(24.dp))
                            .clip(RoundedCornerShape(24.dp)),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Column {
                            // Dedicated Draggable Header (Fixed at top)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                                    .pointerInput(maxWidthPx, maxHeightPx) {
                                        detectDragGestures(
                                            onDragEnd = {
                                                viewModel.setFloatingPosition(offsetX, offsetY)
                                            },
                                            onDrag = { change, dragAmount ->
                                                change.consume()
                                                offsetX = (offsetX + dragAmount.x).coerceIn(0f, (maxWidthPx - 340f).coerceAtLeast(0f))
                                                offsetY = (offsetY + dragAmount.y).coerceIn(0f, (maxHeightPx - 200f).coerceAtLeast(0f))
                                            }
                                        )
                                    }
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        Icons.Default.DragHandle,
                                        contentDescription = "Drag Window",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        com.example.utils.AppConstants.APP_NAME,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = { windowState = FloatingWindowState.EXPANDED },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.OpenInFull, contentDescription = "Expand", modifier = Modifier.size(18.dp))
                                    }
                                    IconButton(
                                        onClick = { windowState = FloatingWindowState.MINIMIZED },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Remove, contentDescription = "Minimize", modifier = Modifier.size(18.dp))
                                    }
                                    IconButton(
                                        onClick = { viewModel.setFloatingMode(false) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(18.dp))
                                    }
                                }
                            }

                            Column(modifier = Modifier.padding(12.dp)) {
                                // Quick Gender Selector
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    listOf("Male", "Female", "Unisex").forEach { g ->
                                        val isSelected = selectedGender == g
                                        FilterChip(
                                            selected = isSelected,
                                            onClick = {
                                                viewModel.setGender(g)
                                                viewModel.generateCurrentTool()
                                            },
                                            label = {
                                                Text(
                                                    when(g) {
                                                        "Male" -> "♂ Male"
                                                        "Female" -> "♀ Female"
                                                        else -> "⚥ Mix"
                                                    },
                                                    fontSize = 11.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                                )
                                            },
                                            modifier = Modifier.weight(1f),
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                            )
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Compact Result Card
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                activeTool.uppercase(),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                FontStyles.apply(currentResultText, fontStyle),
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            IconButton(
                                                onClick = { copyToClipboard(currentResultText) },
                                                modifier = Modifier.size(36.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.ContentCopy,
                                                    contentDescription = "Copy",
                                                    modifier = Modifier.size(18.dp),
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                            IconButton(
                                                onClick = { viewModel.generateCurrentTool() },
                                                modifier = Modifier.size(36.dp)
                                            ) {
                                                if (isGenerating) {
                                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                                } else {
                                                    Icon(
                                                        Icons.Default.Refresh,
                                                        contentDescription = "Regenerate",
                                                        modifier = Modifier.size(20.dp),
                                                        tint = MaterialTheme.colorScheme.primary
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // ==================== 3. EXPANDED STATE ====================
                FloatingWindowState.EXPANDED -> {
                    Box {
                        val boxScope = this
                        Card(
                            modifier = Modifier
                                .width(customWidthDp)
                                .height(customHeightDp)
                                .shadow(16.dp, RoundedCornerShape(24.dp))
                                .clip(RoundedCornerShape(24.dp)),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
                        ) {
                            Column {
                            // Dedicated Draggable Header Area (Fixed)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f))
                                    .pointerInput(maxWidthPx, maxHeightPx) {
                                        detectDragGestures(
                                            onDragEnd = {
                                                viewModel.setFloatingPosition(offsetX, offsetY)
                                            },
                                            onDrag = { change, dragAmount ->
                                                change.consume()
                                                offsetX = (offsetX + dragAmount.x).coerceIn(0f, (maxWidthPx - 360f).coerceAtLeast(0f))
                                                offsetY = (offsetY + dragAmount.y).coerceIn(0f, (maxHeightPx - 400f).coerceAtLeast(0f))
                                            }
                                        )
                                    }
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        Icons.Default.DragIndicator,
                                        contentDescription = "Drag Studio",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    if (appIconBitmap != null) {
                                        Image(
                                            bitmap = appIconBitmap,
                                            contentDescription = "App Logo",
                                            modifier = Modifier.size(24.dp).clip(CircleShape),
                                            contentScale = ContentScale.Fit
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = "App Logo",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "NameGen Studio",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = { windowState = FloatingWindowState.COMPACT },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.CloseFullscreen, contentDescription = "Collapse", modifier = Modifier.size(18.dp))
                                    }
                                    IconButton(
                                        onClick = { windowState = FloatingWindowState.MINIMIZED },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Remove, contentDescription = "Minimize", modifier = Modifier.size(18.dp))
                                    }
                                    IconButton(
                                        onClick = { viewModel.setFloatingMode(false) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(18.dp))
                                    }
                                }
                            }

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp)
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Tool Selector Tabs / Dropdown
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val topTools = listOf("Name", "Username", "Password")
                                    topTools.forEach { tool ->
                                        val isSelected = activeTool == tool
                                        FilterChip(
                                            selected = isSelected,
                                            onClick = {
                                                viewModel.setActiveTool(tool)
                                                viewModel.generateCurrentTool()
                                            },
                                            label = {
                                                Text(
                                                    when(tool) {
                                                        "Name" -> "✨ Name"
                                                        "Username" -> "🆔 User"
                                                        else -> "🔐 Pwd"
                                                    },
                                                    fontSize = 11.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                                )
                                            },
                                            modifier = Modifier.weight(1f),
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                            )
                                        )
                                    }

                                    Box {
                                        IconButton(
                                            onClick = { showToolMenu = true },
                                            modifier = Modifier.size(34.dp)
                                        ) {
                                            Icon(Icons.Default.MoreHoriz, contentDescription = "More Tools")
                                        }
                                        DropdownMenu(
                                            expanded = showToolMenu,
                                            onDismissRequest = { showToolMenu = false }
                                        ) {
                                            allTools.forEach { tool ->
                                                DropdownMenuItem(
                                                    text = {
                                                        Text(
                                                            tool,
                                                            fontWeight = if (activeTool == tool) FontWeight.Bold else FontWeight.Normal,
                                                            color = if (activeTool == tool) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                                        )
                                                    },
                                                    onClick = {
                                                        viewModel.setActiveTool(tool)
                                                        showToolMenu = false
                                                        viewModel.generateCurrentTool()
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }

                            // Hero Result Card
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            activeTool.uppercase(),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.sp
                                        )
                                        if (generatedNames.size > 1 && (activeTool == "Name" || activeTool == "First + Middle")) {
                                            Text(
                                                "${generatedNames.size} Variations",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        FontStyles.apply(currentResultText, fontStyle),
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        TextButton(
                                            onClick = { copyToClipboard(currentResultText) },
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Copy", fontSize = 12.sp)
                                        }
                                        
                                        IconButton(
                                            onClick = {
                                                if (isHeroFavoriteState.value) {
                                                    viewModel.removeFavorite(activeTool.uppercase(), currentResultText)
                                                } else {
                                                    viewModel.addFavorite(activeTool.uppercase(), currentResultText)
                                                }
                                            },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(
                                                if (isHeroFavoriteState.value) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                                contentDescription = "Favorite",
                                                tint = if (isHeroFavoriteState.value) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(4.dp))
                                        Button(
                                            onClick = { viewModel.generateCurrentTool() },
                                            shape = RoundedCornerShape(10.dp),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            if (isGenerating) {
                                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                                            } else {
                                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Generate", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }

                            // === TOOL SPECIFIC COMPREHENSIVE CONTROLS ===
                            when (activeTool) {
                                "Name", "First + Middle", "Middle Name", "Surname" -> {
                                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        // 1. Gender Controls
                                        Text("GENDER", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            listOf("Male", "Female", "Unisex").forEach { g ->
                                                val isSelected = selectedGender == g
                                                FilterChip(
                                                    selected = isSelected,
                                                    onClick = {
                                                        viewModel.setGender(g)
                                                        viewModel.generateCurrentTool()
                                                    },
                                                    label = {
                                                        Text(
                                                            when(g) {
                                                                "Male" -> "♂ Male"
                                                                "Female" -> "♀ Female"
                                                                else -> "⚥ Mix"
                                                            },
                                                            fontSize = 11.sp,
                                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                                        )
                                                    },
                                                    modifier = Modifier.weight(1f),
                                                    colors = FilterChipDefaults.filterChipColors(
                                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                                    )
                                                )
                                            }
                                        }
                                    }

                                    // 2. Mode Controls
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text("GENERATION MODE", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .horizontalScroll(rememberScrollState()),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            listOf("Full Name", "First Name", "Middle Name", "Last Name").forEach { m ->
                                                val isSelected = selectedMode == m
                                                FilterChip(
                                                    selected = isSelected,
                                                    onClick = {
                                                        viewModel.setMode(m)
                                                        viewModel.generateCurrentTool()
                                                    },
                                                    label = { Text(m, fontSize = 11.sp) }
                                                )
                                            }
                                        }
                                    }

                                    // 3. Country & Last Name Category
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Text("COUNTRY", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                            Box {
                                                OutlinedCard(
                                                    onClick = { showCountryDropdown = true },
                                                    shape = RoundedCornerShape(10.dp)
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(8.dp),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.SpaceBetween
                                                    ) {
                                                        Text(selectedCountry, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                                                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp))
                                                    }
                                                }
                                                DropdownMenu(
                                                    expanded = showCountryDropdown,
                                                    onDismissRequest = { showCountryDropdown = false }
                                                ) {
                                                    CountryData.countries.forEach { c ->
                                                        DropdownMenuItem(
                                                            text = { Text(c) },
                                                            onClick = {
                                                                viewModel.setCountry(c)
                                                                showCountryDropdown = false
                                                                viewModel.generateCurrentTool()
                                                            }
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        if (selectedMode == "Full Name" || selectedMode == "Last Name") {
                                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                Text("SURNAME TYPE", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                                var showSurMenu by remember { mutableStateOf(false) }
                                                Box {
                                                    OutlinedCard(
                                                        onClick = { showSurMenu = true },
                                                        shape = RoundedCornerShape(10.dp)
                                                    ) {
                                                        Row(
                                                            modifier = Modifier.padding(8.dp),
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.SpaceBetween
                                                        ) {
                                                            Text(lastNameCategory, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                                                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp))
                                                        }
                                                    }
                                                    DropdownMenu(
                                                        expanded = showSurMenu,
                                                        onDismissRequest = { showSurMenu = false }
                                                    ) {
                                                        listOf("Random", "Popular", "Modern", "Rare", "Royal", "International").forEach { cat ->
                                                            DropdownMenuItem(
                                                                text = { Text(cat) },
                                                                onClick = {
                                                                    viewModel.setLastNameCategory(cat)
                                                                    showSurMenu = false
                                                                    viewModel.generateCurrentTool()
                                                                }
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    // 4. Style Selector
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text("STYLE", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .horizontalScroll(rememberScrollState()),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            listOf("Modern", "Cute", "Stylish", "Unique", "Classic", "Royal", "Gaming", "Aesthetic").forEach { s ->
                                                val isSelected = selectedStyle == s
                                                FilterChip(
                                                    selected = isSelected,
                                                    onClick = {
                                                        viewModel.setStyle(s)
                                                        viewModel.generateCurrentTool()
                                                    },
                                                    label = { Text(s, fontSize = 11.sp) }
                                                )
                                            }
                                        }
                                    }

                                    // 5. Custom Inputs (First, Middle, Surname)
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text("CUSTOM INPUTS", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            OutlinedTextField(
                                                value = customFirst,
                                                onValueChange = { viewModel.setCustomFirst(it) },
                                                label = { Text("First", fontSize = 10.sp) },
                                                modifier = Modifier.weight(1f),
                                                singleLine = true,
                                                shape = RoundedCornerShape(10.dp)
                                            )
                                            OutlinedTextField(
                                                value = customLast,
                                                onValueChange = { viewModel.setCustomLast(it) },
                                                label = { Text("Surname", fontSize = 10.sp) },
                                                modifier = Modifier.weight(1f),
                                                singleLine = true,
                                                shape = RoundedCornerShape(10.dp)
                                            )
                                        }
                                        if (selectedMode == "Full Name" || selectedMode == "Middle Name") {
                                            OutlinedTextField(
                                                value = customMiddle,
                                                onValueChange = { viewModel.setCustomMiddle(it) },
                                                label = { Text("Middle Name", fontSize = 10.sp) },
                                                modifier = Modifier.fillMaxWidth(),
                                                singleLine = true,
                                                shape = RoundedCornerShape(10.dp)
                                            )
                                        }
                                    }

                                    // 6. Advanced Toggle
                                    var showAdvanced by remember { mutableStateOf(false) }
                                    OutlinedCard(
                                        onClick = { showAdvanced = !showAdvanced },
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(10.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("Advanced Settings", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                            Icon(if (showAdvanced) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, contentDescription = null)
                                        }
                                        AnimatedVisibility(visible = showAdvanced) {
                                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                                Text("Quantity: $selectedCount", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                                                    listOf(1, 10, 25, 50, 100).forEach { cnt ->
                                                        FilterChip(
                                                            selected = selectedCount == cnt,
                                                            onClick = { viewModel.setCount(cnt) },
                                                            label = { Text("$cnt", fontSize = 10.sp) }
                                                        )
                                                    }
                                                }
                                                
                                                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                                                
                                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                                    Text("Initials Only", fontSize = 11.sp)
                                                    Switch(checked = initialsOnly, onCheckedChange = { viewModel.setInitialsOnly(it) }, modifier = Modifier.scale(0.7f))
                                                }
                                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                                    Text("Include Middle", fontSize = 11.sp)
                                                    Switch(checked = includeMiddle, onCheckedChange = { viewModel.setIncludeMiddle(it) }, modifier = Modifier.scale(0.7f))
                                                }
                                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                                    Text("Include Meaning", fontSize = 11.sp)
                                                    Switch(checked = includeMeaning, onCheckedChange = { viewModel.setIncludeMeaning(it) }, modifier = Modifier.scale(0.7f))
                                                }
                                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                                    Text("No Repeat", fontSize = 11.sp)
                                                    Switch(checked = noRepeat, onCheckedChange = { viewModel.setNoRepeat(it) }, modifier = Modifier.scale(0.7f))
                                                }
                                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                                    Text("Exclude Duplicates", fontSize = 11.sp)
                                                    Switch(checked = excludeDuplicates, onCheckedChange = { viewModel.setExcludeDuplicates(it) }, modifier = Modifier.scale(0.7f))
                                                }
                                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                                    Text("Use Rare Names", fontSize = 11.sp)
                                                    Switch(checked = useRareNames, onCheckedChange = { viewModel.setUseRareNames(it) }, modifier = Modifier.scale(0.7f))
                                                }

                                                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                                                Text("Name Length: $selectedLength", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                    listOf("Short", "Medium", "Long").forEach { len ->
                                                        FilterChip(
                                                            selected = selectedLength == len,
                                                            onClick = { viewModel.setNameLength(len) },
                                                            label = { Text(len, fontSize = 10.sp) }
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    // 7. Mini Batch Results Viewer
                                    if (generatedNames.size > 1) {
                                        Surface(
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(12.dp),
                                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                        ) {
                                            Column(modifier = Modifier.padding(8.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text("Generated List (${generatedNames.size})", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                                    TextButton(onClick = {
                                                        val allText = generatedNames.joinToString("\n") { it.name }
                                                        copyToClipboard(allText, "Copied all names!")
                                                    }) {
                                                        Text("Copy All", fontSize = 11.sp)
                                                    }
                                                }
                                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                    generatedNames.take(8).forEachIndexed { idx, nameRes ->
                                                        val styledName = FontStyles.apply(nameRes.name, fontStyle)
                                                        val isFav = viewModel.isFavorite("NAME", nameRes.name).collectAsStateWithLifecycle()
                                                        Row(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .padding(vertical = 2.dp),
                                                            horizontalArrangement = Arrangement.SpaceBetween,
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Text("${idx + 1}. $styledName", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                                                            Row {
                                                                IconButton(onClick = { copyToClipboard(styledName) }, modifier = Modifier.size(24.dp)) {
                                                                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.primary)
                                                                }
                                                                IconButton(
                                                                    onClick = {
                                                                        if (isFav.value) viewModel.removeFavorite("NAME", nameRes.name)
                                                                        else viewModel.addFavorite("NAME", nameRes.name)
                                                                    },
                                                                    modifier = Modifier.size(24.dp)
                                                                ) {
                                                                    Icon(
                                                                        if (isFav.value) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                                                        contentDescription = null,
                                                                        modifier = Modifier.size(12.dp),
                                                                        tint = if (isFav.value) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                                                                    )
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                "Username" -> {
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text("USERNAME STYLE", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                        Row(
                                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            listOf("Random", "Aesthetic", "Cute", "Gaming").forEach { s ->
                                                FilterChip(
                                                    selected = usernameStyle == s,
                                                    onClick = {
                                                        viewModel.setUsernameStyle(s)
                                                        viewModel.generateCurrentTool()
                                                    },
                                                    label = { Text(s, fontSize = 11.sp) }
                                                )
                                            }
                                        }
                                        Text("MODIFIERS", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                        Row(modifier = Modifier.fillMaxWidth()) {
                                            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                                Checkbox(checked = usernameAddNum, onCheckedChange = { viewModel.setUsernameAddNum(it); viewModel.generateCurrentTool() })
                                                Text("Nums", fontSize = 11.sp)
                                            }
                                            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                                Checkbox(checked = usernameAddUnder, onCheckedChange = { viewModel.setUsernameAddUnder(it); viewModel.generateCurrentTool() })
                                                Text("Under", fontSize = 11.sp)
                                            }
                                            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                                Checkbox(checked = usernameAddDot, onCheckedChange = { viewModel.setUsernameAddDot(it); viewModel.generateCurrentTool() })
                                                Text("Dot", fontSize = 11.sp)
                                            }
                                        }
                                        Row(modifier = Modifier.fillMaxWidth()) {
                                            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                                Checkbox(checked = usernameShort, onCheckedChange = { viewModel.setUsernameShort(it); viewModel.generateCurrentTool() })
                                                Text("Short", fontSize = 11.sp)
                                            }
                                            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                                Checkbox(checked = usernamePrefix, onCheckedChange = { viewModel.setUsernamePrefix(it); viewModel.generateCurrentTool() })
                                                Text("Pre", fontSize = 11.sp)
                                            }
                                            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                                Checkbox(checked = usernameSuffix, onCheckedChange = { viewModel.setUsernameSuffix(it); viewModel.generateCurrentTool() })
                                                Text("Suf", fontSize = 11.sp)
                                            }
                                        }
                                    }
                                }

                                "Password" -> {
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text("PASSWORD LENGTH: ${passwordLength.toInt()}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                        Slider(
                                            value = passwordLength,
                                            onValueChange = {
                                                viewModel.setPasswordLength(it)
                                                viewModel.generateCurrentTool()
                                            },
                                            valueRange = 8f..32f
                                        )
                                        Row(modifier = Modifier.fillMaxWidth()) {
                                            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                                Checkbox(checked = passwordUpper, onCheckedChange = { viewModel.setPasswordUpper(it); viewModel.generateCurrentTool() })
                                                Text("ABC", fontSize = 11.sp)
                                            }
                                            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                                Checkbox(checked = passwordLower, onCheckedChange = { viewModel.setPasswordLower(it); viewModel.generateCurrentTool() })
                                                Text("abc", fontSize = 11.sp)
                                            }
                                            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                                Checkbox(checked = passwordNums, onCheckedChange = { viewModel.setPasswordNums(it); viewModel.generateCurrentTool() })
                                                Text("123", fontSize = 11.sp)
                                            }
                                            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                                Checkbox(checked = passwordSyms, onCheckedChange = { viewModel.setPasswordSyms(it); viewModel.generateCurrentTool() })
                                                Text("!@#", fontSize = 11.sp)
                                            }
                                        }
                                        Row(modifier = Modifier.fillMaxWidth()) {
                                            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                                Checkbox(checked = passwordExcludeSimilar, onCheckedChange = { viewModel.setPasswordExcludeSimilar(it); viewModel.generateCurrentTool() })
                                                Text("No Sim", fontSize = 11.sp)
                                            }
                                            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                                Checkbox(checked = passwordExcludeAmbiguous, onCheckedChange = { viewModel.setPasswordExcludeAmbiguous(it); viewModel.generateCurrentTool() })
                                                Text("No Amb", fontSize = 11.sp)
                                            }
                                        }
                                    }
                                }

                                "Stylish Text" -> {
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        OutlinedTextField(
                                            value = stylishInput,
                                            onValueChange = {
                                                viewModel.setStylishTextInput(it)
                                                viewModel.generateCurrentTool()
                                            },
                                            label = { Text("Text to style") },
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        Text("FONT STYLE", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                        Row(
                                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            FontStyles.styles.forEach { f ->
                                                FilterChip(
                                                    selected = stylishFont == f,
                                                    onClick = {
                                                        viewModel.setStylishFont(f)
                                                        viewModel.generateCurrentTool()
                                                    },
                                                    label = { Text(f, fontSize = 11.sp) }
                                                )
                                            }
                                        }
                                    }
                                }

                                "Nickname" -> {
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text("NICKNAME CATEGORY", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                        Row(
                                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            listOf("Random", "Cute", "Gaming", "Funny", "Stylish", "Short", "Royal", "Couple").forEach { c ->
                                                FilterChip(
                                                    selected = nicknameCat == c,
                                                    onClick = {
                                                        viewModel.setNicknameCategory(c)
                                                        viewModel.generateCurrentTool()
                                                    },
                                                    label = { Text(c, fontSize = 11.sp) }
                                                )
                                            }
                                        }
                                        Text("COUNT: ${nicknameCount.toInt()}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                        Slider(
                                            value = nicknameCount,
                                            onValueChange = { viewModel.setNicknameCount(it) },
                                            valueRange = 5f..50f,
                                            steps = 8
                                        )
                                    }
                                }

                                "Bio" -> {
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text("BIO CATEGORY", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                        Row(
                                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            listOf("Instagram", "TikTok", "Gaming", "YouTube", "Professional", "Aesthetic", "Funny", "Minimal").forEach { c ->
                                                FilterChip(
                                                    selected = bioCat == c,
                                                    onClick = {
                                                        viewModel.setBioCategory(c)
                                                        viewModel.generateCurrentTool()
                                                    },
                                                    label = { Text(c, fontSize = 11.sp) }
                                                )
                                            }
                                        }
                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            listOf("Short", "Medium", "Long").forEach { l ->
                                                FilterChip(
                                                    selected = bioLength == l,
                                                    onClick = { viewModel.setBioLength(l); viewModel.generateCurrentTool() },
                                                    label = { Text(l, fontSize = 11.sp) }
                                                )
                                            }
                                        }
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("Include Emojis", style = MaterialTheme.typography.bodyMedium)
                                            Switch(checked = bioEmoji, onCheckedChange = { viewModel.setBioEmoji(it); viewModel.generateCurrentTool() })
                                        }
                                    }
                                }

                                "Name Mixer" -> {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedTextField(
                                            value = mix1,
                                            onValueChange = { viewModel.setMixerInputs(it, mix2, mix3) },
                                            label = { Text("Name 1") },
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        OutlinedTextField(
                                            value = mix3,
                                            onValueChange = { viewModel.setMixerInputs(mix1, mix2, it) },
                                            label = { Text("Name 2 (Middle)") },
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        OutlinedTextField(
                                            value = mix2,
                                            onValueChange = { viewModel.setMixerInputs(mix1, it, mix3) },
                                            label = { Text("Name 3 (Last)") },
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        Button(
                                            onClick = { viewModel.mixNames(mix1, mix2, mix3) },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Text("MIX NAMES")
                                        }
                                    }
                                }

                                "Smart Assistant" -> {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedTextField(
                                            value = smartQuery,
                                            onValueChange = { viewModel.setSmartQuery(it) },
                                            label = { Text("Describe name", fontSize = 11.sp) },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        Text("RESULT COUNT: ${nicknameCount.toInt()}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                        Slider(
                                            value = nicknameCount,
                                            onValueChange = { viewModel.setNicknameCount(it) },
                                            valueRange = 5f..50f,
                                            steps = 8
                                        )
                                        Button(
                                            onClick = { viewModel.generateCurrentTool() },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Text("FIND NAMES")
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Bottom-Right Corner Resize Drag Handle
                    Box(
                        modifier = with(boxScope) {
                            Modifier
                                .align(Alignment.BottomEnd)
                                .padding(bottom = 2.dp, end = 2.dp)
                                .size(32.dp)
                                .clip(RoundedCornerShape(topStart = 12.dp, bottomEnd = 24.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f))
                                .pointerInput(Unit) {
                                    detectDragGestures { change, dragAmount ->
                                        change.consume()
                                        val maxW = this@BoxWithConstraints.maxWidth * 0.95f
                                        val maxH = this@BoxWithConstraints.maxHeight * 0.85f
                                        val newWidth = (customWidthDp + dragAmount.x.toDp()).coerceIn(260.dp, maxW)
                                        val newHeight = (customHeightDp + dragAmount.y.toDp()).coerceIn(280.dp, maxH)
                                        customWidthDp = newWidth
                                        customHeightDp = newHeight
                                    }
                                }
                        },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.OpenInFull,
                                contentDescription = "Resize Window",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
}
