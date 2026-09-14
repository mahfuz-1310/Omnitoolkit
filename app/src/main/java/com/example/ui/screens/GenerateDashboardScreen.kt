package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import android.os.SystemClock
import android.util.Log
import com.example.ui.MainViewModel
import com.example.ui.navigation.Screen

@Immutable
data class ToolItem(val name: String, val icon: ImageVector, val route: String, val color: androidx.compose.ui.graphics.Color)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenerateDashboardScreen(navController: NavController, viewModel: MainViewModel) {
    val t0 = remember { SystemClock.uptimeMillis() }
    SideEffect {
        Log.d("Tabs", "GenerateDashboardScreen switched in ${SystemClock.uptimeMillis() - t0}ms")
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(Unit) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.onGenerateVisible()
        }
    }
    val tools = listOf(
        ToolItem("Name Generator", Icons.Filled.Person, Screen.NameGen.route, MaterialTheme.colorScheme.primary),
        ToolItem("Middle Name Generator", Icons.Filled.Badge, Screen.MiddleNameGen.route, MaterialTheme.colorScheme.secondary),
        ToolItem("First + Middle Name Generator", Icons.Filled.People, Screen.FirstMiddleGen.route, MaterialTheme.colorScheme.tertiary),
        ToolItem("Username Generator", Icons.Filled.AlternateEmail, Screen.UsernameGen.route, MaterialTheme.colorScheme.error),
        ToolItem("Nickname Generator", Icons.Filled.Face, Screen.NicknameGen.route, MaterialTheme.colorScheme.primary),
        ToolItem("Stylish Text Generator", Icons.Filled.FontDownload, Screen.StylishTextGen.route, MaterialTheme.colorScheme.secondary),
        
        // Other tools
        ToolItem("Smart Assistant", Icons.Filled.AutoAwesome, Screen.SmartAssistant.route, MaterialTheme.colorScheme.primary),
        ToolItem("Passwords", Icons.Filled.Lock, Screen.PasswordGen.route, MaterialTheme.colorScheme.error),
        ToolItem("Bio Maker", Icons.Filled.TextSnippet, Screen.BioGen.route, MaterialTheme.colorScheme.secondary),
        ToolItem("Random Profile", Icons.Filled.AssignmentInd, Screen.RandomProfile.route, MaterialTheme.colorScheme.error),
        ToolItem("Name Mixer", Icons.Filled.Shuffle, Screen.NameMixer.route, MaterialTheme.colorScheme.primary),
        ToolItem("Username Builder", Icons.Filled.Build, Screen.UsernameBuilder.route, MaterialTheme.colorScheme.secondary),
        ToolItem("Text Saver", Icons.Filled.Save, Screen.TextSaver.route, MaterialTheme.colorScheme.primary),
        ToolItem("Coin Toss", Icons.Filled.Casino, Screen.CoinToss.route, MaterialTheme.colorScheme.tertiary)
    )

    val context = LocalContext.current
    val history by viewModel.allHistory.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Generate", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->
        val gridState = rememberSaveable(saver = LazyGridState.Saver) { LazyGridState() }
        LazyVerticalGrid(
            state = gridState,
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            com.example.utils.SoundEffectManager.playClick()
                            navController.navigate(Screen.History.route)
                        },
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(
                                    imageVector = Icons.Filled.History,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    text = "Generation History",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            
                            Text(
                                text = "View All",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        if (history.isEmpty()) {
                            Text(
                                text = "Your generation history is empty. Try creating some names or passwords above!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                history.take(3).forEach { item ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f))
                                            .clickable {
                                                copyToClipboard(context, item.content, item.type, viewModel)
                                            }
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = item.content,
                                                style = MaterialTheme.typography.bodyLarge,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = item.type,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Icon(
                                            imageVector = Icons.Filled.ContentCopy,
                                            contentDescription = "Copy",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            items(tools) { tool ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clickable {
                            com.example.utils.SoundEffectManager.playClick()
                            navController.navigate(tool.route)
                        },
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.Start
                    ) {
                        Icon(
                            imageVector = tool.icon,
                            contentDescription = tool.name,
                            tint = tool.color,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = tool.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}
