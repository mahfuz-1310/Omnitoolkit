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
import com.example.utils.NameResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MiddleNameGenScreen(navController: NavController, viewModel: MainViewModel) {
    val country by viewModel.selectedCountry.collectAsStateWithLifecycle()
    val gender by viewModel.selectedGender.collectAsStateWithLifecycle()
    val genders = listOf("Male", "Female", "Unisex")
    val style by viewModel.selectedStyle.collectAsStateWithLifecycle()
    val styles = listOf("Modern", "Cute", "Stylish", "Unique", "Classic", "Royal", "Gaming", "Aesthetic")
    val count by viewModel.selectedCount.collectAsStateWithLifecycle()
    val counts = listOf(10, 25, 50, 100, 250, 500)
    val noRepeat by viewModel.noRepeat.collectAsStateWithLifecycle()
    
    val generatedResults by viewModel.generatedNames.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val fontStyle by viewModel.fontStyle.collectAsStateWithLifecycle()
    var showFontSelector by remember { mutableStateOf(false) }
    var showCountryMenu by remember { mutableStateOf(false) }

    fun generate() {
        viewModel.generateNames()
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
                        Text("Middle Name Generator", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                        Text("Generate middle names", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                    Text("✨ Generate Middle Names", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }
            
            // Country selector
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

            // No repeat switch
            item {
                SwitchRow(label = "No Repeat (Session)", checked = noRepeat, onCheckedChange = { viewModel.setNoRepeat(it) })
            }

            item {
                Text("Generated Middle Names", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }

            itemsIndexed(generatedResults) { index, result ->
                val styledText = com.example.utils.FontStyles.apply(result.name, fontStyle)
                ResultRowItem(
                    index = index + 1,
                    name = styledText,
                    viewModel = viewModel,
                    context = context,
                    type = "MIDDLE_NAME",
                    onRegenerate = { generate() }
                )
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
