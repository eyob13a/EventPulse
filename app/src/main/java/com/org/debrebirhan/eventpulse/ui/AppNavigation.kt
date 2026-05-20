package com.org.debrebirhan.eventpulse.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.*
import androidx.navigation.compose.*
import com.org.debrebirhan.eventpulse.navigation.BottomNavItem
import com.org.debrebirhan.eventpulse.viewmodel.AdminViewModel
import com.org.debrebirhan.eventpulse.viewmodel.AuthViewModel
import com.org.debrebirhan.eventpulse.viewmodel.EventViewModel

@Composable
fun AppNavigation(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    eventViewModel: EventViewModel,
    modifier: Modifier = Modifier
) {
    val adminViewModel: AdminViewModel = viewModel()
    val userData by authViewModel.userData.collectAsState()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute != "login" &&
            currentRoute != "signup" &&
            currentRoute != "admin_dashboard" &&
            currentRoute != "organizer_dashboard" &&
            currentRoute != "approvals" &&
            currentRoute != "manage_users" &&
            currentRoute != "create_event" &&
            currentRoute != "manage_events_crud" &&
            currentRoute?.startsWith("update_event") == false &&
            currentRoute != "check_role" &&
            currentRoute?.startsWith("payment") == false

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                EventPulseBottomNavigation(
                    navController = navController,
                    eventViewModel = eventViewModel,
                    authViewModel = authViewModel
                )
            }
        }
    ) { innerPadding ->

        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Home.route,
            modifier = modifier.padding(innerPadding)
        ) {

            // 🔹 ROLE CHECK
            composable("check_role") {
                LaunchedEffect(userData) {
                    if (userData == null) authViewModel.fetchUserProfile()
                    userData?.let { data ->
                        val role = data["role"] as? String
                        when (role) {
                            "admin" -> navController.navigate("admin_dashboard") { popUpTo("check_role") { inclusive = true } }
                            "Organizer" -> navController.navigate("organizer_dashboard") { popUpTo("check_role") { inclusive = true } }
                            else -> navController.navigate(BottomNavItem.Home.route) { popUpTo("check_role") { inclusive = true } }
                        }
                    }
                }
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFFD35400))
                }
            }

            // 🔹 LOGIN & SIGNUP
            composable("login") {
                LoginScreen(
                    viewModel = authViewModel,
                    onNavigateToSignUp = { navController.navigate("signup") },
                    onLoginSuccess = { navController.navigate("check_role") { popUpTo("login") { inclusive = true } } }
                )
            }
            composable("signup") {
                SignUpScreen(viewModel = authViewModel, onNavigateBack = { navController.popBackStack() })
            }

            // 🔹 HOME SCREEN
            composable(BottomNavItem.Home.route) {
                HomeScreen(
                    authViewModel = authViewModel,
                    eventViewModel = eventViewModel,
                    navController = navController,
                    onLogout = {
                        authViewModel.logout()
                        navController.navigate("login") { popUpTo(0) { inclusive = true } }
                    },
                    onEventClick = { eventId -> navController.navigate("detail/$eventId") },
                    onAddEventClick = {

                        navController.navigate("create_event")
                    }
                )
            }

            // 🔹 SEARCH SCREEN
            composable(BottomNavItem.Search.route) {
                SearchScreen(
                    navController = navController,
                    authViewModel = authViewModel,
                    eventViewModel = eventViewModel,
                    onLogout = {
                        authViewModel.logout()
                        navController.navigate("login") { popUpTo(0) { inclusive = true } }
                    }
                )
            }

            // 🔹 TICKETS
            composable(BottomNavItem.Tickets.route) {
                if (authViewModel.isUserLoggedIn()) {
                    MyTicketsScreen(
                        authViewModel = authViewModel,
                        eventViewModel = eventViewModel,
                        navController = navController
                    )
                } else {
                    LaunchedEffect(Unit) { navController.navigate("login") }
                }
            }

            // 🔹 PROFILE SCREEN
            composable(BottomNavItem.Profile.route) {
                if (authViewModel.isUserLoggedIn()) {
                    ProfileScreen(
                        navController = navController,
                        authViewModel = authViewModel
                    )
                } else {
                    LaunchedEffect(Unit) {
                        navController.navigate("login")
                    }
                }
            }

            // 🔹 EVENT DETAIL
            composable("detail/{eventId}") { backStackEntry ->
                val eventId = backStackEntry.arguments?.getString("eventId")
                val events by eventViewModel.events
                val event = events.find { it.id == eventId }

                event?.let {
                    EventDetailScreen(
                        event = it,
                        authViewModel = authViewModel,
                        navController = navController,
                        onBack = { navController.popBackStack() }
                    )
                }
            }

            // 🔹 PAYMENT SCREEN
            composable(
                route = "payment/{eventName}/{price}/{eventDate}",
                arguments = listOf(
                    navArgument("eventName") { type = NavType.StringType },
                    navArgument("price") { type = NavType.StringType },
                    navArgument("eventDate") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                PaymentScreen(
                    viewModel = authViewModel,
                    eventName = backStackEntry.arguments?.getString("eventName") ?: "",
                    price = backStackEntry.arguments?.getString("price") ?: "",
                    eventDate = backStackEntry.arguments?.getString("eventDate") ?: "",
                    onBack = { navController.popBackStack() },
                    onPaymentSuccess = {
                        navController.navigate(BottomNavItem.Tickets.route) {
                            popUpTo(BottomNavItem.Home.route) { inclusive = false }
                        }
                    }
                )
            }

            // 🔹 ORGANIZER DASHBOARD
            composable("organizer_dashboard") {
                OrganizerDashboardScreen(
                    eventViewModel = eventViewModel,
                    onLogout = {
                        authViewModel.logout()
                        navController.navigate("login") { popUpTo(0) { inclusive = true } }
                    },
                    onCreateEvent = { navController.navigate("create_event") },
                    onEventClick = { eventId -> navController.navigate("detail/$eventId") }
                )
            }

            // 🔹 ADMIN DASHBOARD
            composable("admin_dashboard") {
                AdminDashboardScreen(
                    adminViewModel = adminViewModel,
                    onManageUsers = { navController.navigate("manage_users") },
                    onManageEvents = { navController.navigate("approvals") },
                    onCRUDEvents = { navController.navigate("manage_events_crud") },
                    onLogout = {
                        authViewModel.logout()
                        navController.navigate("login") { popUpTo(0) { inclusive = true } }
                    }
                )
            }

            composable("create_event") {
                CreateEventScreen(
                    authViewModel = authViewModel,
                    eventViewModel = eventViewModel,
                    organizerId = authViewModel.currentUserId ?: "",
                    onBack = { navController.popBackStack() },
                    onSuccess = { navController.popBackStack() }
                )
            }
            composable("approvals") { ApprovalsScreen(adminViewModel = adminViewModel, onBack = { navController.popBackStack() }) }
            composable("manage_users") { ManageUsersScreen(adminViewModel = adminViewModel, onBack = { navController.popBackStack() }) }


            composable("manage_events_crud") {
                AllEventsScreen(
                    adminViewModel = adminViewModel,
                    onEditEvent = { event ->
                        navController.navigate("update_event/${event.id}")
                    },
                    onAddEvent = { navController.navigate("create_event") },
                    onBack = { navController.popBackStack() }
                )
            }


            composable("update_event/{eventId}") { backStackEntry ->
                val eventId = backStackEntry.arguments?.getString("eventId")
                val event = adminViewModel.allApprovedEvents.collectAsState().value.find { it.id == eventId }
                if (event != null) {
                    UpdateEventScreen(
                        event = event,
                        eventViewModel = eventViewModel,
                        onBack = { navController.popBackStack() },
                        onSuccess = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}