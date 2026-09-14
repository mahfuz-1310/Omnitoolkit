package com.example.ui.screens

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.ui.MainViewModel
import com.example.ui.components.CustomIconPickerWidget

/**
 * App Icon Selector Section for Settings and Customization Screens.
 * Delegates to CustomIconPickerWidget to display actual launcher logo previews.
 */
@Composable
fun AppIconSelectorSection(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier,
    context: Context = LocalContext.current
) {
    CustomIconPickerWidget(
        viewModel = viewModel,
        modifier = modifier,
        context = context
    )
}
