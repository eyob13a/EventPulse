package com.org.debrebirhan.eventpulse.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.org.debrebirhan.eventpulse.data.Event
import com.org.debrebirhan.eventpulse.viewmodel.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllEventsScreen(
    adminViewModel: AdminViewModel,
    onEditEvent: (Event) -> Unit,
    onAddEvent: () -> Unit,
    onBack: () -> Unit
) {
    val events by adminViewModel.allApprovedEvents.collectAsState()
    val adminBg = Color(0xFF121212)

    // 🚩 ለ Delete Confirmation የሚያገለግሉ State-ዎች
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedEventForDelete by remember { mutableStateOf<Event?>(null) }

    LaunchedEffect(Unit) {
        adminViewModel.fetchAllApprovedEvents()
    }

    // 🚩 የ Delete Confirmation Dialog (Cancel/OK)
    if (showDeleteDialog && selectedEventForDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Event ማጥፊያ") },
            text = { Text("'${selectedEventForDelete?.title}' የሚለውን ኢቨንት ማጥፋት ትፈልጋለህ?") },
            confirmButton = {
                TextButton(onClick = {
                    adminViewModel.deleteEvent(selectedEventForDelete!!.id)
                    showDeleteDialog = false
                    selectedEventForDelete = null
                }) {
                    Text("OK (አጥፋ)", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    selectedEventForDelete = null
                }) {
                    Text("Cancel (ተው)")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage All Events", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = adminBg)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddEvent,
                containerColor = Color(0xFFD35400)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Event", tint = Color.White)
            }
        },
        containerColor = adminBg
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            Text(
                text = "${events.size} Approved Events",
                color = Color.Gray,
                fontSize = 14.sp,
                modifier = Modifier.padding(16.dp)
            )

            if (events.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.EventBusy, contentDescription = null, tint = Color.DarkGray, modifier = Modifier.size(50.dp))
                        Text("No approved events found.", color = Color.DarkGray)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(events) { event ->
                        EventCrudCard(
                            event = event,
                            onEdit = { onEditEvent(event) }, // 🚩 ኤዲት ሲነካ ወደ AppNavigation ይመለሳል
                            onDelete = {
                                selectedEventForDelete = event
                                showDeleteDialog = true // 🚩 ዲያሎጉን እንዲያሳይ
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EventCrudCard(
    event: Event,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = event.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(text = "${event.date} • ${event.location}", color = Color.Gray, fontSize = 14.sp)

                Surface(
                    modifier = Modifier.padding(top = 8.dp),
                    color = Color(0xFF2ECC71).copy(alpha = 0.1f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "Approved",
                        color = Color(0xFF2ECC71),
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color(0xFF3498DB))
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFE74C3C))
                }
            }
        }
    }
}