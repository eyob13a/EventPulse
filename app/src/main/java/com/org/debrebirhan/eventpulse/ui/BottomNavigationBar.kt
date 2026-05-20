package com.org.debrebirhan.eventpulse.ui

import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.org.debrebirhan.eventpulse.navigation.BottomNavItem
import com.org.debrebirhan.eventpulse.viewmodel.AuthViewModel
import com.org.debrebirhan.eventpulse.viewmodel.EventViewModel

@Composable
fun EventPulseBottomNavigation(
    navController: NavController,
    eventViewModel: EventViewModel,
    authViewModel: AuthViewModel
) {

    val eventPulseOrange = Color(0xFFD35400)
    val inactiveGray = Color(0xFF95A5A6)

    val userMap by authViewModel.userData.collectAsState()
    val userRole = userMap?.get("role")?.toString() ?: "user"

    // Hide Bottom Navigation for Admin
    if (userRole != "admin") {

        val items = listOf(
            BottomNavItem.Home,
            BottomNavItem.Search,
            BottomNavItem.Tickets,
            BottomNavItem.Profile
        )

        NavigationBar(
            containerColor = Color.White,
            tonalElevation = 12.dp
        ) {

            val navBackStackEntry by
            navController.currentBackStackEntryAsState()

            val currentRoute =
                navBackStackEntry?.destination?.route

            items.forEach { item ->

                val isSelected =
                    currentRoute == item.route

                NavigationBarItem(

                    icon = {

                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.title,

                            modifier = Modifier.size(
                                if (isSelected)
                                    28.dp
                                else
                                    24.dp
                            ),

                            tint =
                                if (isSelected)
                                    eventPulseOrange
                                else
                                    inactiveGray
                        )
                    },

                    label = {

                        Text(
                            text = item.title,

                            fontSize = 11.sp,

                            fontWeight =
                                if (isSelected)
                                    FontWeight.Bold
                                else
                                    FontWeight.Medium,

                            color =
                                if (isSelected)
                                    eventPulseOrange
                                else
                                    inactiveGray
                        )
                    },

                    selected = isSelected,

                    onClick = {

                        // Refresh Home Events
                        if (item.route ==
                            BottomNavItem.Home.route
                        ) {

                            eventViewModel.fetchEvents()
                        }

                        // Prevent reopening same screen
                        if (currentRoute != item.route) {

                            navController.navigate(item.route) {

                                popUpTo(
                                    navController.graph.startDestinationId
                                ) {
                                    saveState = true
                                }

                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },

                    colors = NavigationBarItemDefaults.colors(

                        indicatorColor =
                            eventPulseOrange.copy(alpha = 0.1f),

                        selectedIconColor =
                            eventPulseOrange,

                        unselectedIconColor =
                            inactiveGray,

                        selectedTextColor =
                            eventPulseOrange,

                        unselectedTextColor =
                            inactiveGray
                    )
                )
            }
        }
    }
}