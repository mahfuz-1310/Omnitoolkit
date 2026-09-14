package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import com.example.utils.NameResult
import com.example.utils.SmartAssistant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartAssistantScreen(navController: NavController, viewModel: MainViewModel) {
    val query by viewModel.smartQuery.collectAsStateWithLifecycle()
    var results by remember { mutableStateOf<List<NameResult>>(emptyList()) }
    val resultCount by viewModel.nicknameCount.collectAsStateWithLifecycle()
    
    val suggestions = listOf("Cute girl", "Modern boy", "Gaming", "Royal female", "Unique unisex", "Aesthetic short")
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Smart Name Assistant", style = MaterialTheme.typography.titleMedium) },
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
            
            Text("Describe the name you want", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Example: short cute modern girl name...", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = query,
                onValueChange = { viewModel.setSmartQuery(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Type here...") },
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(suggestions) { sugg ->
                    FilterChip(
                        selected = query == sugg,
                        onClick = { viewModel.setSmartQuery(sugg) },
                        label = { Text(sugg) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Number of results", style = MaterialTheme.typography.titleMedium)
                Text("${resultCount.toInt()}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
            Slider(
                value = resultCount,
                onValueChange = { viewModel.setNicknameCount(it) },
                valueRange = 5f..50f,
                steps = 8
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = {
                    if (query.isNotBlank()) {
                        val currentQuery = query
                        val currentCount = resultCount.toInt()
                        coroutineScope.launch(kotlinx.coroutines.Dispatchers.Default) {
                            val res = SmartAssistant.findNames(currentQuery, currentCount)
                            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                results = res
                                if (res.isNotEmpty()) {
                                    viewModel.addHistory("NAME", res.first().name)
                                }
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                shape = RoundedCornerShape(20.dp),
                enabled = query.isNotBlank()
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null)
                Spacer(modifier = Modifier.width(12.dp))
                Text("FIND NAMES", fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(results) { res ->
                    AssistantResultCard(res, viewModel, context)
                }
                item { Spacer(modifier = Modifier.height(32.dp)) }
            }
        }
    }
}

@Composable
fun AssistantResultCard(res: NameResult, viewModel: MainViewModel, context: Context) {
    val isFav by viewModel.isFavorite("NAME", res.name).collectAsStateWithLifecycle(initialValue = false)
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.4f)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = res.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                
                IconButton(onClick = {
                    copyToClipboard(context, res.name, "Name", viewModel)
                }) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                }
                
                IconButton(onClick = {
                    if(isFav) viewModel.removeFavorite("NAME", res.name)
                    else viewModel.addFavorite("NAME", res.name)
                }) {
                    Icon(
                        if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if(isFav) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = res.meaning, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(onClick = {}, label = { Text(res.style) })
                AssistChip(onClick = {}, label = { Text(res.length) })
            }
        }
    }
}
