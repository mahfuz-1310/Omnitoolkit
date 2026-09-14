package com.example.ui.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.navigation.compose.rememberNavController
import com.example.ui.theme.NameGenProTheme

/**
 * Android Fragment wrapper for the Interactive Fake GPS Map Picker.
 * Can be hosted in XML activities, viewpagers, or fragment containers.
 */
class FakeGpsMapFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                NameGenProTheme {
                    val navController = rememberNavController()
                    FakeGpsMapScreen(navController = navController)
                }
            }
        }
    }

    companion object {
        fun newInstance(): FakeGpsMapFragment {
            return FakeGpsMapFragment()
        }
    }
}
