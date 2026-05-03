package com.org.debrebirhan.eventpulse.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.ConfirmationNumber
import androidx.compose.material.icons.outlined.Person
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(val route: String, val title: String, val icon: ImageVector) {
    object Home : BottomNavItem("home", "Home", Icons.Outlined.Home)
    object Search : BottomNavItem("search", "Search", Icons.Outlined.Search)
    object Tickets : BottomNavItem("tickets", "My Tickets", Icons.Outlined.ConfirmationNumber)
    object Profile : BottomNavItem("profile", "Profile", Icons.Outlined.Person)
}