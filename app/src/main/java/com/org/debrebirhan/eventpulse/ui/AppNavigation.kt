package com.org.debrebirhan.eventpulse.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.org.debrebirhan.eventpulse.navigation.BottomNavItem
import com.org.debrebirhan.eventpulse.viewmodel.AuthViewModel
import com.org.debrebirhan.eventpulse.viewmodel.EventViewModel

@Composable
fun AppNavigation(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    eventViewModel: EventViewModel,
    modifier: Modifier = Modifier
) {
    val startDestination = if (authViewModel.isUserLoggedIn()) BottomNavItem.Home.route else "login"

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {

        composable("login") {
            LoginScreen(
                viewModel = authViewModel,
                onNavigateToSignUp = { navController.navigate("signup") },
                onLoginSuccess = {
                    navController.navigate(BottomNavItem.Home.route) {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        composable("signup") {
            SignUpScreen(viewModel = authViewModel, onNavigateBack = { navController.popBackStack() })
        }

        composable(BottomNavItem.Home.route) {
            HomeScreen(
                authViewModel = authViewModel,
                eventViewModel = eventViewModel,
                onLogout = {
                    authViewModel.logout()
                    navController.navigate("login") { popUpTo(BottomNavItem.Home.route) { inclusive = true } }
                },
                onEventClick = { eventId -> navController.navigate("detail/$eventId") },
                onAddEventClick = { navController.navigate("create_event") }
            )
        }


        composable(BottomNavItem.Search.route) {
            SearchScreen(
                navController = navController,
                authViewModel = authViewModel,
                eventViewModel = eventViewModel,
                onLogout = {
                    authViewModel.logout()
                    navController.navigate("login") {
                        popUpTo(BottomNavItem.Home.route) { inclusive = true }
                    }
                }
            )
        }

        composable(BottomNavItem.Tickets.route) {
            MyTicketsScreen(navController, eventViewModel, authViewModel)
        }

        composable(BottomNavItem.Profile.route) {
            ProfileScreen(navController, authViewModel)
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

        composable("detail/{eventId}") { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId")
            val event = eventViewModel.events.value.find { it.id == eventId }
            event?.let {
                EventDetailScreen(
                    event = it,
                    onBack = { navController.popBackStack() },
                    onBookTicket = { navController.navigate("payment/${it.title}/${it.price}") }
                )
            }
        }

        composable("payment/{eventName}/{price}") { backStackEntry ->
            val eventName = backStackEntry.arguments?.getString("eventName") ?: ""
            val price = backStackEntry.arguments?.getString("price") ?: ""
            PaymentScreen(
                eventName = eventName,
                price = price,
                onBack = { navController.popBackStack() },
                onPaymentSuccess = {
                    navController.navigate(BottomNavItem.Home.route) {
                        popUpTo(BottomNavItem.Home.route) { inclusive = true }
                    }
                }
            )
        }
    }
}