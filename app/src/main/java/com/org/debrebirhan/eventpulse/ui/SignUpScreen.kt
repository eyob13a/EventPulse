package com.org.debrebirhan.eventpulse.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.org.debrebirhan.eventpulse.viewmodel.AuthViewModel

@Composable
fun SignUpScreen(viewModel: AuthViewModel, onNavigateBack: () -> Unit) {
    var fullName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("+251") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    // --- አዳዲስ የሮል መለያዎች ---
    var selectedRole by remember { mutableStateOf("Attendee") } // Default Attendee ነው
    var orgName by remember { mutableStateOf("") }
    var orgPhone by remember { mutableStateOf("") }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val eventPulseOrange = Color(0xFFD35400)
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp)
            .verticalScroll(scrollState), // ለሞባይል ስክሪን እንዲመች scrolling ጨምሬበታለሁ
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Create Your Account",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = eventPulseOrange,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Full Name Field
        TextField(
            value = fullName,
            onValueChange = { fullName = it },
            label = { Text("Full Name") },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFFDF2E9),
                unfocusedContainerColor = Color(0xFFF2F2F2),
                focusedIndicatorColor = eventPulseOrange,
                focusedLabelColor = eventPulseOrange
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Email Field
        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFFDF2E9),
                unfocusedContainerColor = Color(0xFFF2F2F2),
                focusedIndicatorColor = eventPulseOrange,
                focusedLabelColor = eventPulseOrange
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // --- Role Selection Section ---
        Text(
            text = "Register as:",
            modifier = Modifier.align(Alignment.Start),
            fontWeight = FontWeight.Medium,
            color = Color.Gray
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = selectedRole == "Attendee",
                onClick = { selectedRole = "Attendee" },
                colors = RadioButtonDefaults.colors(selectedColor = eventPulseOrange)
            )
            Text("Attendee")
            Spacer(modifier = Modifier.width(16.dp))
            RadioButton(
                selected = selectedRole == "Organizer",
                onClick = { selectedRole = "Organizer" },
                colors = RadioButtonDefaults.colors(selectedColor = eventPulseOrange)
            )
            Text("Organizer")
        }

        // --- ለአዘጋጅ (Organizer) ብቻ የሚታይ ተጨማሪ ፎርም ---
        if (selectedRole == "Organizer") {
            Spacer(modifier = Modifier.height(12.dp))
            TextField(
                value = orgName,
                onValueChange = { orgName = it },
                label = { Text("Organization Name") },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFFDF2E9),
                    unfocusedContainerColor = Color(0xFFF2F2F2),
                    focusedIndicatorColor = eventPulseOrange
                )
            )
            Spacer(modifier = Modifier.height(12.dp))
            TextField(
                value = orgPhone,
                onValueChange = { orgPhone = it },
                label = { Text("Organization Phone") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFFDF2E9),
                    unfocusedContainerColor = Color(0xFFF2F2F2),
                    focusedIndicatorColor = eventPulseOrange
                )
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Password Field
        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFFDF2E9),
                unfocusedContainerColor = Color(0xFFF2F2F2),
                focusedIndicatorColor = eventPulseOrange
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Confirm Password Field
        TextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirm Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFFDF2E9),
                unfocusedContainerColor = Color(0xFFF2F2F2),
                focusedIndicatorColor = eventPulseOrange
            )
        )

        if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = Color.Red,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        if (isLoading) {
            CircularProgressIndicator(color = eventPulseOrange)
        } else {
            Button(
                onClick = {
                    if (password != confirmPassword) {
                        errorMessage = "Passwords do not match!"
                    } else if (fullName.isEmpty() || email.isEmpty()) {
                        errorMessage = "Please fill all required fields"
                    } else if (selectedRole == "Organizer" && (orgName.isEmpty() || orgPhone.isEmpty())) {
                        errorMessage = "Please fill Organization details"
                    } else {
                        isLoading = true
                        // አዲሱን ሎጂክ ወደ ViewModel መላክ (የሚቀጥለው ፋይላችን ይሆናል)
                        viewModel.signUp(
                            fullName,
                            phoneNumber,
                            email,
                            password,
                            selectedRole,
                            orgName,
                            orgPhone
                        ) { success, error ->
                            isLoading = false
                            if (success) onNavigateBack() else errorMessage = error ?: "Registration Failed"
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = eventPulseOrange)
            ) {
                Text("Register", fontSize = 18.sp, fontWeight = FontWeight.Medium, color = Color.White)
            }

            TextButton(onClick = onNavigateBack, modifier = Modifier.padding(top = 8.dp)) {
                Text("Already have an account? Login", color = eventPulseOrange)
            }
        }
    }
}