package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
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
import com.example.ui.MainViewModel
import com.example.utils.CountryData
import com.example.utils.ModernGenerators
import com.example.utils.SessionCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsernameGenScreen(navController: NavController, viewModel: MainViewModel) {
    
    val fontStyle by viewModel.fontStyle.collectAsStateWithLifecycle()
    var showFontSelector by remember { mutableStateOf(false) }

    val country by viewModel.selectedCountry.collectAsStateWithLifecycle()
    val gender by viewModel.selectedGender.collectAsStateWithLifecycle()
    val genders = listOf("Male", "Female", "Unisex")
    
    val selectedStyle by viewModel.usernameStyle.collectAsStateWithLifecycle()
    val styles = listOf("Random", "Aesthetic", "Cute", "Gaming")
    
    val addNumbers by viewModel.usernameAddNum.collectAsStateWithLifecycle()
    val addUnderscore by viewModel.usernameAddUnder.collectAsStateWithLifecycle()
    val addDot by viewModel.usernameAddDot.collectAsStateWithLifecycle()
    val shortUsername by viewModel.usernameShort.collectAsStateWithLifecycle()
    val randomPrefix by viewModel.usernamePrefix.collectAsStateWithLifecycle()
    val randomSuffix by viewModel.usernameSuffix.collectAsStateWithLifecycle()
    
    val noRepeat by viewModel.noRepeat.collectAsStateWithLifecycle()

    var generatedUsernames by remember { mutableStateOf<List<String>>(emptyList()) }
    var showAvailabilityChecker by remember { mutableStateOf(false) }
    var selectedHandleForChecker by remember { mutableStateOf("") }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    fun generate() {
        val curStyle = selectedStyle
        val curCountry = country
        val curGender = gender
        val curAddNums = addNumbers
        val curAddUnderscore = addUnderscore
        val curAddDot = addDot
        val curShort = shortUsername
        val curPrefix = randomPrefix
        val curSuffix = randomSuffix
        val curNoRepeat = noRepeat

        coroutineScope.launch(kotlinx.coroutines.Dispatchers.Default) {
            val results = ModernGenerators.generateUsername(
                style = curStyle,
                country = curCountry,
                gender = curGender,
                addNumbers = curAddNums,
                addUnderscore = curAddUnderscore,
                addDot = curAddDot,
                shortUsername = curShort,
                randomPrefix = curPrefix,
                randomSuffix = curSuffix,
                noRepeatSession = curNoRepeat,
                count = 15
            )
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                generatedUsernames = results
                if (results.isNotEmpty()) {
                    viewModel.addHistory("USERNAME", com.example.utils.FontStyles.apply(results.first(), fontStyle))
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        if (generatedUsernames.isEmpty()) {
            generate()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Username Generator", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showFontSelector = true }) {
                        Icon(Icons.Default.FontDownload, "Font Style")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
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
                    Text("✨ Generate Username", fontSize = 16.sp, fontWeight = FontWeight.Bold)
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
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // HERO RESULT
            if (generatedUsernames.isNotEmpty()) {
                item {
                    val heroResult = generatedUsernames.first()
                    Card(
                        modifier = Modifier.fillMaxWidth(),
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
                                "GENERATED USERNAME",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "@${heroResult}",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "🚩 ${country} • ${gender} • ${selectedStyle}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                val isFav by viewModel.isFavorite("USERNAME", com.example.utils.FontStyles.apply(heroResult, fontStyle)).collectAsStateWithLifecycle(initialValue = false)
                                
                                HeroActionButton(
                                    icon = Icons.Default.ContentCopy,
                                    label = "Copy",
                                    onClick = {
                                        copyToClipboard(context, com.example.utils.FontStyles.apply(heroResult, fontStyle), "Username", viewModel)
                                    }
                                )
                                HeroActionButton(
                                    icon = if(isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    label = "Favorite",
                                    tint = if(isFav) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                    onClick = {
                                        if(isFav) viewModel.removeFavorite("USERNAME", com.example.utils.FontStyles.apply(heroResult, fontStyle))
                                        else viewModel.addFavorite("USERNAME", com.example.utils.FontStyles.apply(heroResult, fontStyle))
                                    }
                                )
                                HeroActionButton(
                                    icon = Icons.Default.Public,
                                    label = "Check",
                                    onClick = {
                                        selectedHandleForChecker = heroResult
                                        showAvailabilityChecker = true
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
                                selected = selectedStyle == s,
                                onClick = { viewModel.setUsernameStyle(s) },
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
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Session Settings", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    SwitchRow("No Repeat (Unique Session)", noRepeat) { viewModel.setNoRepeat(it) }
                    OutlinedButton(
                        onClick = { SessionCache.resetUsernames(); Toast.makeText(context, "Session history reset!", Toast.LENGTH_SHORT).show() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Reset Generated Results")
                    }
                }
            }
            
            item {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Modifiers", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    
                    SwitchRow("Numbers", addNumbers) { viewModel.setUsernameAddNum(it) }
                    SwitchRow("Underscore", addUnderscore) { viewModel.setUsernameAddUnder(it) }
                    SwitchRow("Dot", addDot) { viewModel.setUsernameAddDot(it) }
                    SwitchRow("Short", shortUsername) { viewModel.setUsernameShort(it) }
                    SwitchRow("Random Prefix", randomPrefix) { viewModel.setUsernamePrefix(it) }
                    SwitchRow("Random Suffix", randomSuffix) { viewModel.setUsernameSuffix(it) }
                }
            }

            // LIST
            if (generatedUsernames.size > 1) {
                item {
                    Text("More Results", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 8.dp))
                }
                itemsIndexed(generatedUsernames.drop(1)) { index, result ->
                    ResultRowItem(
                        index = index + 2,
                        name = "@$result",
                        viewModel = viewModel,
                        context = context,
                        type = "USERNAME",
                        onRegenerate = { generate() }
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

    com.example.ui.components.UsernameAvailabilityBottomSheet(
        visible = showAvailabilityChecker,
        username = selectedHandleForChecker,
        onDismissRequest = { showAvailabilityChecker = false }
    )
}
