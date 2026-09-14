package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.viewmodel.DeepScannerViewModel
import com.example.viewmodel.ScanPhase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeepScannerScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val application = context.applicationContext as android.app.Application
    
    val viewModel: DeepScannerViewModel = viewModel(
        factory = viewModelFactory {
            initializer { DeepScannerViewModel(application) }
        }
    )

    val currentPhase by viewModel.currentPhase.collectAsState()
    val progress by viewModel.progress.collectAsState()
    val statusText by viewModel.statusText.collectAsState()
    val scannedApps by viewModel.scannedApps.collectAsState()
    val scannedFiles by viewModel.scannedFiles.collectAsState()
    val detectedIssues by viewModel.detectedIssues.collectAsState()

    // Permission launcher for Storage
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            viewModel.startScan()
        }
    }

    fun handleStartScan() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                viewModel.startScan()
            } else {
                requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        } else {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED) {
                viewModel.startScan()
            } else {
                requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
            }
        }
    }

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 300),
        label = "progress_anim"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("System Security Scan", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Button(
                    onClick = {
                        when (currentPhase) {
                            ScanPhase.IDLE, ScanPhase.COMPLETED, ScanPhase.STOPPED -> handleStartScan()
                            else -> viewModel.stopScan()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (currentPhase == ScanPhase.IDLE || currentPhase == ScanPhase.COMPLETED || currentPhase == ScanPhase.STOPPED) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.error
                        }
                    )
                ) {
                    Text(
                        text = when (currentPhase) {
                            ScanPhase.IDLE -> "Start Deep Scan"
                            ScanPhase.COMPLETED, ScanPhase.STOPPED -> "Scan Again"
                            else -> "Stop Scanning"
                        },
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Main animated progress indicator
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(200.dp)
            ) {
                CircularProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier.size(180.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    strokeWidth = 12.dp
                )
                
                CircularProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier.size(180.dp),
                    color = if (currentPhase == ScanPhase.COMPLETED) Color(0xFF10B981) else MaterialTheme.colorScheme.primary,
                    strokeWidth = 12.dp
                )
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${(animatedProgress * 100).toInt()}%",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            // Live Status Text
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))
            ) {
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(16.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Metrics Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard("Apps", scannedApps.toString(), Icons.Default.Apps, Modifier.weight(1f))
                MetricCard("Files", scannedFiles.toString(), Icons.Default.Folder, Modifier.weight(1f))
                MetricCard("Issues", detectedIssues.size.toString(), Icons.Default.Warning, Modifier.weight(1f), 
                           iconTint = if (detectedIssues.isNotEmpty()) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Checklist
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ScanCategoryItem(
                    title = "1. Installed Apps & Packages",
                    description = "Checking app signatures and permissions",
                    isActive = currentPhase == ScanPhase.SCANNING_APPS,
                    isCompleted = currentPhase.ordinal > ScanPhase.SCANNING_APPS.ordinal && currentPhase != ScanPhase.STOPPED
                )
                
                ScanCategoryItem(
                    title = "2. Deep File & Storage",
                    description = "Analyzing large files and media via MediaStore",
                    isActive = currentPhase == ScanPhase.SCANNING_STORAGE,
                    isCompleted = currentPhase.ordinal > ScanPhase.SCANNING_STORAGE.ordinal && currentPhase != ScanPhase.STOPPED
                )
                
                ScanCategoryItem(
                    title = "3. System Security",
                    description = "Verifying system integrity and bootloader",
                    isActive = currentPhase == ScanPhase.SCANNING_SECURITY,
                    isCompleted = currentPhase.ordinal > ScanPhase.SCANNING_SECURITY.ordinal && currentPhase != ScanPhase.STOPPED
                )
                
                ScanCategoryItem(
                    title = "4. Cache & Residuals",
                    description = "Finding temporary and junk files",
                    isActive = currentPhase == ScanPhase.SCANNING_CACHE,
                    isCompleted = currentPhase == ScanPhase.COMPLETED
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Detected Issues Results Section (Animates in when completed)
            AnimatedVisibility(
                visible = currentPhase == ScanPhase.COMPLETED,
                enter = expandVertically(animationSpec = tween(500)) + fadeIn(animationSpec = tween(500)),
                exit = shrinkVertically() + fadeOut()
            ) {
                if (detectedIssues.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                    ) {
                        Text(
                            text = "Detected Issues (${detectedIssues.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        
                        detectedIssues.forEach { issue ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.GppBad,
                                        contentDescription = "Warning",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column {
                                        Text(
                                            text = issue.title,
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onErrorContainer
                                        )
                                        Text(
                                            text = issue.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                                        )
                                    }
                                }
                            }
                        }
                        
                        Button(
                            onClick = { viewModel.fixAllIssues() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Fix All Issues", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                } else {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF10B981).copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.VerifiedUser,
                                contentDescription = "Safe",
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = "Device is 100% Secure",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "No threats or vulnerabilities found.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(100.dp)) // Extra padding for bottom bar
        }
    }
}

@Composable
fun MetricCard(title: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier = Modifier, iconTint: Color = MaterialTheme.colorScheme.primary) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(title, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun ScanCategoryItem(title: String, description: String, isActive: Boolean, isCompleted: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isActive) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(
                    when {
                        isCompleted -> Color(0xFF10B981).copy(alpha = 0.2f)
                        isActive -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f)
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            when {
                isCompleted -> Icon(Icons.Default.Check, contentDescription = "Done", tint = Color(0xFF10B981), modifier = Modifier.size(20.dp))
                isActive -> CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.primary)
                else -> Icon(Icons.Default.HourglassEmpty, contentDescription = "Pending", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
            }
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
