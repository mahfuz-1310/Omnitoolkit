package com.example.ui.components

import android.content.Context
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.MainViewModel
import com.example.utils.IconManager
import com.example.utils.SoundEffectManager

/**
 * Returns a dedicated theme accent color for each icon variant for premium glow and borders.
 */
fun getVariantAccentColor(alias: String): Color {
    return when (alias) {
        ".DefaultBlue" -> Color(0xFF3B82F6) // Radiant Blue
        ".DarkStealth" -> Color(0xFF94A3B8) // Titanium Slate
        ".VibrantGradient" -> Color(0xFF06B6D4) // Neon Cyan
        ".CrimsonRed" -> Color(0xFFEF4444) // Crimson Red
        ".GoldPremium" -> Color(0xFFF59E0B) // Amber Gold
        ".CyberPurple" -> Color(0xFFA855F7) // Cyber Purple
        else -> Color(0xFF3B82F6)
    }
}

/**
 * High-End Custom App Icon Picker Widget displaying actual launcher icon drawables
 * with high-resolution previews, squircle styling, glowing accents, and crisp contrast.
 */
@Composable
fun CustomIconPickerWidget(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier,
    context: Context = LocalContext.current
) {
    val currentAppIcon by viewModel.appIcon.collectAsStateWithLifecycle()
    var selectedAlias by remember(currentAppIcon) {
        mutableStateOf(
            IconManager.variants.find { it.name == currentAppIcon }?.alias ?: ".DefaultBlue"
        )
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Column {
                    Text(
                        text = "Custom App Icon",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Select a launcher logo theme for your home screen",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 3x2 Grid of App Icon Logo Previews
            val rows = IconManager.variants.chunked(2)
            rows.forEachIndexed { rowIndex, rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    rowItems.forEach { variant ->
                        val isSelected = selectedAlias == variant.alias
                        val accentColor = getVariantAccentColor(variant.alias)
                        
                        val animatedBorderWidth by animateDpAsState(
                            targetValue = if (isSelected) 2.dp else 1.dp,
                            label = "borderWidth"
                        )
                        val animatedCardBg by animateColorAsState(
                            targetValue = if (isSelected) {
                                accentColor.copy(alpha = 0.12f)
                            } else {
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
                            },
                            label = "cardBg"
                        )

                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .shadow(
                                    elevation = if (isSelected) 6.dp else 2.dp,
                                    shape = RoundedCornerShape(20.dp),
                                    ambientColor = if (isSelected) accentColor else Color.Black,
                                    spotColor = if (isSelected) accentColor else Color.Black
                                )
                                .clickable {
                                    SoundEffectManager.playClick(context)
                                    selectedAlias = variant.alias
                                },
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = animatedCardBg),
                            border = BorderStroke(
                                width = animatedBorderWidth,
                                color = if (isSelected) accentColor else MaterialTheme.colorScheme.outline
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(vertical = 16.dp, horizontal = 10.dp)
                                    .fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                // 68dp Squircle Logo Preview with Outer Glow & Top-Right Checkmark
                                Box(
                                    modifier = Modifier.size(76.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    // Glow and Icon Box (68dp)
                                    Box(
                                        modifier = Modifier
                                            .size(68.dp)
                                            .shadow(
                                                elevation = if (isSelected) 8.dp else 3.dp,
                                                shape = RoundedCornerShape(16.dp),
                                                ambientColor = accentColor,
                                                spotColor = accentColor
                                            )
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(
                                                Brush.verticalGradient(
                                                    colors = listOf(
                                                        MaterialTheme.colorScheme.surface,
                                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                                    )
                                                )
                                            )
                                            .background(accentColor.copy(alpha = 0.08f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        SafeIconImage(
                                            resId = variant.drawableResId,
                                            contentDescription = variant.name,
                                            isDefaultBlue = (variant.alias == ".DefaultBlue"),
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }

                                    // Outer squircle border stroke around the icon
                                    Surface(
                                        shape = RoundedCornerShape(16.dp),
                                        color = Color.Transparent,
                                        border = BorderStroke(
                                            width = if (isSelected) 2.dp else 1.dp,
                                            color = if (isSelected) accentColor else accentColor.copy(alpha = 0.3f)
                                        ),
                                        modifier = Modifier.size(68.dp)
                                    ) {}

                                    // Top-Right Checkmark Badge (Clean position without obstructing center logo)
                                    if (isSelected) {
                                        Surface(
                                            shape = CircleShape,
                                            color = accentColor,
                                            border = BorderStroke(2.dp, MaterialTheme.colorScheme.surface),
                                            shadowElevation = 4.dp,
                                            modifier = Modifier
                                                .size(24.dp)
                                                .align(Alignment.TopEnd)
                                                .offset(x = 2.dp, y = (-2).dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = "Selected",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // High-contrast Title
                                Text(
                                    text = variant.name,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                    color = if (isSelected) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    },
                                    maxLines = 1
                                )

                                Spacer(modifier = Modifier.height(3.dp))

                                // High-contrast Subtitle
                                Text(
                                    text = variant.description,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                    if (rowItems.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
                if (rowIndex < rows.size - 1) {
                    Spacer(modifier = Modifier.height(14.dp))
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Prominent High-End "Set Icon" Button
            Button(
                onClick = {
                    SoundEffectManager.playClick(context)
                    val variant = IconManager.variants.find { it.alias == selectedAlias }
                    if (variant != null) {
                        viewModel.setAppIcon(variant.name)
                        IconManager.applyIcon(context, variant.alias)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 3.dp,
                    pressedElevation = 6.dp
                ),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Apply Selected Icon",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }
}

@Composable
fun SafeIconImage(
    resId: Int,
    contentDescription: String?,
    isDefaultBlue: Boolean,
    modifier: Modifier = Modifier
) {
    if (resId != 0) {
        Image(
            painter = painterResource(id = resId),
            contentDescription = contentDescription,
            contentScale = ContentScale.Fit,
            modifier = modifier.padding(if (isDefaultBlue) 2.dp else 6.dp)
        )
    } else {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Apps,
                contentDescription = contentDescription,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

