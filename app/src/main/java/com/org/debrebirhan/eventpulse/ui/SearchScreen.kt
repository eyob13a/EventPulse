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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.org.debrebirhan.eventpulse.viewmodel.AuthViewModel
import com.org.debrebirhan.eventpulse.viewmodel.EventViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    eventViewModel: EventViewModel,
    onLogout: () -> Unit
) {
    val eventPulseOrange = Color(0xFFD35400)
    var searchQuery by remember { mutableStateOf("") }
    val eventsList = eventViewModel.events.value

    val filteredEvents = eventsList.filter {
        it.title.contains(searchQuery, ignoreCase = true) ||
                it.location.contains(searchQuery, ignoreCase = true)
    }

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
                title = { Text("Event Pulse", color = Color.White, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = eventPulseOrange),
                actions = {
                    IconButton(onClick = {
                        onLogout()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout", tint = Color.White)
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Explore Events", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Search by name or location...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = eventPulseOrange,
                            focusedContainerColor = Color(0xFFF8F8F8),
                            unfocusedContainerColor = Color(0xFFF8F8F8)
                        ),
                        singleLine = true
                    )
                }
            }

            item {
                Column {
                    Text(
                        text = if (searchQuery.isEmpty()) "Featured Events" else "Search Results",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )

                    if (filteredEvents.isEmpty()) {
                        Text("No events found.", modifier = Modifier.padding(16.dp), color = Color.Gray)
                    } else {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(filteredEvents) { event ->
                                FeaturedEventItem(event = event, onClick = { navController.navigate("detail/${event.id}") })
                            }
                        }
                    }
                }
            }

            item {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    Text(
                        text = "Categories",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
                        categories.chunked(3).forEach { rowItems ->
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
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