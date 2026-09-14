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
import com.example.utils.ProfileGenerator
import com.example.utils.RandomProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RandomProfileScreen(navController: NavController, viewModel: MainViewModel) {
    var profile by remember { mutableStateOf<RandomProfile?>(null) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    LaunchedEffect(Unit) {
        if (profile == null) {
            coroutineScope.launch(kotlinx.coroutines.Dispatchers.Default) {
                val newProfile = ProfileGenerator.generateProfile()
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    profile = newProfile
                }
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Random Profile", style = MaterialTheme.typography.titleMedium) },
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
            
            Button(
                onClick = {
                    coroutineScope.launch(kotlinx.coroutines.Dispatchers.Default) {
                        val newProfile = ProfileGenerator.generateProfile()
                        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                            profile = newProfile
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(64.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null)
                Spacer(modifier = Modifier.width(12.dp))
                Text("GENERATE NEW PROFILE", fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            profile?.let { p ->
                val profileText = """
                    Name: ${p.name}
                    Username: @${p.username}
                    Nickname: ${p.nickname}
                    Bio: ${p.bio}
                    Location: ${p.country}
                    Age: ${p.ageRange}
                    Personality: ${p.personality}
                    Interests: ${p.interests}
                """.trimIndent()
                
                val isFav by viewModel.isFavorite("PROFILE", profileText).collectAsStateWithLifecycle(initialValue = false)
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.3f))
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Profile Data", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                            Row {
                                IconButton(onClick = {
                                    copyToClipboard(context, profileText, "Profile", viewModel)
                                    viewModel.addHistory("PROFILE", profileText)
                                }) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                                }
                                IconButton(onClick = {
                                    if (isFav) viewModel.removeFavorite("PROFILE", profileText)
                                    else viewModel.addFavorite("PROFILE", profileText)
                                }) {
                                    Icon(
                                        if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                        contentDescription = "Favorite",
                                        tint = if (isFav) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                        
                        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = MaterialTheme.colorScheme.surfaceVariant)
                        
                        ProfileItem("Name", p.name, onCopy = { copyToClipboard(context, p.name, "Name", viewModel) })
                        ProfileItem("Username", "@${p.username}", onCopy = { copyToClipboard(context, "@${p.username}", "Username", viewModel) })
                        ProfileItem("Nickname", p.nickname, onCopy = { copyToClipboard(context, p.nickname, "Nickname", viewModel) })
                        ProfileItem("Bio", p.bio, onCopy = { copyToClipboard(context, p.bio, "Bio", viewModel) })
                        ProfileItem("Location", p.country, onCopy = { copyToClipboard(context, p.country, "Location", viewModel) })
                        ProfileItem("Age", p.ageRange, onCopy = { copyToClipboard(context, p.ageRange, "Age", viewModel) })
                        ProfileItem("Personality", p.personality, onCopy = { copyToClipboard(context, p.personality, "Personality", viewModel) })
                        ProfileItem("Interests", p.interests, onCopy = { copyToClipboard(context, p.interests, "Interests", viewModel) })
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Note: This is a randomly generated fictional profile.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun ProfileItem(label: String, value: String, onCopy: (() -> Unit)? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = value, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium)
        }
        if (onCopy != null) {
            IconButton(onClick = onCopy, modifier = Modifier.size(36.dp)) {
                Icon(
                    Icons.Default.ContentCopy,
                    contentDescription = "Copy $label",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
