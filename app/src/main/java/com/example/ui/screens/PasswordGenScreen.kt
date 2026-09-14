package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import android.content.Intent
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Refresh
import com.example.ui.MainViewModel
import com.example.utils.PasswordGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordGenScreen(navController: NavController, viewModel: MainViewModel) {
    val passwordLength by viewModel.passwordLength.collectAsStateWithLifecycle()
    val uppercase by viewModel.passwordUpper.collectAsStateWithLifecycle()
    val lowercase by viewModel.passwordLower.collectAsStateWithLifecycle()
    val numbers by viewModel.passwordNums.collectAsStateWithLifecycle()
    val symbols by viewModel.passwordSyms.collectAsStateWithLifecycle()
    val excludeSimilar by viewModel.passwordExcludeSimilar.collectAsStateWithLifecycle()
    val excludeAmbiguous by viewModel.passwordExcludeAmbiguous.collectAsStateWithLifecycle()
    
    var generatedPassword by remember { mutableStateOf("") }
    var passwordStrength by remember { mutableStateOf("") }
    var showMenu by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    fun generate() {
        val len = passwordLength.toInt()
        val u = uppercase
        val l = lowercase
        val n = numbers
        val s = symbols
        val sim = excludeSimilar
        val amb = excludeAmbiguous

        coroutineScope.launch(kotlinx.coroutines.Dispatchers.Default) {
            val pwd = PasswordGenerator.generatePassword(len, u, l, n, s, sim, amb)
            val strength = PasswordGenerator.evaluateStrength(pwd)
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                generatedPassword = pwd
                passwordStrength = strength
                viewModel.addHistory("PASSWORD", pwd)
            }
        }
    }

    // Auto-generate on first launch if empty
    LaunchedEffect(Unit) {
        if (generatedPassword.isEmpty()) {
            val len = passwordLength.toInt()
            val u = uppercase
            val l = lowercase
            val n = numbers
            val s = symbols
            val sim = excludeSimilar
            val amb = excludeAmbiguous
            coroutineScope.launch(kotlinx.coroutines.Dispatchers.Default) {
                val pwd = PasswordGenerator.generatePassword(len, u, l, n, s, sim, amb)
                val strength = PasswordGenerator.evaluateStrength(pwd)
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    generatedPassword = pwd
                    passwordStrength = strength
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Password Generator", style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Result Card
            Box {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onTap = {
                                    if (generatedPassword.isNotEmpty()) {
                                        copyToClipboard(context, generatedPassword, "Password", viewModel)
                                    }
                                },
                                onLongPress = {
                                    if (generatedPassword.isNotEmpty()) {
                                        com.example.utils.SoundEffectManager.playClick()
                                        showMenu = true
                                    }
                                }
                            )
                        },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "YOUR PASSWORD",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = generatedPassword.ifEmpty { "..." },
                                style = MaterialTheme.typography.headlineLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Start,
                                modifier = Modifier.weight(1f),
                                letterSpacing = 2.sp
                            )
                            if (generatedPassword.isNotEmpty()) {
                                IconButton(
                                    onClick = {
                                        copyToClipboard(context, generatedPassword, "Password", viewModel)
                                    },
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    Icon(
                                        Icons.Default.ContentCopy,
                                        contentDescription = "Copy to Clipboard",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Segmented color-changing password strength indicator bar
                    val activeSegments = when (passwordStrength) {
                        "Weak" -> 1
                        "Medium" -> 2
                        "Strong" -> 3
                        "Very Strong" -> 4
                        else -> 0
                    }
                    val strengthColor = when (passwordStrength) {
                        "Weak" -> MaterialTheme.colorScheme.error
                        "Medium" -> Color(0xFFEAB308) // Amber/Yellow
                        "Strong" -> Color(0xFF22C55E) // Green
                        "Very Strong" -> Color(0xFF16A34A) // Darker Green
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("password_strength_bar"),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        for (i in 1..4) {
                            val isActive = i <= activeSegments
                            val segmentColor = if (isActive) strengthColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(6.dp)
                                    .background(
                                        color = segmentColor,
                                        shape = RoundedCornerShape(3.dp)
                                    )
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val entropy = PasswordGenerator.calculateEntropy(generatedPassword)
                        val strengthColor = when (passwordStrength) {
                            "Weak" -> MaterialTheme.colorScheme.error
                            "Medium" -> Color(0xFFEAB308)
                            "Strong" -> Color(0xFF22C55E)
                            "Very Strong" -> Color(0xFF16A34A)
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                        
                        Text(
                            text = "Strength: ${passwordStrength.uppercase()} (${entropy.toInt()} bits)",
                            color = strengthColor,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
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
                            copyToClipboard(context, generatedPassword, "Password", viewModel)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Share") },
                        leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) },
                        onClick = {
                            showMenu = false
                            com.example.utils.SoundEffectManager.playClick()
                            try {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    this.type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, generatedPassword)
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
                            showMenu = false
                            com.example.utils.SoundEffectManager.playClick()
                            generate()
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Length Slider
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Password Length", style = MaterialTheme.typography.titleMedium)
                Text("${passwordLength.toInt()}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Slider(
                value = passwordLength,
                onValueChange = { viewModel.setPasswordLength(it) },
                valueRange = 8f..64f,
                steps = 55,
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("8", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("64", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text("Options", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(16.dp))
            
            val options: List<Pair<String, Boolean>> = listOf(
                "Uppercase Letters" to uppercase,
                "Lowercase Letters" to lowercase,
                "Numbers" to numbers,
                "Symbols" to symbols,
                "Exclude Similar (i, l, 1, o, 0)" to excludeSimilar,
                "Exclude Ambiguous ({ } [ ] ( ) / \\ ' \" ` ~ , ; : . < >)" to excludeAmbiguous
            )
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    options.forEach { (label, value) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val checked = !value
                                    val count = listOf(uppercase, lowercase, numbers, symbols).count { it }
                                    val isCharType = label == "Uppercase Letters" || label == "Lowercase Letters" || label == "Numbers" || label == "Symbols"
                                    
                                    if (isCharType && count == 1 && !checked) {
                                        Toast.makeText(context, "Must select at least one character type", Toast.LENGTH_SHORT).show()
                                        return@clickable
                                    }
                                    
                                    when (label) {
                                        "Uppercase Letters" -> viewModel.setPasswordUpper(checked)
                                        "Lowercase Letters" -> viewModel.setPasswordLower(checked)
                                        "Numbers" -> viewModel.setPasswordNums(checked)
                                        "Symbols" -> viewModel.setPasswordSyms(checked)
                                        "Exclude Similar (i, l, 1, o, 0)" -> viewModel.setPasswordExcludeSimilar(checked)
                                        "Exclude Ambiguous ({ } [ ] ( ) / \\ ' \" ` ~ , ; : . < >)" -> viewModel.setPasswordExcludeAmbiguous(checked)
                                    }
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = value,
                                onCheckedChange = null,
                                colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = { generate() },
                modifier = Modifier.fillMaxWidth().height(64.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text("GENERATE PASSWORD", fontSize = 16.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
