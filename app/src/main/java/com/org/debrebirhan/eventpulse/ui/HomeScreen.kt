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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import coil.compose.AsyncImage
import com.org.debrebirhan.eventpulse.data.Event
import com.org.debrebirhan.eventpulse.viewmodel.AuthViewModel
import com.org.debrebirhan.eventpulse.viewmodel.EventViewModel


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
    onLogout: () -> Unit,
    onEventClick: (String) -> Unit,
    onAddEventClick: () -> Unit
) {
    val eventPulseOrange = Color(0xFFD35400)
    val eventsList = eventViewModel.events.value

    val userMap by authViewModel.userData.collectAsState()
    val userName = userMap?.get("fullName")?.toString() ?: ""


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
                        authViewModel.logout()
                        onLogout()
                    }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Logout",
                            tint = Color.White
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddEventClick,
                containerColor = eventPulseOrange,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Event")
            }
        }
    ) { paddingValues ->
        if (eventsList.isEmpty() && eventViewModel.isFetching.value) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = eventPulseOrange)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                item {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = if (userName.isNotEmpty()) "Welcome, $userName!" else "Welcome!",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Discover Amazing Events",
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
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
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
                    Column(modifier = Modifier.padding(top = 16.dp)) {

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Categories",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )


                            Text(
                                text = "See All",
                                color = eventPulseOrange,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                modifier = Modifier.clickable {
                                    eventViewModel.fetchEvents()
                                }
                            )
                        }

                        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
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
        modifier = Modifier
            .padding(8.dp)
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(65.dp)
                .clip(CircleShape)
                .background(category.color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = category.icon,
                contentDescription = category.name,
                tint = category.color,
                modifier = Modifier.size(30.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = category.name,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black
        )
    }
}

@Composable
fun FeaturedEventItem(event: Event, onClick: () -> Unit) {
    val eventPulseOrange = Color(0xFFD35400)
    Card(
        modifier = Modifier
            .width(280.dp)
            .height(340.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            AsyncImage(
                model = event.imageUrl.ifEmpty { "https://via.placeholder.com/400x250?text=Event" },
                contentDescription = null,
                modifier = Modifier.fillMaxWidth().height(160.dp),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = event.date, color = eventPulseOrange, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text(text = event.title, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = Color.Gray
                    )
                    Text(text = event.location, fontSize = 13.sp, color = Color.Gray)
                }
                Spacer(modifier = Modifier.weight(1f))
                Button(
                    onClick = onClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = eventPulseOrange),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Book Ticket", color = Color.White)
                }
            }
        }
    }
}