package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.ui.MainViewModel
import com.example.utils.BioGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BioGenScreen(navController: NavController, viewModel: MainViewModel) {
    val categories = listOf("Instagram", "TikTok", "Gaming", "YouTube", "Professional", "Aesthetic", "Funny", "Minimal")
    val selectedCategory by viewModel.bioCategory.collectAsStateWithLifecycle()
    
    val lengths = listOf("Short", "Medium", "Long")
    val selectedLength by viewModel.bioLength.collectAsStateWithLifecycle()
    
    val useEmoji by viewModel.bioEmoji.collectAsStateWithLifecycle()
    
    var generatedBios by remember { mutableStateOf<List<String>>(emptyList()) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        if (generatedBios.isEmpty()) {
            val cat = selectedCategory
            val len = selectedLength
            val emoji = useEmoji
            coroutineScope.launch(kotlinx.coroutines.Dispatchers.Default) {
                val bios = List(5) { BioGenerator.generateBio(cat, len, emoji) }
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    generatedBios = bios
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bio Generator", style = MaterialTheme.typography.titleMedium) },
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
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            Text("Category", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { viewModel.setBioCategory(cat) },
                        label = { Text(cat) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            Text("Length", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                lengths.forEach { len ->
                    FilterChip(
                        selected = selectedLength == len,
                        onClick = { viewModel.setBioLength(len) },
                        label = { Text(len) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text("Include Emojis", modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
                Switch(checked = useEmoji, onCheckedChange = { viewModel.setBioEmoji(it) })
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = {
                    val cat = selectedCategory
                    val len = selectedLength
                    val emoji = useEmoji
                    coroutineScope.launch(kotlinx.coroutines.Dispatchers.Default) {
                        val newBios = List(5) { BioGenerator.generateBio(cat, len, emoji) }
                        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                            generatedBios = newBios
                            if (newBios.isNotEmpty()) {
                                viewModel.addHistory("BIO", newBios.first())
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(64.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null)
                Spacer(modifier = Modifier.width(12.dp))
                Text("GENERATE BIOS", fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(generatedBios) { bio ->
                    BioCard(bio = bio, viewModel = viewModel, context = context) {
                        val newBio = BioGenerator.generateBio(selectedCategory, selectedLength, useEmoji)
                        val newList = generatedBios.toMutableList()
                        val idx = newList.indexOf(bio)
                        if (idx != -1) newList[idx] = newBio
                        generatedBios = newList
                        viewModel.addHistory("BIO", newBio)
                    }
                }
                item { Spacer(modifier = Modifier.height(32.dp)) }
            }
        }
    }
}

@Composable
fun BioCard(bio: String, viewModel: MainViewModel, context: Context, onRegenerate: () -> Unit) {
    val isFav by viewModel.isFavorite("BIO", bio).collectAsStateWithLifecycle(initialValue = false)
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = bio,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                IconButton(onClick = onRegenerate) {
                    Icon(Icons.Default.Refresh, contentDescription = "Regenerate", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = {
                    copyToClipboard(context, bio, "Bio", viewModel)
                }) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = {
                    if (isFav) viewModel.removeFavorite("BIO", bio)
                    else viewModel.addFavorite("BIO", bio)
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
