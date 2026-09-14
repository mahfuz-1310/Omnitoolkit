package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class ScanItemState { PENDING, SCANNING, SAFE }

data class SecurityCategory(
    val title: String,
    var state: ScanItemState = ScanItemState.PENDING
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecurityScanScreen(onBack: () -> Unit) {
    var isScanning by remember { mutableStateOf(false) }
    var scanCompleted by remember { mutableStateOf(false) }
    var currentItemText by remember { mutableStateOf("Ready to scan") }
    
    val categories = remember {
        mutableStateListOf(
            SecurityCategory("System vulnerabilities"),
            SecurityCategory("Payment environment"),
            SecurityCategory("Trojan viruses"),
            SecurityCategory("Risky apps")
        )
    }

    val coroutineScope = rememberCoroutineScope()

    fun startScan() {
        if (isScanning) return
        isScanning = true
        scanCompleted = false
        categories.forEach { it.state = ScanItemState.PENDING }
        
        coroutineScope.launch {
            val fakeApps = listOf("com.whatsapp", "com.instagram.android", "com.google.android.gm", "com.spotify.music")
            val systemChecks = listOf("System vulnerabilities", "Root access", "Bootloader", "SE Linux")
            
            for (i in categories.indices) {
                if (!isScanning) break
                val category = categories[i]
                categories[i] = category.copy(state = ScanItemState.SCANNING)
                
                when (i) {
                    0 -> { // System vulnerabilities
                        for (check in systemChecks) {
                            if (!isScanning) break
                            currentItemText = "Scanning: $check"
                            delay(500)
                        }
                    }
                    1 -> { // Payment environment
                        currentItemText = "Scanning: Payment environment"
                        delay(1200)
                    }
                    2 -> { // Trojan viruses
                        currentItemText = "Scanning: Trojan viruses"
                        delay(1500)
                    }
                    3 -> { // Risky apps
                        for (app in fakeApps) {
                            if (!isScanning) break
                            currentItemText = "Scanning: $app"
                            delay(400)
                        }
                    }
                }
                
                if (isScanning) {
                    categories[i] = category.copy(state = ScanItemState.SAFE)
                }
            }
            
            if (isScanning) {
                isScanning = false
                scanCompleted = true
                currentItemText = "Scan Complete: 100% Secure"
            }
        }
    }

    fun stopScan() {
        isScanning = false
        currentItemText = "Scan stopped"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Security Scan", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Button(
                    onClick = { if (isScanning) stopScan() else startScan() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isScanning) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = if (isScanning) "Stop scanning" else if (scanCompleted) "Scan Again" else "Start Scan",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Shield Section (No circular progress bars, just a glowing shield)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(160.dp)
            ) {
                // Animated Base Glow
                val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                val pulseRadius by infiniteTransition.animateFloat(
                    initialValue = 70f,
                    targetValue = 90f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1200, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "pulse_radius"
                )
                
                val shieldColor = if (scanCompleted) Color(0xFF10B981) else MaterialTheme.colorScheme.primary

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2, size.height / 2)
                    
                    if (isScanning || scanCompleted) {
                        // Soft glowing aura
                        drawCircle(
                            color = shieldColor.copy(alpha = 0.2f),
                            radius = pulseRadius.dp.toPx(),
                            center = center
                        )
                    } else {
                        // Static glow
                        drawCircle(
                            color = shieldColor.copy(alpha = 0.1f),
                            radius = 70.dp.toPx(),
                            center = center
                        )
                    }
                    
                    // Central shield circle base
                    drawCircle(
                        color = shieldColor.copy(alpha = 0.15f),
                        radius = 60.dp.toPx(),
                        center = center
                    )
                }
                
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = shieldColor
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = currentItemText,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Category Checklist
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                itemsIndexed(categories) { _, category ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                            .padding(horizontal = 16.dp, vertical = 20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = category.title,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        Box(
                            modifier = Modifier.size(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            when (category.state) {
                                ScanItemState.PENDING -> {
                                    // Empty state, or a small dot
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
                                    )
                                }
                                ScanItemState.SCANNING -> {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                ScanItemState.SAFE -> {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Safe",
                                        tint = Color(0xFF10B981),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
