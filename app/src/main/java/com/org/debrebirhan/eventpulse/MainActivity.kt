package com.org.debrebirhan.eventpulse

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.org.debrebirhan.eventpulse.ui.EventPulseBottomNavigation
import com.org.debrebirhan.eventpulse.ui.AppNavigation
import com.org.debrebirhan.eventpulse.viewmodel.AuthViewModel
import com.org.debrebirhan.eventpulse.viewmodel.EventViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainAppContent()
                }
            }
        }
    }
}

@Composable
fun MainAppContent() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val eventViewModel: EventViewModel = viewModel()

    val isLoggedIn = authViewModel.isUserLoggedIn()

    if (isLoggedIn) {
        Scaffold(
            bottomBar = {

                EventPulseBottomNavigation(
                    navController = navController,
                    eventViewModel = eventViewModel
                )
            }
        ) { innerPadding ->
            AppNavigation(
                navController = navController,
                authViewModel = authViewModel,
                eventViewModel = eventViewModel,
                modifier = Modifier.padding(innerPadding)
            )
        }
    } else {
        AppNavigation(
            navController = navController,
            authViewModel = authViewModel,
            eventViewModel = eventViewModel
        )
    }
}