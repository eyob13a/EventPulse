package com.org.debrebirhan.eventpulse.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.org.debrebirhan.eventpulse.viewmodel.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageUsersScreen(
    adminViewModel: AdminViewModel,
    onBack: () -> Unit
) {
    val users by adminViewModel.users.collectAsState()
    var userToDelete by remember { mutableStateOf<Pair<String, String>?>(null) } // UID and Name

    // ስክሪኑ ሲከፈት ዳታውን ይጭናል
    LaunchedEffect(Unit) {
        adminViewModel.fetchAllUsers()
    }

    // የማጥፊያ ማረጋገጫ (Delete Confirmation Dialog)
    if (userToDelete != null) {
        AlertDialog(
            onDismissRequest = { userToDelete = null },
            title = { Text("Confirm Delete") },
            text = { Text("Are you sure you want to delete ${userToDelete?.second}?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        userToDelete?.let { pair ->
                            adminViewModel.deleteUser(pair.first) {
                                userToDelete = null
                            }
                        }
                    }
                ) { Text("Delete", color = Color.Red) }
            },
            dismissButton = {
                TextButton(onClick = { userToDelete = null }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Users", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (users.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFFD35400))
                Text("Loading users...", modifier = Modifier.padding(top = 80.dp), color = Color.Gray)
            }
        } else {
            LazyColumn(modifier = Modifier.padding(padding).fillMaxSize()) {
                items(users) { user ->
                    val uid = user["uid"]?.toString().orEmpty()
                    val name = user["fullName"]?.toString() ?: "Unknown User"
                    val email = user["email"]?.toString() ?: "No Email"
                    val role = user["role"]?.toString() ?: "Attendee"

                    ListItem(
                        headlineContent = { Text(name, fontWeight = FontWeight.SemiBold) },
                        supportingContent = {
                            Column {
                                Text(email, fontSize = 14.sp)
                                Text(
                                    text = "Role: $role",
                                    color = if (role == "admin") Color.Red else Color(0xFFD35400),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        },
                        leadingContent = {
                            Surface(
                                shape = MaterialTheme.shapes.small,
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Person, contentDescription = null)
                                }
                            }
                        },
                        trailingContent = {
                            IconButton(onClick = { userToDelete = uid to name }) {
                                Icon(Icons.Default.Delete, tint = Color.Red, contentDescription = "Delete User")
                            }
                        }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp)
                }
            }
        }
    }
}