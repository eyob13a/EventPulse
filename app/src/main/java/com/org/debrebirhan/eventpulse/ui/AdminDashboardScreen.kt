package com.org.debrebirhan.eventpulse.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.org.debrebirhan.eventpulse.viewmodel.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    adminViewModel: AdminViewModel,
    onManageUsers: () -> Unit,
    onManageEvents: () -> Unit,
    onCRUDEvents: () -> Unit,
    onLogout: () -> Unit
) {
    // አዲስ Pending የሆኑ ኢቨንቶችን ቁጥር ለማግኘት
    val pendingEvents by adminViewModel.pendingEvents.collectAsState()

    // ገጹ ሲከፈት ዳታውን እንዲያመጣ
    LaunchedEffect(Unit) {
        adminViewModel.fetchPendingEvents()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Panel", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Logout", tint = Color.Red)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AdminCard(
                title = "Manage Users",
                subtitle = "View and delete users",
                icon = Icons.Default.Person,
                onClick = onManageUsers
            )

            AdminCard(
                title = "Event Requests",
                // አዲስ ኢቨንት ካለ ቁጥሩን ያሳያል
                subtitle = if (pendingEvents.isNotEmpty()) "${pendingEvents.size} pending requests" else "No new requests",
                icon = Icons.Default.CheckCircle,
                onClick = onManageEvents,
                badgeCount = pendingEvents.size // ባጅ እንዲኖረው
            )

            AdminCard(
                title = "All Events",
                subtitle = "CRUD operations for events",
                icon = Icons.Default.DateRange,
                onCRUDEvents
            )
        }
    }
}

@Composable
fun AdminCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    badgeCount: Int = 0 // አዲስ የተጨመረ
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box {
                Icon(icon, contentDescription = null, modifier = Modifier.size(40.dp), tint = Color(0xFFD35400))
                // ቁጥር ካለ ቀይ ክብ ምልክት ያሳያል
                if (badgeCount > 0) {
                    Badge(
                        modifier = Modifier.align(Alignment.TopEnd),
                        containerColor = Color.Red
                    ) {
                        Text(badgeCount.toString(), color = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(subtitle, fontSize = 14.sp, color = if (badgeCount > 0) Color(0xFF2E7D32) else Color.Gray)
            }
        }
    }
} 