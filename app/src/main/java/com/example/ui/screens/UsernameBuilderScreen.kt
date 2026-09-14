package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.ui.MainViewModel
import com.example.utils.UsernameGenerator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsernameBuilderScreen(navController: NavController, viewModel: MainViewModel) {
    val baseName by viewModel.builderBaseName.collectAsStateWithLifecycle()
    val prefix by viewModel.builderPrefix.collectAsStateWithLifecycle()
    val suffix by viewModel.builderSuffix.collectAsStateWithLifecycle()
    val addNumbers by viewModel.builderAddNumbers.collectAsStateWithLifecycle()
    val addUnderscore by viewModel.builderAddUnderscore.collectAsStateWithLifecycle()
    val addDot by viewModel.builderAddDot.collectAsStateWithLifecycle()
    val randomLetters by viewModel.builderRandomLetters.collectAsStateWithLifecycle()
    
    val generatedUsername = UsernameGenerator.buildUsername(baseName, prefix, suffix, addNumbers, addUnderscore, addDot, randomLetters)
    
    val context = LocalContext.current
    val isFav by viewModel.isFavorite("USERNAME", generatedUsername).collectAsStateWithLifecycle(initialValue = false)
    var showAvailabilityChecker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Username Builder", style = MaterialTheme.typography.titleMedium) },
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
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("Live Preview", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (generatedUsername.isEmpty()) "@username" else "@$generatedUsername",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.weight(1f)
                        )
                        if (generatedUsername.isNotEmpty()) {
                            IconButton(onClick = {
                                com.example.utils.SoundEffectManager.playClick(context)
                                showAvailabilityChecker = true
                            }) {
                                Icon(Icons.Default.Public, contentDescription = "Check Availability")
                            }
                            IconButton(onClick = {
                                copyToClipboard(context, generatedUsername, "Username", viewModel)
                                viewModel.addHistory("USERNAME", generatedUsername)
                            }) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                            }
                            IconButton(onClick = {
                                if (isFav) viewModel.removeFavorite("USERNAME", generatedUsername)
                                else viewModel.addFavorite("USERNAME", generatedUsername)
                            }) {
                                Icon(
                                    if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = "Favorite",
                                    tint = if (isFav) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            OutlinedTextField(
                value = baseName,
                onValueChange = { viewModel.setBuilderBaseName(it) },
                label = { Text("Base Name") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = prefix,
                    onValueChange = { viewModel.setBuilderPrefix(it) },
                    label = { Text("Prefix (Opt)") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true
                )
                OutlinedTextField(
                    value = suffix,
                    onValueChange = { viewModel.setBuilderSuffix(it) },
                    label = { Text("Suffix (Opt)") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text("Modifiers", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Add Numbers", style = MaterialTheme.typography.bodyLarge)
                Switch(checked = addNumbers, onCheckedChange = { viewModel.setBuilderAddNumbers(it) })
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Add Underscore (_)", style = MaterialTheme.typography.bodyLarge)
                Switch(checked = addUnderscore, onCheckedChange = { viewModel.setBuilderAddUnderscore(it) })
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Add Dot (.)", style = MaterialTheme.typography.bodyLarge)
                Switch(checked = addDot, onCheckedChange = { viewModel.setBuilderAddDot(it) })
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Random Letters", style = MaterialTheme.typography.bodyLarge)
                Switch(checked = randomLetters, onCheckedChange = { viewModel.setBuilderRandomLetters(it) })
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }

        com.example.ui.components.UsernameAvailabilityBottomSheet(
            visible = showAvailabilityChecker,
            username = generatedUsername,
            onDismissRequest = { showAvailabilityChecker = false }
        )
    }
}
