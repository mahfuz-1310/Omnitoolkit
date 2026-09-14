package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.ui.MainViewModel
import com.example.utils.CountryData
import com.example.utils.ModernGenerators
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FirstMiddleGenScreen(navController: NavController, viewModel: MainViewModel) {
    val country by viewModel.selectedCountry.collectAsStateWithLifecycle()
    val gender by viewModel.selectedGender.collectAsStateWithLifecycle()
    val genders = listOf("Male", "Female", "Unisex")
    val style by viewModel.selectedStyle.collectAsStateWithLifecycle()
    val styles = listOf("Modern", "Cute", "Stylish", "Unique", "Classic", "Royal")
    val count by viewModel.selectedCount.collectAsStateWithLifecycle()
    val counts = listOf(10, 25, 50, 100, 250, 500)
    val noRepeat by viewModel.noRepeat.collectAsStateWithLifecycle()

    var items by remember { mutableStateOf<List<ModernGenerators.FirstMiddleItem>>(emptyList()) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val fontStyle by viewModel.fontStyle.collectAsStateWithLifecycle()
    var showFontSelector by remember { mutableStateOf(false) }
    var showCountryMenu by remember { mutableStateOf(false) }

    fun generate() {
        val currentCountry = country
        val currentGender = gender
        val currentStyle = style
        val currentCount = count
        val currentNoRepeat = noRepeat

        coroutineScope.launch(kotlinx.coroutines.Dispatchers.Default) {
            val generated = ModernGenerators.generateFirstMiddle(
                country = currentCountry,
                gender = currentGender,
                style = currentStyle,
                count = currentCount,
                noRepeatSession = currentNoRepeat
            )
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                if (currentNoRepeat && generated.size < currentCount) {
                    Toast.makeText(context, "Only ${generated.size} unique combinations available.", Toast.LENGTH_SHORT).show()
                }
                items = generated
                if (items.isNotEmpty()) {
                    viewModel.addHistory("FIRST_MIDDLE", com.example.utils.FontStyles.apply(items.first().fullName, fontStyle))
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        if (items.isEmpty()) {
            generate()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("First + Middle Name", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                        Text("Generate a perfect first and middle name combination", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                    Text("✨ Generate First + Middle Name", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // Country
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Country", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(
                        value = country,
                        onValueChange = {},
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showCountryMenu = true },
                        shape = RoundedCornerShape(12.dp),
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
                                                generate()
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    )
                }
            }

            // Gender
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Gender", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    SegmentedControl(options = genders, selected = gender, onSelected = { viewModel.setGender(it); generate() })
                }
            }

            // Style
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                                onClick = { viewModel.setStyle(s); generate() },
                                label = { Text(s) }
                            )
                        }
                    }
                }
            }

            // Count
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

            // No Repeat
            item {
                SwitchRow(label = "No Repeat (Session)", checked = noRepeat, onCheckedChange = { viewModel.setNoRepeat(it) })
            }

            item {
                Text("Combinations", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }

            itemsIndexed(items) { index, item ->
                val styledFullName = com.example.utils.FontStyles.apply(item.fullName, fontStyle)
                val styledFirst = com.example.utils.FontStyles.apply(item.firstName, fontStyle)
                val styledMiddle = com.example.utils.FontStyles.apply(item.middleName, fontStyle)
                val isFav by viewModel.isFavorite("FIRST_MIDDLE", item.fullName).collectAsStateWithLifecycle(initialValue = false)

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${index + 1}. ${item.country} • ${item.gender} • ${item.style}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(
                                onClick = {
                                    val newFirst = ModernGenerators.getRandomFirstName(country, gender)
                                    val newMiddle = ModernGenerators.getRandomMiddleName(country, gender)
                                    val updated = items.toMutableList()
                                    updated[index] = item.copy(firstName = newFirst, middleName = newMiddle)
                                    items = updated
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = "Regenerate Both", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = styledFullName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(8.dp))

                        // Independent regeneration of First & Middle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column {
                                    Text("First Name", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(styledFirst, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                IconButton(
                                    onClick = {
                                        var newFirst = ModernGenerators.getRandomFirstName(country, gender)
                                        while (newFirst.equals(item.middleName, ignoreCase = true)) {
                                            newFirst = ModernGenerators.getRandomFirstName(country, gender)
                                        }
                                        val updated = items.toMutableList()
                                        updated[index] = item.copy(firstName = newFirst)
                                        items = updated
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = "Regenerate First", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Middle Name", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(styledMiddle, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                IconButton(
                                    onClick = {
                                        var newMiddle = ModernGenerators.getRandomMiddleName(country, gender)
                                        while (newMiddle.equals(item.firstName, ignoreCase = true)) {
                                            newMiddle = ModernGenerators.getRandomMiddleName(country, gender)
                                        }
                                        val updated = items.toMutableList()
                                        updated[index] = item.copy(middleName = newMiddle)
                                        items = updated
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = "Regenerate Middle", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = {
                                copyToClipboard(context, styledFullName, "First + Middle", viewModel)
                                viewModel.addHistory("FIRST_MIDDLE", styledFullName)
                            }) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            IconButton(onClick = {
                                if (isFav) viewModel.removeFavorite("FIRST_MIDDLE", item.fullName)
                                else viewModel.addFavorite("FIRST_MIDDLE", styledFullName)
                            }) {
                                Icon(
                                    if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = "Favorite",
                                    tint = if (isFav) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }

    FontStyleBottomSheet(
        visible = showFontSelector,
        onDismissRequest = { showFontSelector = false },
        currentStyle = fontStyle,
        onStyleSelected = { viewModel.setFontStyle(it) }
    )
}
