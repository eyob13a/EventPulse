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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    eventName: String,
    price: String,
    onBack: () -> Unit,
    onPaymentSuccess: () -> Unit
) {
    val context = LocalContext.current
    var selectedMethod by remember { mutableStateOf("Telebirr") }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }

    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Payment Successful!") },
            text = { Text("You have successfully booked your ticket for $eventName. Enjoy the event!") },
            confirmButton = {
                Button(
                    onClick = {

                        if (!isSaving) {
                            isSaving = true
                            val userId = auth.currentUser?.uid

                            if (userId != null) {
                                val ticketData = hashMapOf(
                                    "userId" to userId,
                                    "eventName" to eventName,
                                    "price" to price,
                                    "purchaseDate" to com.google.firebase.Timestamp.now()
                                )

                                db.collection("tickets")
                                    .add(ticketData)
                                    .addOnSuccessListener {
                                        showSuccessDialog = false
                                        onPaymentSuccess()
                                    }
                                    .addOnFailureListener { e ->
                                        isSaving = false
                                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD35400))
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
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
                title = { Text("Payment") },
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Checkout", fontSize = 24.sp, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(20.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFDEBD0))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Event: $eventName")
                    Text(
                        text = "Total Price: $price ETB",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD35400)
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            Text(text = "Select Payment Method", modifier = Modifier.fillMaxWidth(), fontWeight = FontWeight.SemiBold)

            listOf("Telebirr", "Chapa (CBE/Abyssinia)", "BOA MPesa").forEach { method ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = selectedMethod == method, onClick = { selectedMethod = method })
                    Text(text = method, modifier = Modifier.padding(start = 8.dp))
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { showSuccessDialog = true },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD35400))
            ) {
                Text(text = "Confirm Payment", color = Color.White, fontSize = 18.sp)
            }
        }
    }
}