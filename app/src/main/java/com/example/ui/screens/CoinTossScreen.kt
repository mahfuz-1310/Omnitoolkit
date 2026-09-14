package com.example.ui.screens

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.ui.MainViewModel
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoinTossScreen(navController: NavController, viewModel: MainViewModel) {
    val coroutineScope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    // Rotation and Height animation states
    val animRotation = remember { Animatable(0f) }
    val animHeight = remember { Animatable(0f) } // 0f to 1f

    var isAnimating by remember { mutableStateOf(false) }
    var resultText by remember { mutableStateOf("") }
    var landedHeadsResult by remember { mutableStateOf<Boolean?>(null) }

    // Stats and History
    var headsCount by remember { mutableStateOf(0) }
    var tailsCount by remember { mutableStateOf(0) }
    val tossHistory = remember { mutableStateListOf<Boolean>() }

    // Click handler with dynamic synthesized sound and real 3D calculations
    val onTossCoin = {
        if (!isAnimating) {
            coroutineScope.launch {
                isAnimating = true
                landedHeadsResult = null // Reset so old banner hides during animation
                resultText = "Flipping..."
                
                // Trigger subtle initial haptic
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)

                // Synthesize & play coin flip sound in background
                playCoinTossSound()

                // Random boolean outcome
                val landedHeads = kotlin.random.Random.nextBoolean()

                // Calculate next target rotation (multiples of 360 plus 180 offset if landing on Tails)
                val baseRotations = 1440f // 4 full spins
                val targetRot = if (landedHeads) baseRotations else baseRotations + 180f

                // Run height (up/down) and rotation in parallel
                launch {
                    // Parabolic height curve
                    animHeight.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing)
                    )
                    animHeight.animateTo(
                        targetValue = 0f,
                        animationSpec = tween(durationMillis = 600, easing = LinearOutSlowInEasing)
                    )
                }

                animRotation.animateTo(
                    targetValue = targetRot,
                    animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing)
                )

                // Play realistic physical coin landing multi-bounce rattle sound on the table
                com.example.utils.SoundEffectManager.playCoinLand()

                // Finish Toss
                landedHeadsResult = landedHeads
                if (landedHeads) {
                    headsCount++
                    tossHistory.add(0, true)
                    resultText = "🎉 It's HEADS!"
                } else {
                    tailsCount++
                    tossHistory.add(0, false)
                    resultText = "🎉 It's TAILS!"
                }

                // Snap rotation back to base modulo (0 or 180) to avoid infinite build-up
                animRotation.snapTo(if (landedHeads) 0f else 180f)

                // Trigger landing haptic
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                isAnimating = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Coin Toss", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = {
                        com.example.utils.SoundEffectManager.playClick()
                        navController.popBackStack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        com.example.utils.SoundEffectManager.playClick()
                        headsCount = 0
                        tailsCount = 0
                        tossHistory.clear()
                        landedHeadsResult = null
                        resultText = ""
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reset Stats")
                    }
                }
            )
        }
    ) { padding ->
        // Beautiful ambient gradient background matching system theme colors
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                
                // 1. Stats Counter Banner at the top
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Casino,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Stats: Heads: $headsCount | Tails: $tailsCount",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        TextButton(
                            onClick = {
                                com.example.utils.SoundEffectManager.playClick()
                                headsCount = 0
                                tailsCount = 0
                                tossHistory.clear()
                                landedHeadsResult = null
                                resultText = ""
                            },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("Reset Stats", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // 2. Main 3D-styled Coin Interactive Area
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    // Physical realistic ground shadow
                    val shadowScale = 1f + (animHeight.value * 0.4f)
                    val shadowAlpha = 0.22f * (1f - animHeight.value * 0.6f)
                    val shadowBlur = (12 + (animHeight.value * 24)).dp

                    Box(
                        modifier = Modifier
                            .offset(y = 110.dp)
                            .size(120.dp)
                            .graphicsLayer {
                                scaleX = shadowScale
                                scaleY = shadowScale * 0.3f
                                alpha = shadowAlpha
                            }
                            .blur(shadowBlur)
                            .background(Color.Black, shape = CircleShape)
                    )

                    // 3D Moving Coin component
                    val coinYOffset = -(animHeight.value * 150)
                    Coin(
                        rotationX = animRotation.value,
                        modifier = Modifier
                            .size(180.dp)
                            .offset(y = coinYOffset.dp)
                    )
                }

                // 3. Dynamic Colorful Pop-in Result Banner
                Column(
                    modifier = Modifier
                        .height(72.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    AnimatedVisibility(
                        visible = landedHeadsResult != null && !isAnimating,
                        enter = fadeIn(animationSpec = tween(300)) + scaleIn(
                            initialScale = 0.8f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                        )
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (landedHeadsResult == true) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    MaterialTheme.colorScheme.secondaryContainer
                                }
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.padding(horizontal = 16.dp)
                        ) {
                            Text(
                                text = resultText,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (landedHeadsResult == true) {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onSecondaryContainer
                                },
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                // 4. Primary Toss Action Button
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        com.example.utils.SoundEffectManager.playClick()
                        onTossCoin()
                    },
                    enabled = !isAnimating,
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp)
                ) {
                    Text(
                        text = if (isAnimating) "Flipping..." else "TOSS COIN",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                // 5. Horizontal History Logs
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    if (tossHistory.isNotEmpty()) {
                        Text(
                            "Recent Flips",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(bottom = 4.dp)
                        ) {
                            items(tossHistory.take(8)) { isHeads ->
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isHeads) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                            else MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = if (isHeads) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (isHeads) "H" else "T",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isHeads) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.height(48.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun Coin(rotationX: Float, modifier: Modifier = Modifier) {
    val normalizedRotation = (rotationX % 360f + 360f) % 360f
    val isHeads = normalizedRotation < 90f || normalizedRotation > 270f
    
    val contentRotationX = if (isHeads) 0f else 180f

    Card(
        modifier = modifier
            .graphicsLayer {
                this.rotationX = rotationX
                cameraDistance = 12f * density
            },
        shape = CircleShape,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFFFD700), // Premium Gold Center
                            Color(0xFFDAA520), // Goldenrod Middle
                            Color(0xFF8B6508)  // Solid Coin Edge Color
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            // High-detail outer shiny sweep ring
            Box(
                modifier = Modifier
                    .fillMaxSize(0.92f)
                    .background(
                        brush = Brush.sweepGradient(
                            colors = listOf(
                                Color(0xFFFFF8DC),
                                Color(0xFFFFD700),
                                Color(0xFFB8860B),
                                Color(0xFFFFF8DC)
                            )
                        ),
                        shape = CircleShape
                    )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize(0.90f)
                        .align(Alignment.Center)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFFFFEC8B),
                                    Color(0xFFDAA520)
                                )
                            ),
                            shape = CircleShape
                        )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                this.rotationX = contentRotationX
                            }
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text(
                            text = if (isHeads) "HEADS" else "TAILS",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF5D4037),
                            letterSpacing = 2.sp
                        )
                        
                        // HEADS: Imperial/Crown icon (EmojiEvents/Award)
                        // TAILS: Eagle/Number emblem (WorkspacePremium/Badge)
                        Icon(
                            imageVector = if (isHeads) Icons.Filled.EmojiEvents else Icons.Filled.WorkspacePremium,
                            contentDescription = null,
                            tint = Color(0xFF5D4037),
                            modifier = Modifier.size(60.dp)
                        )
                        
                        Text(
                            text = if (isHeads) "IMPERIAL" else "ONE GENERATOR",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF5D4037).copy(alpha = 0.8f),
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
        }
    }
}

/**
 * Synthesizes a premium coin flip sound effect dynamically using Android AudioTrack.
 */
fun playCoinTossSound() {
    com.example.utils.SoundEffectManager.playCoinToss()
}
