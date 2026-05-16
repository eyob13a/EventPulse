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

    // የተጠቃሚውን ሮል እናወጣለን
    val userMap by authViewModel.userData.collectAsState()
    val userRole = userMap?.get("role")?.toString() ?: "user"

    // Admin ካልሆነ ብቻ ነው Bottom Navigation የሚታየው
    if (userRole != "admin") {
        val items = listOf(
            BottomNavItem.Home,
            BottomNavItem.Search,
            BottomNavItem.Tickets,
            BottomNavItem.Profile
        )

        NavigationBar(
            containerColor = Color.White,
            tonalElevation = 12.dp, // ለየት ያለ ጥላ እንዲኖረው
            modifier = Modifier
        ) {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route

            items.forEach { item ->
                // 🚩 ማሻሻያ፦ ተጠቃሚው Login ገጽ ላይ ቢሆንም እንኳ Tickets ወይም Profile መብራታቸውን በትክክል እንዲያውቅ ያደርጋል
                val isSelected = currentRoute == item.route ||
                        (currentRoute == "login" && (item.route == BottomNavItem.Tickets.route || item.route == BottomNavItem.Profile.route))

                NavigationBarItem(
                    icon = {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.title,
                            modifier = Modifier.size(if (isSelected) 28.dp else 24.dp), // ሲመረጥ ትንሽ ገዘፍ እንዲል
                            tint = if (isSelected) eventPulseOrange else inactiveGray
                        )
                    },
                    label = {
                        Text(
                            text = item.title,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) eventPulseOrange else inactiveGray
                        )
                    },
                    selected = isSelected,
                    onClick = {
                        // Home ከተነካ ዳታውን አዲስ ያደርጋል
                        if (item.route == BottomNavItem.Home.route) {
                            eventViewModel.fetchEvents()
                        }

                        // 🚩 ዋናው ማስተካከያ፦ ከትኬት ወይም ከሎጊን ገጽ ወደ ሆም ሲመለስ የ BackStack መቆለፍ ችግር እንዳይፈጠር ያደርጋል
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = eventPulseOrange.copy(alpha = 0.1f), // ሲመረጥ በስተጀርባ የሚኖረው ለስላሳ ቀለም
                        selectedIconColor = eventPulseOrange,
                        unselectedIconColor = inactiveGray,
                        selectedTextColor = eventPulseOrange,
                        unselectedTextColor = inactiveGray
                    )
                )
            }
        }
    }
}