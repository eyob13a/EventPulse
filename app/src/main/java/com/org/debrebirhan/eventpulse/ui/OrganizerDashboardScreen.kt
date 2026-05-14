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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.org.debrebirhan.eventpulse.viewmodel.EventViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrganizerDashboardScreen(
    eventViewModel: EventViewModel = viewModel(),
    onLogout: () -> Unit,
    onCreateEvent: () -> Unit,
    onEventClick: (String) -> Unit
) {
    val eventPulseOrange = Color(0xFFD35400)
    val eventsList = eventViewModel.events.value
    val isLoading = eventViewModel.isFetching.value

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
                title = { Text("Organizer Dashboard", color = Color.White, fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Logout", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = eventPulseOrange)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateEvent,
                containerColor = eventPulseOrange,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Event", tint = Color.White)
            }
        }
    ) { paddingValues ->
        if (eventsList.isEmpty() && isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = eventPulseOrange)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                item {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "Welcome Back, Organizer!", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        Text(text = "Manage and Discover Events", fontSize = 16.sp, color = Color.Gray)
                    }
                }

                item {
                    Column {
                        Text(text = "Your Featured Events", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp))
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(eventsList.take(5)) { event ->
                                FeaturedEventItem(event = event, onClick = { onEventClick(event.id) })
                            }
                        }
                    }
                }

                item {
                    Column(modifier = Modifier.padding(top = 8.dp)) {
                        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Categories", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            Text(text = "Refresh", color = eventPulseOrange, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { eventViewModel.fetchEvents() })
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Column(modifier = Modifier.padding(horizontal = 8.dp)) {
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
}