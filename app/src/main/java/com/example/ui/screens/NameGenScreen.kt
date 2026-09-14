package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import android.content.Intent
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import com.example.ui.MainViewModel
import com.example.utils.ModernGenerators
import com.example.utils.NameResult
import com.example.utils.SessionCache
import com.example.utils.CountryData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NameGenScreen(navController: NavController, viewModel: MainViewModel) {
    val mode by viewModel.selectedMode.collectAsStateWithLifecycle()
    val modes = listOf("Full Name", "First Name", "Middle Name", "Last Name")

    val country by viewModel.selectedCountry.collectAsStateWithLifecycle()

    val gender by viewModel.selectedGender.collectAsStateWithLifecycle()
    val genders = listOf("Male", "Female", "Unisex")

    val style by viewModel.selectedStyle.collectAsStateWithLifecycle()
    val styles = listOf("Modern", "Cute", "Stylish", "Unique", "Classic", "Royal", "Gaming", "Aesthetic")

    val lastNameCategory by viewModel.lastNameCategory.collectAsStateWithLifecycle()
    val lastNameCategories = listOf("Random", "Popular", "Modern", "Rare", "Royal", "International")

    val customFirst by viewModel.customFirst.collectAsStateWithLifecycle()
    val customMiddle by viewModel.customMiddle.collectAsStateWithLifecycle()
    val customLast by viewModel.customLast.collectAsStateWithLifecycle()
    
    val count by viewModel.selectedCount.collectAsStateWithLifecycle()
    val counts = listOf(10, 25, 50, 100, 250, 500)
    val includeMiddle by viewModel.includeMiddle.collectAsStateWithLifecycle()
    val noRepeat by viewModel.noRepeat.collectAsStateWithLifecycle()
    
    val nameLength by viewModel.selectedNameLength.collectAsStateWithLifecycle()
    val lengths = listOf("Short", "Medium", "Long")
    
    val initialsOnly by viewModel.initialsOnly.collectAsStateWithLifecycle()
    val excludeDuplicates by viewModel.excludeDuplicates.collectAsStateWithLifecycle()
    val useRareNames by viewModel.useRareNames.collectAsStateWithLifecycle()
    val includeMeaning by viewModel.includeMeaning.collectAsStateWithLifecycle()

    var isAdvancedExpanded by remember { mutableStateOf(false) }

    val generatedResults by viewModel.generatedNames.collectAsStateWithLifecycle()
    
    val context = LocalContext.current
    val fontStyle by viewModel.fontStyle.collectAsStateWithLifecycle()
    var showFontSelector by remember { mutableStateOf(false) }
    var showHeroMenu by remember { mutableStateOf(false) }

    val hasCustomInput = customFirst.isNotBlank() || customMiddle.isNotBlank() || customLast.isNotBlank()

    fun generate() {
        viewModel.generateNames { results ->
            if (noRepeat && results.size < count) {
                Toast.makeText(context, "Only ${results.size} unique combinations available.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    LaunchedEffect(Unit) {
        if (generatedResults.isEmpty()) {
            generate()
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Name Generator", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                        Text("Create your perfect name", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showFontSelector = true }) {
                        Icon(Icons.Default.FontDownload, "Font Style")
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.background,
                tonalElevation = 8.dp,
                shadowElevation = 8.dp
            ) {
                PaddingValues(16.dp)
                Button(
                    onClick = { generate() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if(hasCustomInput) "Generate Variations" else "✨ Generate Name", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }            // HERO RESULT
            if (generatedResults.isNotEmpty()) {
                item {
                    val heroResult = generatedResults.first()
                    Box {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .pointerInput(Unit) {
                                    detectTapGestures(
                                        onTap = {
                                            val textToCopy = com.example.utils.FontStyles.apply(heroResult.name, fontStyle)
                                            copyToClipboard(context, textToCopy, "Name", viewModel)
                                        },
                                        onLongPress = {
                                            com.example.utils.SoundEffectManager.playClick()
                                            showHeroMenu = true
                                        }
                                    )
                                },
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            elevation = CardDefaults.cardElevation(0.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    "GENERATED NAME",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = com.example.utils.FontStyles.apply(heroResult.name, fontStyle),
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "🚩 ${country} • ${gender} • ${style}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(24.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    val isFav by viewModel.isFavorite("NAME", com.example.utils.FontStyles.apply(heroResult.name, fontStyle)).collectAsStateWithLifecycle(initialValue = false)
                                    
                                    HeroActionButton(
                                        icon = Icons.Default.ContentCopy,
                                        label = "Copy",
                                        onClick = {
                                            val textToCopy = com.example.utils.FontStyles.apply(heroResult.name, fontStyle)
                                            copyToClipboard(context, textToCopy, "Name", viewModel)
                                        }
                                    )
                                    HeroActionButton(
                                        icon = if(isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                        label = "Favorite",
                                        tint = if(isFav) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                        onClick = {
                                            if(isFav) viewModel.removeFavorite("NAME", com.example.utils.FontStyles.apply(heroResult.name, fontStyle))
                                            else viewModel.addFavorite("NAME", com.example.utils.FontStyles.apply(heroResult.name, fontStyle))
                                        }
                                    )
                                    HeroActionButton(
                                        icon = Icons.Default.Refresh,
                                        label = "Regenerate",
                                        onClick = { generate() }
                                    )
                                }
                            }
                        }

                        DropdownMenu(
                            expanded = showHeroMenu,
                            onDismissRequest = { showHeroMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Copy") },
                                leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null) },
                                onClick = {
                                    showHeroMenu = false
                                    val textToCopy = com.example.utils.FontStyles.apply(heroResult.name, fontStyle)
                                    copyToClipboard(context, textToCopy, "Name", viewModel)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Share") },
                                leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) },
                                onClick = {
                                    showHeroMenu = false
                                    com.example.utils.SoundEffectManager.playClick()
                                    try {
                                        val textToCopy = com.example.utils.FontStyles.apply(heroResult.name, fontStyle)
                                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                            this.type = "text/plain"
                                            putExtra(Intent.EXTRA_TEXT, textToCopy)
                                        }
                                        context.startActivity(Intent.createChooser(shareIntent, "Share with"))
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Regenerate") },
                                leadingIcon = { Icon(Icons.Default.Refresh, contentDescription = null) },
                                onClick = {
                                    showHeroMenu = false
                                    com.example.utils.SoundEffectManager.playClick()
                                    generate()
                                }
                            )
                        }
                    }
                }
            }

            // CONTROLS
            item {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Generation Mode", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    SegmentedControl(options = modes, selected = mode, onSelected = { viewModel.setMode(it) })
                }
            }
            
            // COUNTRY SELECTOR
            item {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Country", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    var showCountryMenu by remember { mutableStateOf(false) }
                    
                    OutlinedTextField(
                        value = country,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth().clickable { showCountryMenu = true },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledBorderColor = MaterialTheme.colorScheme.outline,
                            disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        enabled = false,
                        trailingIcon = {
                            Box {
                                IconButton(onClick = { showCountryMenu = true }) {
                                    Icon(Icons.Default.ArrowDropDown, "Select Country")
                                }
                                DropdownMenu(
                                    expanded = showCountryMenu,
                                    onDismissRequest = { showCountryMenu = false }
                                ) {
                                    CountryData.countries.forEach { cat ->
                                        DropdownMenuItem(
                                            text = { Text(cat) },
                                            onClick = {
                                                viewModel.setCountry(cat)
                                                showCountryMenu = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    )
                }
            }
            
            item {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Gender", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    SegmentedControl(options = genders, selected = gender, onSelected = { viewModel.setGender(it) })
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Style", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        styles.forEach { s ->
                            FilterChip(
                                selected = style == s,
                                onClick = { viewModel.setStyle(s) },
                                label = { Text(s) },
                                shape = CircleShape,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }
                    }
                }
            }
            
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Quantity", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        counts.forEach { c ->
                            val isSelected = count == c
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { viewModel.setCount(c); generate() }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = c.toString(),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
            
            item {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Session Settings", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    SwitchRow("No Repeat (Unique Session)", noRepeat) { viewModel.setNoRepeat(it) }
                    OutlinedButton(
                        onClick = { SessionCache.resetNames(); SessionCache.resetMiddleNames(); Toast.makeText(context, "Session history reset!", Toast.LENGTH_SHORT).show() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Reset Generated Results & Middle Names")
                    }
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Name Parts", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    
                    if (mode == "Full Name" || mode == "First Name") {
                        OutlinedTextField(
                            value = customFirst,
                            onValueChange = { viewModel.setCustomFirst(it) },
                            label = { Text("First Name") },
                            placeholder = { Text("Random First Name") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            trailingIcon = {
                                IconButton(onClick = { 
                                    val gen = ModernGenerators.generateName("First Name", gender, country, style, lastNameCategory, false, "", "", "", 1, nameLength, initialsOnly, excludeDuplicates, noRepeat).firstOrNull()?.name ?: ""
                                    viewModel.setCustomFirst(gen)
                                }) {
                                    Icon(Icons.Default.Refresh, "Regenerate First Name")
                                }
                            }
                        )
                    }

                    if (mode == "Full Name" || mode == "Middle Name") {
                        if (mode == "Full Name") {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = includeMiddle,
                                    onCheckedChange = { viewModel.setIncludeMiddle(it) },
                                    colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                                )
                                Text("Include Middle Name", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                        
                        if (includeMiddle || mode == "Middle Name") {
                            OutlinedTextField(
                                value = customMiddle,
                                onValueChange = { viewModel.setCustomMiddle(it) },
                                label = { Text("Middle Name") },
                                placeholder = { Text("Random Middle Name") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                trailingIcon = {
                                    IconButton(onClick = { 
                                        val gen = ModernGenerators.generateName("Middle Name", gender, country, style, lastNameCategory, true, "", "", "", 1, nameLength, initialsOnly, excludeDuplicates, noRepeat).firstOrNull()?.name ?: ""
                                        viewModel.setCustomMiddle(gen)
                                    }) {
                                        Icon(Icons.Default.Refresh, "Regenerate Middle Name")
                                    }
                                }
                            )
                        }
                    }

                    if (mode == "Full Name" || mode == "Last Name") {
                        var showCategoryMenu by remember { mutableStateOf(false) }
                        
                        OutlinedTextField(
                            value = customLast,
                            onValueChange = { viewModel.setCustomLast(it) },
                            label = { Text("Last Name") },
                            placeholder = { Text("Random $lastNameCategory Last Name") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            trailingIcon = {
                                Row {
                                    IconButton(onClick = { 
                                        val gen = ModernGenerators.generateName("Last Name", gender, country, style, lastNameCategory, false, "", "", "", 1, nameLength, initialsOnly, excludeDuplicates, noRepeat).firstOrNull()?.name ?: ""
                                        viewModel.setCustomLast(gen)
                                    }) {
                                        Icon(Icons.Default.Refresh, "Regenerate Last Name")
                                    }
                                    Box {
                                        IconButton(onClick = { showCategoryMenu = true }) {
                                            Icon(Icons.Default.ArrowDropDown, "Select Category")
                                        }
                                        DropdownMenu(
                                            expanded = showCategoryMenu,
                                            onDismissRequest = { showCategoryMenu = false }
                                        ) {
                                            lastNameCategories.forEach { cat ->
                                                DropdownMenuItem(
                                                    text = { Text(cat) },
                                                    onClick = {
                                                        viewModel.setLastNameCategory(cat)
                                                        showCategoryMenu = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
            }
            // ADVANCED OPTIONS
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    onClick = { isAdvancedExpanded = !isAdvancedExpanded }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Advanced Options", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Icon(
                                if(isAdvancedExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = null
                            )
                        }
                        
                        AnimatedVisibility(visible = isAdvancedExpanded) {
                            Column(modifier = Modifier.padding(top = 16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                Text("Name Length", style = MaterialTheme.typography.bodyMedium)
                                SegmentedControl(options = lengths, selected = nameLength, onSelected = { viewModel.setNameLength(it) })
                                
                                SwitchRow("Initials Only", initialsOnly) { viewModel.setInitialsOnly(it) }
                                SwitchRow("Exclude Duplicates", excludeDuplicates) { viewModel.setExcludeDuplicates(it) }
                                SwitchRow("Use Rare Names", useRareNames) { viewModel.setUseRareNames(it) }
                                SwitchRow("Include Meaning", includeMeaning) { viewModel.setIncludeMeaning(it) }
                            }
                        }
                    }
                }
            }

            // LIST
            if (generatedResults.size > 1) {
                item {
                    Text("More Results", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 8.dp))
                }
                itemsIndexed(generatedResults.drop(1)) { index, result ->
                    ResultRowItem(
                        index = index + 2,
                        name = com.example.utils.FontStyles.apply(result.name, fontStyle),
                        viewModel = viewModel,
                        context = context,
                        onRegenerate = { viewModel.generateNames() }
                    )
                }
            }
            
            item { Spacer(modifier = Modifier.height(80.dp)) } // padding for bottom bar
        }
    }
    FontStyleBottomSheet(
        visible = showFontSelector,
        onDismissRequest = { showFontSelector = false },
        currentStyle = fontStyle,
        onStyleSelected = { viewModel.setFontStyle(it) }
    )
}
