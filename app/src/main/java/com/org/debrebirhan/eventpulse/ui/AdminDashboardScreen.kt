package com.org.debrebirhan.eventpulse.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.org.debrebirhan.eventpulse.viewmodel.AdminViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    adminViewModel: AdminViewModel,
    onManageUsers: () -> Unit,
    onManageEvents: () -> Unit,
    onCRUDEvents: () -> Unit,
    onLogout: () -> Unit
) {

    val totalEvents by adminViewModel.totalEventsCount.collectAsState()
    val totalUsers by adminViewModel.totalUsersCount.collectAsState()
    val pendingApprovals by adminViewModel.pendingApprovalsCount.collectAsState()

    val adminBgColor = Color(0xFF121212)
    val accentOrange = Color(0xFFD35400)

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        adminViewModel.fetchStats()
        adminViewModel.fetchPendingEvents()
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(280.dp),
                drawerContainerColor = Color(0xFF161616)
            ) {
                Column(
                    modifier = Modifier.fillMaxHeight().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Event Pulse", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp)
                    Text("ADMIN PANEL", color = Color.Gray, fontSize = 11.sp, modifier = Modifier.padding(bottom = 20.dp))

                    AdminNavItem("Dashboard", Icons.Default.Dashboard, isSelected = true) {
                        scope.launch { drawerState.close() }
                    }
                    AdminNavItem("Event Requests", Icons.Default.Notifications) {
                        scope.launch { drawerState.close() }; onManageEvents()
                    }
                    AdminNavItem("Manage Users", Icons.Default.People) {
                        scope.launch { drawerState.close() }; onManageUsers()
                    }
                    AdminNavItem("All Events", Icons.Default.EventNote) {
                        scope.launch { drawerState.close() }; onCRUDEvents()
                    }

                    Spacer(modifier = Modifier.weight(1f))
                    AdminNavItem("Logout", Icons.AutoMirrored.Filled.ExitToApp, color = Color.Red) { onLogout() }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Admin Dashboard", color = Color.White, fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = adminBgColor)
                )
            },
            containerColor = adminBgColor
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                Text("System Overview", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Text("Real-time database statistics", color = Color.Gray, fontSize = 14.sp)

                Spacer(modifier = Modifier.height(30.dp))


                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    StatCard(
                        title = "Total Approved Events",
                        value = totalEvents.toString(),
                        info = "Live Events",
                        color = accentOrange
                    )

                    StatCard(
                        title = "Total Platform Users",
                        value = totalUsers.toString(),
                        info = "Registered",
                        color = Color(0xFF3498DB)
                    )

                    StatCard(
                        title = "Pending Approvals",
                        value = pendingApprovals.toString(),
                        info = "Review Needed",
                        color = Color(0xFFF1C40F)
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))


                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF1E1E1E))
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("All systems are operational", color = Color(0xFF2ECC71), fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
fun StatCard(title: String, value: String, info: String, color: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(24.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(title, color = Color.Gray, fontSize = 13.sp)
                Text(value, color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.ExtraBold)
            }
            Surface(
                color = color.copy(alpha = 0.15f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = info,
                    color = color,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
fun AdminNavItem(label: String, icon: ImageVector, isSelected: Boolean = false, color: Color = Color.Gray, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) Color(0xFFD35400).copy(alpha = 0.15f) else Color.Transparent)
            .clickable { onClick() }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = if (isSelected) Color(0xFFD35400) else color, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(label, color = if (isSelected) Color.White else color, fontSize = 16.sp)
    }
}