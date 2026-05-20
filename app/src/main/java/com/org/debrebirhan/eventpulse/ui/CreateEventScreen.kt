package com.org.debrebirhan.eventpulse.ui

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.org.debrebirhan.eventpulse.viewmodel.AuthViewModel
import com.org.debrebirhan.eventpulse.viewmodel.EventViewModel
import com.org.debrebirhan.eventpulse.data.Event
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEventScreen(
    authViewModel: AuthViewModel,
    eventViewModel: EventViewModel,
    organizerId: String,
    eventId: String? = null,
    onBack: () -> Unit,
    onSuccess: () -> Unit
) {
    val context = LocalContext.current
    val eventPulseOrange = Color(0xFFD35400)

    // --- State Variables ---
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var capacity by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var existingImageUrl by remember { mutableStateOf("") }
    var dateText by remember { mutableStateOf("") }

    // --- Category Dropdown State ---
    val categories = listOf("Concerts", "Culture", "Seminars", "Festivals", "Sports", "Exhibitions", "Other")
    var selectedCategory by remember { mutableStateOf(categories[0]) }
    var expanded by remember { mutableStateOf(false) }

    // --- Date Picker State ---
    val datePickerState = rememberDatePickerState()
    var showDatePicker by remember { mutableStateOf(false) }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    // Edit Mode logic
    LaunchedEffect(eventId) {
        if (eventId != null) {
            val event = eventViewModel.events.value.find { it.id == eventId }
            event?.let {
                title = it.title
                description = it.description
                location = it.location
                price = it.price
                capacity = it.capacity
                dateText = it.date
                selectedCategory = it.category
                existingImageUrl = it.imageUrl
            }
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? -> selectedImageUri = uri }

    // Date Picker Dialog UI
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val date = datePickerState.selectedDateMillis?.let {
                        SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(it))
                    } ?: ""
                    dateText = date
                    showDatePicker = false
                }) { Text("OK", color = eventPulseOrange) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) { DatePicker(state = datePickerState) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (eventId == null) "Create New Event" else "Edit Event",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // IMAGE PICKER SECTION
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                    .clickable { imagePickerLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (selectedImageUri != null || existingImageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = selectedImageUri ?: existingImageUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.AddAPhoto,
                            contentDescription = null,
                            tint = eventPulseOrange,
                            modifier = Modifier.size(40.dp)
                        )
                        Text("Add Event Poster", color = Color.Gray, fontSize = 12.sp)
                    }
                }
            }

            // EVENT TITLE
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Event Title") },
                modifier = Modifier.fillMaxWidth()
            )

            // CATEGORY DROPDOWN
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = selectedCategory,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Category") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    categories.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category) },
                            onClick = {
                                selectedCategory = category
                                expanded = false
                            }
                        )
                    }
                }
            }

            // DATE PICKER FIELD
            OutlinedTextField(
                value = dateText,
                onValueChange = {},
                readOnly = true,
                label = { Text("Event Date") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = "Pick Date")
                    }
                }
            )

            // PRICE & CAPACITY ROW
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Price (ETB)") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = capacity,
                    onValueChange = { capacity = it },
                    label = { Text("Capacity") },
                    modifier = Modifier.weight(1f)
                )
            }

            // LOCATION & DESCRIPTION
            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Venue Location") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            // ERROR MESSAGE
            if (errorMessage.isNotEmpty()) {
                Text(
                    errorMessage,
                    color = Color.Red,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // PUBLISH / UPDATE BUTTON
            Button(
                onClick = {
                    if (title.isBlank() || dateText.isBlank() || location.isBlank()) {
                        errorMessage = "Title, Date and Location are required!"
                        return@Button
                    }
                    isLoading = true
                    errorMessage = ""

                    val saveAction: (String) -> Unit = { imageUrl ->
                        val eventToSave = Event(
                            id = eventId ?: "",
                            title = title,
                            description = description,
                            location = location,
                            date = dateText,
                            price = price,
                            capacity = capacity,
                            imageUrl = imageUrl,
                            organizerId = organizerId,
                            category = selectedCategory,
                            status = "pending"
                        )

                        if (eventId == null) {
                            eventViewModel.addEvent(
                                eventToSave,
                                onSuccess = {
                                    isLoading = false
                                    Toast.makeText(context, "Event published! Waiting for admin approval.", Toast.LENGTH_LONG).show()
                                    onSuccess()
                                },
                                onError = {
                                    isLoading = false
                                    errorMessage = it
                                    Toast.makeText(context, "Error: $it", Toast.LENGTH_SHORT).show()
                                }
                            )
                        } else {
                            eventViewModel.updateEvent(
                                eventToSave,
                                onSuccess = {
                                    isLoading = false
                                    Toast.makeText(context, "Event updated! Waiting for re-approval.", Toast.LENGTH_LONG).show()
                                    onSuccess()
                                },
                                onError = {
                                    isLoading = false
                                    errorMessage = it
                                    Toast.makeText(context, "Update failed: $it", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }

                    // Image Upload Logic
                    if (selectedImageUri != null) {
                        authViewModel.uploadImage(context, selectedImageUri!!) { url ->
                            if (url != null) {
                                saveAction(url)
                            } else {
                                isLoading = false
                                errorMessage = "Image upload failed. Please try again."
                                Toast.makeText(context, "Image upload failed!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        saveAction(existingImageUrl)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = eventPulseOrange)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text(
                        if (eventId == null) "Publish Event" else "Update Event",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}