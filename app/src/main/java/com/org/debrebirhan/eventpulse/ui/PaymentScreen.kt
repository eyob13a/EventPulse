package com.org.debrebirhan.eventpulse.ui

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.google.firebase.firestore.FirebaseFirestore
import com.org.debrebirhan.eventpulse.notification.ReminderWorker
import com.org.debrebirhan.eventpulse.viewmodel.AuthViewModel
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    viewModel: AuthViewModel,
    eventName: String,
    eventDate: String,
    price: String,
    onBack: () -> Unit,
    onPaymentSuccess: () -> Unit
) {
    val context = LocalContext.current
    var selectedMethod by remember { mutableStateOf("Telebirr") }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }

    val db = FirebaseFirestore.getInstance()


    fun scheduleReminder(eventName: String, eventDateString: String) {
        try {
            val sdf = SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault())
            val date = sdf.parse(eventDateString)

            if (date != null) {
                val currentTime = System.currentTimeMillis()

                val delay = (date.time - 300000) - currentTime

                val finalDelay = if (delay > 0) delay else 5000L

                val reminderRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
                    .setInitialDelay(finalDelay, TimeUnit.MILLISECONDS)
                    .setInputData(workDataOf("eventTitle" to eventName))
                    .build()

                WorkManager.getInstance(context).enqueue(reminderRequest)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Payment Successful!", fontWeight = FontWeight.Bold) },
            text = { Text("You have successfully booked your ticket for $eventName. Enjoy the event!") },
            confirmButton = {
                Button(
                    onClick = {
                        if (!isSaving) {
                            isSaving = true
                            val userId = viewModel.currentUserId

                            if (userId != null) {
                                val bookingId = UUID.randomUUID().toString()
                                val ticketNumber = "EP-${(1000..9999).random()}"

                                val bookingData = hashMapOf(
                                    "bookingId" to bookingId,
                                    "userId" to userId,
                                    "eventTitle" to eventName,
                                    "eventDate" to eventDate,
                                    "price" to price,
                                    "status" to "confirmed",
                                    "ticketNumber" to ticketNumber,
                                    "bookingDate" to System.currentTimeMillis()
                                )

                                db.collection("bookings")
                                    .document(bookingId)
                                    .set(bookingData)
                                    .addOnSuccessListener {
                                        scheduleReminder(eventName, eventDate)
                                        viewModel.fetchUserTickets()
                                        showSuccessDialog = false
                                        isSaving = false
                                        onPaymentSuccess()
                                    }
                                    .addOnFailureListener { e ->
                                        isSaving = false
                                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                            } else {
                                isSaving = false
                                Toast.makeText(context, "User not found!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD35400))
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Text("OK", color = Color.White)
                    }
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Payment", fontWeight = FontWeight.Bold) },
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Checkout", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)

            Spacer(modifier = Modifier.height(24.dp))


            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(4.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Event Name", fontSize = 14.sp, color = Color.Gray)
                    Text(eventName, fontSize = 18.sp, fontWeight = FontWeight.Bold)

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Total Amount", fontSize = 14.sp, color = Color.Gray)
                    Text(
                        text = "$price ETB",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFD35400)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Select Payment Method",
                modifier = Modifier.fillMaxWidth(),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(8.dp))


            val methods = listOf("Telebirr", "Chapa (CBE/Abyssinia)", "BOA MPesa")
            methods.forEach { method ->
                Surface(
                    onClick = { selectedMethod = method },
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                    color = if (selectedMethod == method) Color(0xFFFFF3E0) else Color.Transparent,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (selectedMethod == method) Color(0xFFD35400) else Color.LightGray
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedMethod == method,
                            onClick = { selectedMethod = method },
                            colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFD35400))
                        )
                        Text(text = method, fontSize = 16.sp, modifier = Modifier.padding(start = 12.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))


            Button(
                onClick = { showSuccessDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD35400))
            ) {
                Text(text = "Pay Now", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}