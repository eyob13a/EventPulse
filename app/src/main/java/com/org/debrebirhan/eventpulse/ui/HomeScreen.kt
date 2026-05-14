package com.org.debrebirhan.eventpulse.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.org.debrebirhan.eventpulse.viewmodel.AuthViewModel
import com.org.debrebirhan.eventpulse.viewmodel.EventViewModel
import com.org.debrebirhan.eventpulse.data.Event

// ---------------- CATEGORY MODEL ----------------
data class EventCategory(
    val name: String,
    val icon: ImageVector,
    val color: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    authViewModel: AuthViewModel,
    eventViewModel: EventViewModel = viewModel(),
    navController: NavController,
    onLogout: () -> Unit,
    onEventClick: (String) -> Unit,
    onAddEventClick: () -> Unit
) {

    val eventColor = Color(0xFFD35400)

    val eventsList = eventViewModel.events.value
    val isLoading = eventViewModel.isFetching.value

    val userMap by authViewModel.userData.collectAsState()
    val userName = userMap?.get("fullName")?.toString() ?: ""
    val userRole = userMap?.get("role")?.toString() ?: "user"

    val isLoggedIn = authViewModel.isUserLoggedIn()

    // 🚩 ካቴጎሪዎቹ ከ CreateEventScreen ጋር አንድ አይነት እንዲሆኑ ተደርጓል
    val categories = listOf(
        EventCategory("Concerts", Icons.Default.MusicNote, Color(0xFFE74C3C)),
        EventCategory("Culture", Icons.Default.Public, Color(0xFFF39C12)),
        EventCategory("Seminars", Icons.Default.School, Color(0xFF3498DB)),
        EventCategory("Festivals", Icons.Default.Celebration, Color(0xFF9B59B6)),
        EventCategory("Sports", Icons.Default.SportsSoccer, Color(0xFF27AE60)),
        EventCategory("Exhibitions", Icons.Default.TheaterComedy, Color(0xFF34495E))
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Event Pulse", color = Color.White, fontWeight = FontWeight.Bold)
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = eventColor),
                actions = {
                    if (isLoggedIn) {
                        IconButton(onClick = {
                            authViewModel.logout()
                            onLogout()
                        }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ExitToApp,
                                contentDescription = "Logout",
                                tint = Color.White
                            )
                        }
                    } else {
                        TextButton(onClick = { navController.navigate("login") }) {
                            Text(
                                "Sign In",
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            )
        },

        floatingActionButton = {
            // 🚩 አድሚኑን አውጥተን ለ Organizer ብቻ እንዲታይ ተደርጓል
            if (isLoggedIn && userRole == "Organizer") {
                FloatingActionButton(
                    onClick = onAddEventClick,
                    containerColor = eventColor
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Event", tint = Color.White)
                }
            }
        }
    ) { paddingValues ->

        if (eventsList.isEmpty() && isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = eventColor)
            }
        } else {

            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {

                item {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = if (isLoggedIn && userName.isNotEmpty()) "Welcome, $userName!" else "Welcome to Event Pulse!",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isLoggedIn) "Discover Amazing Events" else "Sign in to book your favorite events",
                            fontSize = 16.sp,
                            color = Color.Gray
                        )
                    }
                }

                item {
                    Column {
                        Text(
                            text = "Featured Events",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(eventsList.take(5)) { event ->
                                FeaturedEventItem(
                                    event = event,
                                    onClick = { onEventClick(event.id) }
                                )
                            }
                        }
                    }
                }

                item {
                    Column(modifier = Modifier.padding(top = 8.dp)) {

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Categories", fontSize = 20.sp, fontWeight = FontWeight.Bold)

                            Text(
                                text = "Refresh",
                                color = eventColor,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.clickable { eventViewModel.fetchEvents() }
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Column(modifier = Modifier.padding(horizontal = 8.dp)) {
                            categories.chunked(3).forEach { rowItems ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    rowItems.forEach { category ->
                                        CategoryItem(category = category) {
                                            eventViewModel.fetchEventsByCategory(category.name)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryItem(category: EventCategory, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(8.dp).clickable { onClick() }
    ) {
        Box(
            modifier = Modifier.size(65.dp).clip(CircleShape).background(category.color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = category.icon,
                contentDescription = category.name,
                tint = category.color,
                modifier = Modifier.size(30.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = category.name, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun FeaturedEventItem(event: Event, onClick: () -> Unit) {
    val eventColor = Color(0xFFD35400)
    Card(
        modifier = Modifier.width(260.dp).height(320.dp).clickable { onClick() },
        shape = RoundedCornerShape(20.dp)
    ) {
        Column {
            AsyncImage(
                model = event.imageUrl.ifEmpty { "https://via.placeholder.com/400x250?text=Event" },
                contentDescription = null,
                modifier = Modifier.fillMaxWidth().height(150.dp),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.padding(12.dp)) {
                Text(text = event.date, color = eventColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text(text = event.title, fontSize = 16.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(event.location, fontSize = 12.sp, color = Color.Gray)
                }
                Spacer(modifier = Modifier.weight(1f))
                Button(
                    onClick = onClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = eventColor)
                ) {
                    Text("View Detail", color = Color.White)
                }
            }
        }
    }
}