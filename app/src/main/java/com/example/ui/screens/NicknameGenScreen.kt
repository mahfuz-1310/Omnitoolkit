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
import androidx.compose.material.icons.filled.FontDownload
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
import com.example.utils.NicknameGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NicknameGenScreen(navController: NavController, viewModel: MainViewModel) {
    val categories = listOf("Random", "Cute", "Gaming", "Funny", "Stylish", "Short", "Royal", "Couple")
    val fontStyle by viewModel.fontStyle.collectAsStateWithLifecycle()
    var showFontSelector by remember { mutableStateOf(false) }

    val selectedCategory by viewModel.nicknameCategory.collectAsStateWithLifecycle()
    
    val generateCount by viewModel.nicknameCount.collectAsStateWithLifecycle()
    var generatedNicknames by remember { mutableStateOf<List<String>>(emptyList()) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        if (generatedNicknames.isEmpty()) {
            val count = generateCount.toInt()
            val cat = selectedCategory
            coroutineScope.launch(kotlinx.coroutines.Dispatchers.Default) {
                val results = List(count) { NicknameGenerator.generateNickname(cat) }
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    generatedNicknames = results
                    if (results.isNotEmpty()) viewModel.addHistory("NICKNAME", com.example.utils.FontStyles.apply(results.first(), fontStyle))
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nickname Generator", style = MaterialTheme.typography.titleMedium) },
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
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            Text("Select Category", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(16.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { viewModel.setNicknameCategory(cat) },
                        label = { Text(cat) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Generate Count", style = MaterialTheme.typography.titleMedium)
                Text("${generateCount.toInt()}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Slider(
                value = generateCount,
                onValueChange = { viewModel.setNicknameCount(it) },
                valueRange = 5f..50f,
                steps = 8
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = {
                    val count = generateCount.toInt()
                    val cat = selectedCategory
                    coroutineScope.launch(kotlinx.coroutines.Dispatchers.Default) {
                        val newNicks = List(count) { NicknameGenerator.generateNickname(cat) }
                        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                            generatedNicknames = newNicks
                            if (newNicks.isNotEmpty()) {
                                viewModel.addHistory("NICKNAME", com.example.utils.FontStyles.apply(newNicks.first(), fontStyle))
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(64.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null)
                Spacer(modifier = Modifier.width(12.dp))
                Text("GENERATE NICKNAMES", fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(generatedNicknames) { nick ->
                    NicknameCard(nick = nick, fontStyle = fontStyle, viewModel = viewModel, context = context) {
                        val newNick = NicknameGenerator.generateNickname(selectedCategory)
                        val newList = generatedNicknames.toMutableList()
                        val idx = newList.indexOf(nick)
                        if (idx != -1) newList[idx] = newNick
                        generatedNicknames = newList
                        viewModel.addHistory("NICKNAME", com.example.utils.FontStyles.apply(newNick, fontStyle))
                    }
                }
                item { Spacer(modifier = Modifier.height(32.dp)) }
            }
        }
        FontStyleBottomSheet(
            visible = showFontSelector,
            onDismissRequest = { showFontSelector = false },
            currentStyle = fontStyle,
            onStyleSelected = { viewModel.setFontStyle(it) }
        )
    }

}

@Composable
fun NicknameCard(nick: String, fontStyle: String, viewModel: MainViewModel, context: Context, onRegenerate: () -> Unit) {
    val isFav by viewModel.isFavorite("NICKNAME", com.example.utils.FontStyles.apply(nick, fontStyle)).collectAsStateWithLifecycle(initialValue = false)
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.3f))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                    text = com.example.utils.FontStyles.apply(nick, fontStyle),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onRegenerate, modifier = Modifier.size(40.dp)) {
                Icon(Icons.Default.Refresh, contentDescription = "Regenerate", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = {
                copyToClipboard(context, com.example.utils.FontStyles.apply(nick, fontStyle), "Nickname", viewModel)
            }, modifier = Modifier.size(40.dp)) {
                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = {
                if (isFav) viewModel.removeFavorite("NICKNAME", com.example.utils.FontStyles.apply(nick, fontStyle))
                else viewModel.addFavorite("NICKNAME", com.example.utils.FontStyles.apply(nick, fontStyle))
            }, modifier = Modifier.size(40.dp)) {
                Icon(
                    if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (isFav) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
