package com.example.feature.system.wifi.speedtest

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.utils.SoundEffectManager

@Composable
fun SpeedTestSection(
    speedTestState: SpeedTestState,
    onStartTest: () -> Unit,
    onCancelTest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isTesting = speedTestState.phase in listOf(
        SpeedTestPhase.PING,
        SpeedTestPhase.DOWNLOAD,
        SpeedTestPhase.UPLOAD
    )

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = "Speed Test",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Speed Test Utility",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = when (speedTestState.phase) {
                                SpeedTestPhase.IDLE -> "Measure real-time bandwidth"
                                SpeedTestPhase.PING -> "Measuring latency & jitter..."
                                SpeedTestPhase.DOWNLOAD -> "Testing download throughput..."
                                SpeedTestPhase.UPLOAD -> "Testing upload throughput..."
                                SpeedTestPhase.COMPLETED -> "Test finished"
                                SpeedTestPhase.ERROR -> "Test failed"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = when (speedTestState.phase) {
                                SpeedTestPhase.ERROR -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }

                if (isTesting) {
                    FilledTonalButton(
                        onClick = {
                            SoundEffectManager.playClick()
                            onCancelTest()
                        },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Stop", style = MaterialTheme.typography.labelMedium)
                    }
                } else {
                    Button(
                        onClick = {
                            SoundEffectManager.playClick()
                            onStartTest()
                        },
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = if (speedTestState.phase == SpeedTestPhase.COMPLETED) Icons.Default.Refresh else Icons.Default.Speed,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            if (speedTestState.phase == SpeedTestPhase.COMPLETED) "Retest" else "Run Test",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }

            // Live gauge bar during testing
            AnimatedVisibility(visible = isTesting) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    val animatedProgress by animateFloatAsState(
                        targetValue = speedTestState.progress,
                        label = "SpeedTestProgress"
                    )
                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (speedTestState.phase == SpeedTestPhase.DOWNLOAD) "Downloading..." else if (speedTestState.phase == SpeedTestPhase.UPLOAD) "Uploading..." else "Pinging...",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = if (speedTestState.currentSpeedMbps > 0) "${speedTestState.currentSpeedMbps} Mbps" else "",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Error message if any
            if (speedTestState.phase == SpeedTestPhase.ERROR && speedTestState.errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = speedTestState.errorMessage,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Metrics Grid: Download | Upload | Ping
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricCard(
                    title = "Download",
                    value = if (speedTestState.downloadMbps > 0) "${speedTestState.downloadMbps}" else "--",
                    unit = "Mbps",
                    icon = Icons.Default.ArrowDownward,
                    accentColor = Color(0xFF10B981),
                    isActive = speedTestState.phase == SpeedTestPhase.DOWNLOAD,
                    modifier = Modifier.weight(1f)
                )

                MetricCard(
                    title = "Upload",
                    value = if (speedTestState.uploadMbps > 0) "${speedTestState.uploadMbps}" else "--",
                    unit = "Mbps",
                    icon = Icons.Default.ArrowUpward,
                    accentColor = Color(0xFF0EA5E9),
                    isActive = speedTestState.phase == SpeedTestPhase.UPLOAD,
                    modifier = Modifier.weight(1f)
                )

                MetricCard(
                    title = "Ping / Jitter",
                    value = if (speedTestState.pingMs > 0) "${speedTestState.pingMs}" else "--",
                    unit = if (speedTestState.jitterMs > 0) "ms (±${speedTestState.jitterMs})" else "ms",
                    icon = Icons.Default.Timer,
                    accentColor = Color(0xFFF59E0B),
                    isActive = speedTestState.phase == SpeedTestPhase.PING,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    unit: String,
    icon: ImageVector,
    accentColor: Color,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    val borderColor by animateColorAsState(
        targetValue = if (isActive) accentColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
        label = "MetricBorder"
    )

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(if (isActive) 1.5.dp else 1.dp, borderColor),
        tonalElevation = if (isActive) 3.dp else 0.dp
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = if (value != "--") MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )

            Text(
                text = unit,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
