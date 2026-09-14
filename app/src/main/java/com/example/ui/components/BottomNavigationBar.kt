package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.Immutable
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.example.ui.navigation.Screen

fun NavController.navigateSingleTopWithRestore(route: String) {
    this.navigate(route) {
        launchSingleTop = true
        restoreState = true
        popUpTo(this@navigateSingleTopWithRestore.graph.findStartDestination().id) {
            saveState = true
        }
    }
}

@Immutable
data class BottomNavItem(
    val title: String,
    val route: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem("Home", Screen.Home.route, Icons.Filled.Home, Icons.Outlined.Home),
    BottomNavItem("Generate", Screen.GenerateDashboard.route, Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome),
    BottomNavItem("Modules", Screen.Modules.route, Icons.Filled.Extension, Icons.Outlined.Extension),
    BottomNavItem("System", Screen.SystemDashboard.route, Icons.Filled.Dns, Icons.Outlined.Dns),
    BottomNavItem("History", Screen.History.route, Icons.Filled.History, Icons.Outlined.History),
    BottomNavItem("Settings", Screen.Settings.route, Icons.Filled.Settings, Icons.Outlined.Settings)
)

@Composable
fun CustomBottomNavigationBar(
    navController: NavController,
    currentDestination: NavDestination?,
    isDarkTheme: Boolean
) {
    Surface(
        modifier = Modifier
            .padding(horizontal = 14.dp, vertical = 18.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = if (isDarkTheme) Color(0xFF16161E) else Color(0xFFFFFFFF),
        shadowElevation = if (isDarkTheme) 0.dp else 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(66.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            bottomNavItems.forEach { item ->
                val isSelected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            navController.navigateSingleTopWithRestore(item.route)
                        },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(width = 42.dp, height = 26.dp)
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.Transparent,
                                RoundedCornerShape(percent = 50)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                            contentDescription = item.title,
                            modifier = Modifier.size(20.dp),
                            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = item.title,
                        fontSize = 9.sp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
