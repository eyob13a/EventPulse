package com.org.debrebirhan.eventpulse.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.org.debrebirhan.eventpulse.data.Event
import com.org.debrebirhan.eventpulse.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(
    event: Event,
    authViewModel: AuthViewModel,
    navController: NavController,
    onBack: () -> Unit
) {
    val eventColor = Color(0xFFD35400) // Deep Orange color

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Event Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black
                )
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {

            // --- EVENT IMAGE ---
            AsyncImage(
                model = event.imageUrl.ifEmpty {
                    "https://via.placeholder.com/400x250?text=Event+Image"
                },
                contentDescription = "Event Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp),
                contentScale = ContentScale.Crop
            )

            Column(modifier = Modifier.padding(20.dp)) {

                // --- TITLE ---
                Text(
                    text = event.title,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = eventColor
                )

                Spacer(modifier = Modifier.height(20.dp))

                // --- DATE ---
                Row {
                    Icon(Icons.Default.DateRange, contentDescription = null, tint = Color.Gray)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(text = event.date, fontSize = 16.sp, color = Color.DarkGray)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // --- LOCATION ---
                Row {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.Gray)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(text = event.location, fontSize = 16.sp, color = Color.DarkGray)
                }

                Spacer(modifier = Modifier.height(20.dp))

                // --- PRICE SECTION ---
                Card(
                    colors = CardDefaults.cardColors(containerColor = eventColor.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Ticket Price: ${event.price} ETB",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = eventColor
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // --- ABOUT SECTION ---
                Text(
                    text = "About this event",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = event.description.ifEmpty { "No description available for this event." },
                    fontSize = 16.sp,
                    color = Color.DarkGray,
                    lineHeight = 24.sp
                )

                Spacer(modifier = Modifier.height(40.dp))

                // --- BOOK TICKET BUTTON ---
                Button(
                    onClick = {
                        if (authViewModel.isUserLoggedIn()) {


                            navController.navigate("payment/${event.title}/${event.price}/${event.date}")
                        } else {

                            navController.navigate("login")
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = eventColor)
                ) {
                    Text(
                        text = "Book Ticket Now",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}