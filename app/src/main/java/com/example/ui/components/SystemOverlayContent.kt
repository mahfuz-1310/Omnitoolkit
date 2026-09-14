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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.core.graphics.drawable.toBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.layout.layout
import androidx.compose.ui.zIndex
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.MainViewModel
import com.example.utils.*

@OptIn(ExperimentalMaterial3Api::class)

fun Modifier.zeroSize() = layout { measurable, constraints ->
    val placeable = measurable.measure(constraints)
    layout(0, 0) {
        placeable.place(0, 0)
    }
}

@Composable
fun InlineDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    AnimatedVisibility(
        visible = expanded,
        enter = fadeIn() + expandVertically(expandFrom = Alignment.Top),
        exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Top),
        modifier = modifier.zeroSize().zIndex(100f)
    ) {
        Card(
            modifier = Modifier
                .padding(top = 4.dp)
                .width(200.dp)
                .shadow(8.dp, RoundedCornerShape(8.dp)),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .heightIn(max = 300.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 8.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
fun InlineDropdownMenuItem(
    text: @Composable () -> Unit,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        text()
    }
}

@Composable
fun SystemOverlayContent(
    viewModel: MainViewModel,
    onDragDelta: (dx: Float, dy: Float) -> Unit,
    onMinimize: () -> Unit = {},
    onTogglePanel: () -> Unit = {},
    onLaunchApp: () -> Unit = {},
    onClose: () -> Unit = {}
) {
    var isExpanded by remember { mutableStateOf(false) }
    var showToolMenu by remember { mutableStateOf(false) }
    var showCountryDropdown by remember { mutableStateOf(false) }
    var smartQuery by remember { mutableStateOf("popular modern name") }
    
    var isCustomFirstFocused by remember { mutableStateOf(false) }
    var isCustomLastFocused by remember { mutableStateOf(false) }
    var isMix1Focused by remember { mutableStateOf(false) }
    var isMix2Focused by remember { mutableStateOf(false) }
    var isSmartQueryFocused by remember { mutableStateOf(false) }
    var isTextInputFocused by remember { mutableStateOf(false) }
    
    val isAnyFocused = isCustomFirstFocused || isCustomLastFocused || isMix1Focused || isMix2Focused || isSmartQueryFocused || isTextInputFocused
    
    LaunchedEffect(isAnyFocused) {
        viewModel.setFloatingWindowFocusable(isAnyFocused)
    }

    val context = LocalContext.current
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    // Shared StateFlows from ViewModel
    val activeTool by viewModel.activeTool.collectAsStateWithLifecycle()
    val selectedMode by viewModel.selectedMode.collectAsStateWithLifecycle()
    val selectedGender by viewModel.selectedGender.collectAsStateWithLifecycle()
    val selectedCountry by viewModel.selectedCountry.collectAsStateWithLifecycle()
    val selectedStyle by viewModel.selectedStyle.collectAsStateWithLifecycle()
    val selectedCount by viewModel.selectedCount.collectAsStateWithLifecycle()
    val noRepeat by viewModel.noRepeat.collectAsStateWithLifecycle()
    val customFirst by viewModel.customFirst.collectAsStateWithLifecycle()
    val customLast by viewModel.customLast.collectAsStateWithLifecycle()
    val currentResultText by viewModel.currentResultText.collectAsStateWithLifecycle()
    val generatedNames by viewModel.generatedNames.collectAsStateWithLifecycle()
    val isGenerating by viewModel.isGenerating.collectAsStateWithLifecycle()
    val fontStyle by viewModel.fontStyle.collectAsStateWithLifecycle()

    // Tool specific shared states
    val userStyle by viewModel.usernameStyle.collectAsStateWithLifecycle()
    val addNum by viewModel.usernameAddNum.collectAsStateWithLifecycle()
    val addUnder by viewModel.usernameAddUnder.collectAsStateWithLifecycle()
    val addDot by viewModel.usernameAddDot.collectAsStateWithLifecycle()
    val shortUsername by viewModel.usernameShort.collectAsStateWithLifecycle()
    val randomPrefix by viewModel.usernamePrefix.collectAsStateWithLifecycle()
    val randomSuffix by viewModel.usernameSuffix.collectAsStateWithLifecycle()
    
    val pwdLength by viewModel.passwordLength.collectAsStateWithLifecycle()
    val pwdUpper by viewModel.passwordUpper.collectAsStateWithLifecycle()
    val pwdLower by viewModel.passwordLower.collectAsStateWithLifecycle()
    val pwdNums by viewModel.passwordNums.collectAsStateWithLifecycle()
    val pwdSyms by viewModel.passwordSyms.collectAsStateWithLifecycle()
    val pwdExcludeSimilar by viewModel.passwordExcludeSimilar.collectAsStateWithLifecycle()
    val pwdExcludeAmbiguous by viewModel.passwordExcludeAmbiguous.collectAsStateWithLifecycle()

    val stylishInput by viewModel.stylishTextInput.collectAsStateWithLifecycle()
    val stylishFont by viewModel.stylishFont.collectAsStateWithLifecycle()
    val nicknameCat by viewModel.nicknameCategory.collectAsStateWithLifecycle()
    val bioCat by viewModel.bioCategory.collectAsStateWithLifecycle()
    val bioLength by viewModel.bioLength.collectAsStateWithLifecycle()
    val bioEmoji by viewModel.bioEmoji.collectAsStateWithLifecycle()
    val mix1 by viewModel.mixerName1.collectAsStateWithLifecycle()
    val mix2 by viewModel.mixerName2.collectAsStateWithLifecycle()

    val includeMiddle by viewModel.includeMiddle.collectAsStateWithLifecycle()
    val savedList by viewModel.savedList.collectAsStateWithLifecycle()
    val savedText by viewModel.savedText.collectAsStateWithLifecycle()
    val textInput by viewModel.textSaverInput.collectAsStateWithLifecycle()

    val isFav = viewModel.isFavorite(activeTool.uppercase(), currentResultText).collectAsStateWithLifecycle()

    fun copyText(text: String, label: String = "Copied!") {
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
        "Stylish Text", "Random Profile", "Smart Assistant", "Name Mixer", "Saver"
    )

    val appIconBitmap = remember(context) {
        try {
            val drawable = context.packageManager.getApplicationIcon(context.packageName)
            if (drawable is BitmapDrawable && drawable.bitmap != null) {
                drawable.bitmap.asImageBitmap()
            } else {
                val bmp = android.graphics.Bitmap.createBitmap(
                    if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else 128,
                    if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else 128,
                    android.graphics.Bitmap.Config.ARGB_8888
                )
                val canvas = android.graphics.Canvas(bmp)
                drawable.setBounds(0, 0, canvas.width, canvas.height)
                drawable.draw(canvas)
                bmp.asImageBitmap()
            }
        } catch (e: Exception) {
            null
        }
    }

    if (!isExpanded) {
        // Compact Floating Window
        Card(
            modifier = Modifier
                .width(330.dp)
                .shadow(12.dp, RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                // Dedicated Drag Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    onDragDelta(dragAmount.x, dragAmount.y)
                                }
                            )
                        }
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .weight(1f)
                    ) {
                        Icon(
                            Icons.Default.DragHandle,
                            contentDescription = "Drag Overlay",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
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
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            com.example.utils.AppConstants.APP_NAME,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = {
                                isExpanded = !isExpanded
                                onTogglePanel()
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                if (isExpanded) Icons.Default.CloseFullscreen else Icons.Default.OpenInFull,
                                contentDescription = if (isExpanded) "Collapse Panel" else "Expand Panel",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        IconButton(onClick = onMinimize, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Remove, contentDescription = "Minimize", modifier = Modifier.size(16.dp))
                        }
                        IconButton(onClick = onClose, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(16.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                ) {
                    when (activeTool) {
                        "Calculator" -> {
                            com.example.ui.screens.CalculatorView()
                        }
                        else -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .verticalScroll(rememberScrollState())
                            ) {
                    // Male / Female / Unisex Chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
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
                                        else -> "⚥ Unisex"
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                labelColor = MaterialTheme.colorScheme.onSurface,
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Result Box
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                activeTool.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Text(
                                FontStyles.apply(currentResultText, fontStyle),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                onClick = { copyText(currentResultText) },
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(12.dp))
                                Spacer(Modifier.width(2.dp))
                                Text("Copy", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                            
                            IconButton(
                                onClick = {
                                    if (isFav.value) viewModel.removeFavorite(activeTool.uppercase(), currentResultText)
                                    else viewModel.addFavorite(activeTool.uppercase(), currentResultText)
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    if (isFav.value) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                    contentDescription = "Favorite",
                                    modifier = Modifier.size(14.dp),
                                    tint = if (isFav.value) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                )
                            }
                            
                            TextButton(
                                onClick = { viewModel.generateCurrentTool() },
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                if (isGenerating) {
                                    CircularProgressIndicator(modifier = Modifier.size(12.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.primary)
                                } else {
                                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(Modifier.width(2.dp))
                                    Text("Regen", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
                }
                }
                }
                }
            }
        }
    } else {
        // Expanded Studio Window
        Box {
            val boxScope = this
            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .shadow(8.dp, RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp)
                ) {
                    // Draggable Header Area (Fixed at top)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                            .pointerInput(Unit) {
                                detectDragGestures(
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        onDragDelta(dragAmount.x, dragAmount.y)
                                    }
                                )
                            }
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .weight(1f)
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
                                com.example.utils.AppConstants.APP_NAME,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = {
                                    isExpanded = !isExpanded
                                    onTogglePanel()
                                },
                                modifier = Modifier.size(30.dp)
                            ) {
                                Icon(
                                    if (isExpanded) Icons.Default.CloseFullscreen else Icons.Default.OpenInFull,
                                    contentDescription = if (isExpanded) "Collapse Panel" else "Expand Panel",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            IconButton(onClick = onMinimize, modifier = Modifier.size(30.dp)) {
                                Icon(Icons.Default.Remove, contentDescription = "Minimize", modifier = Modifier.size(16.dp))
                            }
                            IconButton(onClick = onClose, modifier = Modifier.size(30.dp)) {
                                Icon(Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Content Area (Scrollable body)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(horizontal = 12.dp, vertical = 12.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {

                // Top Tool Selectors
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 4.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf("Name", "Username", "Password", "Saver", "Calculator").forEach { tool ->
                        val isSelected = activeTool == tool
                        Surface(
                            onClick = {
                                viewModel.setActiveTool(tool)
                                if (tool != "Saver" && tool != "Calculator") viewModel.generateCurrentTool()
                            },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.height(32.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = when(tool) {
                                        "Name" -> Icons.Default.Face
                                        "Username" -> Icons.Default.AlternateEmail
                                        "Password" -> Icons.Default.Password
                                        else -> Icons.Default.Save
                                    },
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    tool,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                    letterSpacing = 0.2.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Visible
                                )
                            }
                        }
                    }


                }

                // Hero Result Card
                // Result Box (only if not Saver or Calculator)
                if (activeTool !in listOf("Saver", "Calculator")) {
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
                                    Text("${generatedNames.size} Variations", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                                var showFontMenu by remember { mutableStateOf(false) }
                                Box {
                                    IconButton(
                                        onClick = { showFontMenu = true },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.FontDownload,
                                            contentDescription = "Font Style",
                                            modifier = Modifier.size(18.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    DropdownMenu(
                                        expanded = showFontMenu,
                                        onDismissRequest = { showFontMenu = false }
                                    ) {
                                        FontStyles.styles.forEach { style ->
                                            DropdownMenuItem(
                                                text = {
                                                    Text(
                                                        text = FontStyles.apply("Sample", style),
                                                        fontWeight = if (fontStyle == style) FontWeight.Bold else FontWeight.Normal,
                                                        color = if (fontStyle == style) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                                    )
                                                },
                                                onClick = {
                                                    viewModel.setFontStyle(style)
                                                    showFontMenu = false
                                                }
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.width(2.dp))
                                TextButton(
                                    onClick = { copyText(FontStyles.apply(currentResultText, fontStyle)) },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Copy", fontSize = 12.sp)
                                }
                                
                                IconButton(
                                    onClick = {
                                        val styledText = FontStyles.apply(currentResultText, fontStyle)
                                        if (isFav.value) viewModel.removeFavorite(activeTool.uppercase(), styledText)
                                        else viewModel.addFavorite(activeTool.uppercase(), styledText)
                                    },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        if (isFav.value) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                        contentDescription = "Favorite",
                                        tint = if (isFav.value) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
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
                }

                // Controls based on activeTool
                when (activeTool) {
                    "Name", "First + Middle", "Middle Name", "Surname" -> {
                        // 1. Gender Controls
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
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
                                                    else -> "⚥ Unisex"
                                                },
                                                fontSize = 11.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            )
                                        },
                                        modifier = Modifier.weight(1f),
                                        colors = FilterChipDefaults.filterChipColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                            labelColor = MaterialTheme.colorScheme.onSurface,
                                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                        )
                                    )
                                }
                            }
                        }

                        // 2. Mode Selector
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("MODE", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf("Full Name", "First Name", "Middle Name", "Surname").forEach { m ->
                                    FilterChip(
                                        selected = selectedMode == m,
                                        onClick = {
                                            viewModel.setMode(m)
                                            viewModel.generateCurrentTool()
                                        },
                                        label = { Text(m, fontSize = 11.sp, fontWeight = if (selectedMode == m) FontWeight.Bold else FontWeight.Normal) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                            labelColor = MaterialTheme.colorScheme.onSurface,
                                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                        )
                                    )
                                }
                            }
                        }

                        // 3. Country Selector
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("COUNTRY / ORIGIN", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Box(modifier = Modifier.fillMaxWidth()) {
                                OutlinedButton(
                                    onClick = { showCountryDropdown = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(selectedCountry, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                    }
                                }
                                InlineDropdownMenu(
                                    expanded = showCountryDropdown,
                                    onDismissRequest = { showCountryDropdown = false }
                                ) {
                                    CountryData.countries.forEach { c ->
                                        InlineDropdownMenuItem(
                                            text = { Text(c, fontWeight = if (c == selectedCountry) FontWeight.Bold else FontWeight.Normal) },
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

                        // 4. Style
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("STYLE", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf("Modern", "Cute", "Stylish", "Unique", "Classic", "Royal", "Gaming", "Aesthetic").forEach { s ->
                                    FilterChip(
                                        selected = selectedStyle == s,
                                        onClick = {
                                            viewModel.setStyle(s)
                                            viewModel.generateCurrentTool()
                                        },
                                        label = { Text(s, fontSize = 11.sp, fontWeight = if (selectedStyle == s) FontWeight.Bold else FontWeight.Normal) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                            labelColor = MaterialTheme.colorScheme.onSurface,
                                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                        )
                                    )
                                }
                            }
                        }

                        // 5. Custom Inputs
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = customFirst,
                                onValueChange = { viewModel.setCustomFirst(it) },
                                label = { Text("First Name", fontSize = 11.sp) },
                                modifier = Modifier.weight(1f).onFocusChanged { isCustomFirstFocused = it.isFocused },
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = customLast,
                                onValueChange = { viewModel.setCustomLast(it) },
                                label = { Text("Surname", fontSize = 11.sp) },
                                modifier = Modifier.weight(1f).onFocusChanged { isCustomLastFocused = it.isFocused },
                                singleLine = true
                            )
                        }

                        // 6. Quantity
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Quantity: $selectedCount", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Row(
                                modifier = Modifier.horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                listOf(1, 5, 10, 25, 50).forEach { cnt ->
                                    val isCountSelected = selectedCount == cnt
                                    FilterChip(
                                        selected = isCountSelected,
                                        onClick = {
                                            viewModel.setCount(cnt)
                                            viewModel.generateCurrentTool()
                                        },
                                        label = { Text("$cnt", fontSize = 10.sp, fontWeight = if (isCountSelected) FontWeight.Bold else FontWeight.Normal) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                            labelColor = MaterialTheme.colorScheme.onSurface,
                                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                        )
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Include Middle Name", style = MaterialTheme.typography.bodySmall)
                            Switch(checked = includeMiddle, onCheckedChange = { viewModel.setIncludeMiddle(it); viewModel.generateCurrentTool() }, modifier = Modifier.scale(0.8f))
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("No Repeat Session", style = MaterialTheme.typography.bodySmall)
                            Switch(checked = noRepeat, onCheckedChange = { viewModel.setNoRepeat(it) }, modifier = Modifier.scale(0.8f))
                        }

                        // 7. Results list
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
                                            copyText(allText, "Copied all names!")
                                        }) {
                                            Text("Copy All", fontSize = 11.sp)
                                        }
                                    }
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        generatedNames.take(6).forEachIndexed { idx, nameRes ->
                                            val styledName = FontStyles.apply(nameRes.name, fontStyle)
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 2.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text("${idx + 1}. $styledName", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                                                Row {
                                                    IconButton(onClick = { copyText(styledName) }, modifier = Modifier.size(24.dp)) {
                                                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.primary)
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
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Country for Username
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("COUNTRY", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Box(modifier = Modifier.fillMaxWidth()) {
                                    OutlinedButton(
                                        onClick = { showCountryDropdown = true },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                            Text(selectedCountry, style = MaterialTheme.typography.bodyMedium)
                                            Icon(Icons.Default.ArrowDropDown, null)
                                        }
                                    }
                                    InlineDropdownMenu(expanded = showCountryDropdown, onDismissRequest = { showCountryDropdown = false }) {
                                        CountryData.countries.forEach { c ->
                                            InlineDropdownMenuItem(text = { Text(c) }, onClick = { viewModel.setCountry(c); showCountryDropdown = false; viewModel.generateCurrentTool() })
                                        }
                                    }
                                }
                            }

                            // Gender for Username
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("GENDER", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    listOf("Male", "Female", "Unisex").forEach { g ->
                                        val isGSelected = selectedGender == g
                                        FilterChip(
                                            selected = isGSelected,
                                            onClick = { viewModel.setGender(g); viewModel.generateCurrentTool() },
                                            label = { Text(g, fontSize = 11.sp, fontWeight = if (isGSelected) FontWeight.Bold else FontWeight.Normal) },
                                            modifier = Modifier.weight(1f),
                                            colors = FilterChipDefaults.filterChipColors(
                                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                                labelColor = MaterialTheme.colorScheme.onSurface,
                                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                            )
                                        )
                                    }
                                }
                            }

                            Text("USERNAME STYLE", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Row(
                                modifier = Modifier.horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf("Random", "Aesthetic", "Cute", "Gaming", "Professional").forEach { s ->
                                    val isSSelected = userStyle == s
                                    FilterChip(
                                        selected = isSSelected,
                                        onClick = {
                                            viewModel.setUsernameStyle(s)
                                            viewModel.generateCurrentTool()
                                        },
                                        label = { Text(s, fontSize = 11.sp, fontWeight = if (isSSelected) FontWeight.Bold else FontWeight.Normal) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                            labelColor = MaterialTheme.colorScheme.onSurface,
                                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                        )
                                    )
                                }
                            }
                            
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("MODIFIERS", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                        Checkbox(checked = addNum, onCheckedChange = { viewModel.setUsernameAddNum(it); viewModel.generateCurrentTool() }, modifier = Modifier.scale(0.8f))
                                        Text("Numbers", fontSize = 10.sp, maxLines = 1)
                                    }
                                    Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                        Checkbox(checked = addUnder, onCheckedChange = { viewModel.setUsernameAddUnder(it); viewModel.generateCurrentTool() }, modifier = Modifier.scale(0.8f))
                                        Text("Under", fontSize = 10.sp, maxLines = 1)
                                    }
                                    Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                        Checkbox(checked = addDot, onCheckedChange = { viewModel.setUsernameAddDot(it); viewModel.generateCurrentTool() }, modifier = Modifier.scale(0.8f))
                                        Text("Dots", fontSize = 10.sp, maxLines = 1)
                                    }
                                }
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                        Checkbox(checked = shortUsername, onCheckedChange = { viewModel.setUsernameShort(it); viewModel.generateCurrentTool() }, modifier = Modifier.scale(0.8f))
                                        Text("Short", fontSize = 10.sp, maxLines = 1)
                                    }
                                    Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                        Checkbox(checked = randomPrefix, onCheckedChange = { viewModel.setUsernamePrefix(it); viewModel.generateCurrentTool() }, modifier = Modifier.scale(0.8f))
                                        Text("Prefix", fontSize = 10.sp, maxLines = 1)
                                    }
                                    Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                        Checkbox(checked = randomSuffix, onCheckedChange = { viewModel.setUsernameSuffix(it); viewModel.generateCurrentTool() }, modifier = Modifier.scale(0.8f))
                                        Text("Suffix", fontSize = 10.sp, maxLines = 1)
                                    }
                                }
                            }

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("No Repeat Session", style = MaterialTheme.typography.bodySmall)
                                Switch(checked = noRepeat, onCheckedChange = { viewModel.setNoRepeat(it) }, modifier = Modifier.scale(0.8f))
                            }
                        }
                    }

                    "Password" -> {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("PASSWORD LENGTH: ${pwdLength.toInt()}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Slider(
                                value = pwdLength,
                                onValueChange = {
                                    viewModel.setPasswordLength(it)
                                    viewModel.generateCurrentTool()
                                },
                                valueRange = 8f..32f
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Uppercase (A-Z)", style = MaterialTheme.typography.bodyMedium)
                                Switch(checked = pwdUpper, onCheckedChange = { viewModel.setPasswordUpper(it); viewModel.generateCurrentTool() })
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Numbers (0-9)", style = MaterialTheme.typography.bodyMedium)
                                Switch(checked = pwdNums, onCheckedChange = { viewModel.setPasswordNums(it); viewModel.generateCurrentTool() })
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Symbols (!@#)", style = MaterialTheme.typography.bodyMedium)
                                Switch(checked = pwdSyms, onCheckedChange = { viewModel.setPasswordSyms(it); viewModel.generateCurrentTool() })
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Exclude Similar (i, l, 1, o, 0)", style = MaterialTheme.typography.bodySmall)
                                Switch(checked = pwdExcludeSimilar, onCheckedChange = { viewModel.setPasswordExcludeSimilar(it); viewModel.generateCurrentTool() }, modifier = Modifier.scale(0.8f))
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Exclude Ambiguous ({}, [], (), /)", style = MaterialTheme.typography.bodySmall)
                                Switch(checked = pwdExcludeAmbiguous, onCheckedChange = { viewModel.setPasswordExcludeAmbiguous(it); viewModel.generateCurrentTool() }, modifier = Modifier.scale(0.8f))
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
                                    val isFSelected = stylishFont == f
                                    FilterChip(
                                        selected = isFSelected,
                                        onClick = {
                                            viewModel.setStylishFont(f)
                                            viewModel.generateCurrentTool()
                                        },
                                        label = { Text(f, fontSize = 11.sp, fontWeight = if (isFSelected) FontWeight.Bold else FontWeight.Normal) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                            labelColor = MaterialTheme.colorScheme.onSurface,
                                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                        )
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
                                listOf("Cute", "Cool", "Short", "Gamer", "Funny").forEach { c ->
                                    val isCSelected = nicknameCat == c
                                    FilterChip(
                                        selected = isCSelected,
                                        onClick = {
                                            viewModel.setNicknameCategory(c)
                                            viewModel.generateCurrentTool()
                                        },
                                        label = { Text(c, fontSize = 11.sp, fontWeight = if (isCSelected) FontWeight.Bold else FontWeight.Normal) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                            labelColor = MaterialTheme.colorScheme.onSurface,
                                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                        )
                                    )
                                }
                            }
                            
                            val nickCount by viewModel.nicknameCount.collectAsStateWithLifecycle()
                            Text("Quantity: ${nickCount.toInt()}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            Slider(
                                value = nickCount,
                                onValueChange = { viewModel.setNicknameCount(it); viewModel.generateCurrentTool() },
                                valueRange = 1f..50f,
                                steps = 9
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
                                listOf("Gamer", "Aesthetic", "Professional", "Creative").forEach { c ->
                                    val isCSelected = bioCat == c
                                    FilterChip(
                                        selected = isCSelected,
                                        onClick = {
                                            viewModel.setBioCategory(c)
                                            viewModel.generateCurrentTool()
                                        },
                                        label = { Text(c, fontSize = 11.sp, fontWeight = if (isCSelected) FontWeight.Bold else FontWeight.Normal) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                            labelColor = MaterialTheme.colorScheme.onSurface,
                                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                        )
                                    )
                                }
                            }

                            Text("BIO LENGTH", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Row(
                                modifier = Modifier.horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf("Short", "Medium", "Long").forEach { len ->
                                    val isLSelected = bioLength == len
                                    FilterChip(
                                        selected = isLSelected,
                                        onClick = {
                                            viewModel.setBioLength(len)
                                            viewModel.generateCurrentTool()
                                        },
                                        label = { Text(len, fontSize = 11.sp, fontWeight = if (isLSelected) FontWeight.Bold else FontWeight.Normal) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                            labelColor = MaterialTheme.colorScheme.onSurface,
                                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                        )
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
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = mix1,
                                    onValueChange = { viewModel.setMixerInputs(it, mix2) },
                                    label = { Text("Name 1") },
                                    modifier = Modifier.weight(1f).onFocusChanged { isMix1Focused = it.isFocused }
                                )
                                OutlinedTextField(
                                    value = mix2,
                                    onValueChange = { viewModel.setMixerInputs(mix1, it) },
                                    label = { Text("Name 2") },
                                    modifier = Modifier.weight(1f).onFocusChanged { isMix2Focused = it.isFocused }
                                )
                            }
                            Button(
                                onClick = { viewModel.mixNames(mix1, mix2) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Mix Names Together")
                            }
                        }
                    }

                    "Random Profile" -> {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.AccountCircle, null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
                            Text("Generate a complete random persona profile including name, username, bio, and details.", textAlign = androidx.compose.ui.text.style.TextAlign.Center, style = MaterialTheme.typography.bodySmall)
                            Button(
                                onClick = { viewModel.generateCurrentTool() },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Generate Full Profile")
                            }
                        }
                    }

                    "Smart Assistant" -> {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            OutlinedTextField(
                                value = smartQuery,
                                onValueChange = { smartQuery = it },
                                label = { Text("Search description") },
                                modifier = Modifier.fillMaxWidth().onFocusChanged { isSmartQueryFocused = it.isFocused }
                            )
                            Button(
                                onClick = {
                                    val res = SmartAssistant.findNames(smartQuery, 1).firstOrNull()?.name ?: "Aria"
                                    viewModel.setCurrentResultText(res)
                                    viewModel.addHistory("SMART", res)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Find Names with AI")
                            }
                        }
                    }

                    "Saver" -> {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = textInput,
                                onValueChange = { viewModel.setTextSaverInput(it) },
                                label = { Text("Enter text to save") },
                                modifier = Modifier.fillMaxWidth().onFocusChanged { isTextInputFocused = it.isFocused },
                                minLines = 2,
                                maxLines = 5,
                                shape = RoundedCornerShape(12.dp),
                                placeholder = { Text("Type or paste here...") }
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        if (textInput.isBlank()) {
                                            Toast.makeText(context, "Enter text first", Toast.LENGTH_SHORT).show()
                                        } else {
                                            viewModel.saveText()
                                            Toast.makeText(context, "Text Saved", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Icon(Icons.Default.Save, null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Save Text", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }

                                if (savedList.isNotEmpty()) {
                                    OutlinedButton(
                                        onClick = {
                                            viewModel.clearSavedText()
                                            Toast.makeText(context, "All items cleared", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                    ) {
                                        Icon(Icons.Default.DeleteSweep, null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text("Clear All", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            if (savedList.isNotEmpty()) {
                                Text(
                                    "SAVED ITEMS (${savedList.size})",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    savedList.forEachIndexed { index, itemText ->
                                        Surface(
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(12.dp),
                                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                                        ) {
                                            Column(modifier = Modifier.padding(10.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        "#${index + 1} • ${itemText.length} chars",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.primary,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Row {
                                                        IconButton(
                                                            onClick = { copyText(itemText, "Copied") },
                                                            modifier = Modifier.size(28.dp)
                                                        ) {
                                                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy Item", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(15.dp))
                                                        }
                                                        IconButton(
                                                            onClick = {
                                                                viewModel.deleteSavedItem(index)
                                                                Toast.makeText(context, "Item deleted", Toast.LENGTH_SHORT).show()
                                                            },
                                                            modifier = Modifier.size(28.dp)
                                                        ) {
                                                            Icon(Icons.Default.Delete, contentDescription = "Delete Item", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(15.dp))
                                                        }
                                                    }
                                                }
                                                Spacer(Modifier.height(2.dp))
                                                Text(
                                                    itemText,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    maxLines = 6,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "No saved items yet",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }
                    }
                    
                    "Calculator" -> {
                        com.example.ui.screens.CalculatorView()
                    }
                }
            }
        }
    }
}
}
}
