package com.org.debrebirhan.eventpulse.ui

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.org.debrebirhan.eventpulse.navigation.BottomNavItem
import com.org.debrebirhan.eventpulse.viewmodel.EventViewModel

@Composable
fun EventPulseBottomNavigation(
    navController: NavController,
    eventViewModel: EventViewModel
) {

    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Search,
        BottomNavItem.Tickets,
        BottomNavItem.Profile
    )

    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEach { item ->
            val isSelected = currentRoute == item.route

            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title,

                        tint = if (isSelected) Color(0xFFD35400) else Color.Gray
                    )
                },
                label = {
                    Text(
                        text = item.title,
                        color = if (isSelected) Color(0xFFD35400) else Color.Gray,
                        fontSize = 10.sp
                    )
                },
                selected = isSelected,
                onClick = {

                    if (item.route == BottomNavItem.Home.route) {
                        eventViewModel.fetchEvents()
                    }

                    navController.navigate(item.route) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = Color.Transparent,
                    selectedIconColor = Color(0xFFD35400),
                    unselectedIconColor = Color.Gray,
                    selectedTextColor = Color(0xFFD35400),
                    unselectedTextColor = Color.Gray
                )
            )
        }
    }
}