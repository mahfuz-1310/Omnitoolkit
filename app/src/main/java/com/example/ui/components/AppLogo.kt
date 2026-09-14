package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Single source of truth for the official Hyper Toolkit Logo presentation.
 * Features the official emblem, white wordmark, and clean deep black styling.
 */
@Composable
fun AppLogo(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    cornerRadius: Dp = 12.dp,
    contentDescription: String = "Hyper Toolkit Logo"
) {
    val context = LocalContext.current
    val resourceId = remember {
        val id = context.resources.getIdentifier("hyper_toolkit_logo_1789410452090", "drawable", context.packageName)
        if (id != 0) id else context.resources.getIdentifier("omnikit_logo_1789391961077", "drawable", context.packageName)
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(cornerRadius))
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        if (resourceId != 0) {
            Image(
                painter = painterResource(id = resourceId),
                contentDescription = contentDescription,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
    }
}
