package com.org.debrebirhan.eventpulse.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    object Home : BottomNavItem("home", "Home", Icons.Default.Home)
    object Search : BottomNavItem("search", "Search", Icons.Default.Search)
    object Tickets : BottomNavItem("tickets", "Tickets", Icons.Default.ConfirmationNumber)
    object Profile : BottomNavItem("profile", "Profile", Icons.Default.Person)
}