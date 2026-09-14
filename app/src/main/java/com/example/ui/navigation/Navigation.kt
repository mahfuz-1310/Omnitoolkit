package com.example.ui.navigation

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.MainViewModel
import com.example.ui.components.CustomBottomNavigationBar
import com.example.ui.components.bottomNavItems
import com.example.ui.screens.*

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Home : Screen("home")
    object GenerateDashboard : Screen("generate_dashboard")
    
    // Tools
    object SmartAssistant : Screen("smart_assistant")
    object NameGen : Screen("name_gen")
    object MiddleNameGen : Screen("middle_name_gen")
    object FirstMiddleGen : Screen("first_middle_gen")
    object UsernameGen : Screen("username_gen")
    object PasswordGen : Screen("password_gen")
    object BioGen : Screen("bio_gen")
    object NicknameGen : Screen("nickname_gen")
    object StylishTextGen : Screen("stylish_text_gen")
    object RandomProfile : Screen("random_profile")
    object NameMixer : Screen("name_mixer")
    object UsernameBuilder : Screen("username_builder")
    object TextSaver : Screen("text_saver")
    object CoinToss : Screen("coin_toss")
    object DnsChanger : Screen("dns_changer")
    object AnimationSettings : Screen("animation_settings")
    object AppList : Screen("app_list")
    object FakeGps : Screen("fake_gps")
    object Modules : Screen("modules")
    object SecurityScan : Screen("security_scan")
    object DeepScanner : Screen("deep_scanner")
    object ImageMetadataEditor : Screen("image_metadata_editor")
    
    object SystemDashboard : Screen("system_dashboard")
    object History : Screen("history")
    object Favorites : Screen("favorites")
    object Settings : Screen("settings")
    object About : Screen("about")
    object Calculator : Screen("calculator")
}

@Composable
fun MainAppScreen(viewModel: MainViewModel) {
    val navController = rememberNavController()

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            bottomBar = {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                val showBottomBar = bottomNavItems.any { it.route == currentDestination?.route }

                if (showBottomBar) {
                    val darkModeState by viewModel.darkMode.collectAsStateWithLifecycle()
                    val whiteThemeState by viewModel.whiteTheme.collectAsStateWithLifecycle()
                    val isDarkTheme = (darkModeState ?: isSystemInDarkTheme()) && !whiteThemeState
                    CustomBottomNavigationBar(
                        navController = navController,
                        currentDestination = currentDestination,
                        isDarkTheme = isDarkTheme
                    )
                }
            }
        ) { paddingValues ->
            NameGenNavHost(
                navController = navController,
                viewModel = viewModel,
                modifier = Modifier.padding(paddingValues)
            )
        }

        // Animated custom floating toast notification
        val floatingToastMessage by viewModel.floatingToastMessage.collectAsStateWithLifecycle()

        AnimatedVisibility(
            visible = floatingToastMessage != null,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 12.dp)
                .padding(horizontal = 24.dp)
                .zIndex(100f)
        ) {
            floatingToastMessage?.let { msg ->
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.inverseSurface,
                    shadowElevation = 8.dp,
                    modifier = Modifier.widthIn(max = 320.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Success",
                            tint = MaterialTheme.colorScheme.inversePrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = msg,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.inverseOnSurface,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NameGenNavHost(
    navController: NavHostController,
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        composable(Screen.Home.route) {
            HomeScreen(navController = navController, viewModel = viewModel)
        }
        composable(Screen.GenerateDashboard.route) {
            GenerateDashboardScreen(navController = navController, viewModel = viewModel)
        }
        composable(Screen.SmartAssistant.route) {
            SmartAssistantScreen(navController = navController, viewModel = viewModel)
        }
        composable(Screen.NameGen.route) {
            NameGenScreen(navController = navController, viewModel = viewModel)
        }
        composable(Screen.MiddleNameGen.route) {
            MiddleNameGenScreen(navController = navController, viewModel = viewModel)
        }
        composable(Screen.FirstMiddleGen.route) {
            FirstMiddleGenScreen(navController = navController, viewModel = viewModel)
        }
        composable(Screen.UsernameGen.route) {
            UsernameGenScreen(navController = navController, viewModel = viewModel)
        }
        composable(Screen.PasswordGen.route) {
            PasswordGenScreen(navController = navController, viewModel = viewModel)
        }
        composable(Screen.BioGen.route) {
            BioGenScreen(navController = navController, viewModel = viewModel)
        }
        composable(Screen.NicknameGen.route) {
            NicknameGenScreen(navController = navController, viewModel = viewModel)
        }
        composable(Screen.StylishTextGen.route) {
            StylishTextGenScreen(navController = navController, viewModel = viewModel)
        }
        composable(Screen.RandomProfile.route) {
            RandomProfileScreen(navController = navController, viewModel = viewModel)
        }
        composable(Screen.NameMixer.route) {
            NameMixerScreen(navController = navController, viewModel = viewModel)
        }
        composable(Screen.UsernameBuilder.route) {
            UsernameBuilderScreen(navController = navController, viewModel = viewModel)
        }
        composable(Screen.TextSaver.route) {
            TextSaverScreen(navController = navController, viewModel = viewModel)
        }
        composable(Screen.CoinToss.route) {
            CoinTossScreen(navController = navController, viewModel = viewModel)
        }
        composable(Screen.SystemDashboard.route) {
            val app = androidx.compose.ui.platform.LocalContext.current.applicationContext as com.example.NameGenApplication
            SystemDashboardScreen(navController = navController, appManagerViewModel = app.appManagerViewModel)
        }
        composable(Screen.History.route) {
            HistoryScreen(navController = navController, viewModel = viewModel)
        }
        composable(Screen.Favorites.route) {
            FavoritesScreen(navController = navController, viewModel = viewModel)
        }
        composable(Screen.Settings.route) {
            SettingsScreen(navController = navController, viewModel = viewModel)
        }
        composable(Screen.About.route) {
            AboutScreen(navController = navController)
        }
        composable(Screen.Calculator.route) {
            CalculatorScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.DnsChanger.route) {
            val context = androidx.compose.ui.platform.LocalContext.current
            LaunchedEffect(Unit) {
                context.startActivity(android.content.Intent(context, com.example.DnsChangerActivity::class.java))
                navController.popBackStack()
            }
        }
        composable(Screen.AnimationSettings.route) {
            val context = androidx.compose.ui.platform.LocalContext.current
            LaunchedEffect(Unit) {
                context.startActivity(android.content.Intent(context, com.example.AnimationSettingsActivity::class.java))
                navController.popBackStack()
            }
        }
        composable(Screen.AppList.route) {
            val app = androidx.compose.ui.platform.LocalContext.current.applicationContext as com.example.NameGenApplication
            AppListScreen(viewModel = app.appManagerViewModel, onBack = { navController.popBackStack() })
        }
        composable(Screen.FakeGps.route) {
            FakeGpsMapScreen(navController = navController)
        }
        composable(Screen.Modules.route) {
            ModuleScreen(viewModel = viewModel, onNavigateBack = { navController.popBackStack() })
        }
        composable(Screen.DeepScanner.route) {
            com.example.ui.screens.DeepScannerScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.SecurityScan.route) {
            com.example.ui.screens.SecurityScanScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.ImageMetadataEditor.route) {
            ImageMetadataEditorScreen(navController = navController, viewModel = viewModel)
        }
    }
}
