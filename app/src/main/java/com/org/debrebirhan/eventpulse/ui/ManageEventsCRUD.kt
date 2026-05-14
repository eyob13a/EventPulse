package com.org.debrebirhan.eventpulse.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.org.debrebirhan.eventpulse.viewmodel.AdminViewModel
import com.org.debrebirhan.eventpulse.viewmodel.EventViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageEventsCRUD(
    adminViewModel: AdminViewModel,
    eventViewModel: EventViewModel,
    onBack: () -> Unit,
    onAddEvent: () -> Unit,
    onEditEvent: (String) -> Unit
) {

    // ✅ Always load fresh data
    LaunchedEffect(Unit) {
        eventViewModel.fetchEvents()
    }

    // 🔥 SAFE: never allow null crash
    val events = eventViewModel.events.value ?: emptyList()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Events (CRUD)") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = onAddEvent) {
                        Icon(Icons.Default.Add, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->

        if (events.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Text("No events found", color = Color.Gray)
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {

            items(events) { event ->

                ListItem(
                    headlineContent = { Text(event.title ?: "No Title") },
                    supportingContent = { Text(event.date ?: "No Date") },
                    trailingContent = {

                        Row {

                            IconButton(onClick = {
                                onEditEvent(event.id ?: "")
                            }) {
                                Icon(Icons.Default.Edit, contentDescription = null)
                            }

                            IconButton(onClick = {
                                if (!event.id.isNullOrEmpty()) {
                                    adminViewModel.deleteEvent(event.id)
                                }
                            }) {
                                Icon(
                                    Icons.Default.Delete,
                                    tint = Color.Red,
                                    contentDescription = null
                                )
                            }
                        }
                    }
                )

                HorizontalDivider()
            }
        }
    }
}