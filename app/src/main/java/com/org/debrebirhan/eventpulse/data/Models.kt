package com.org.debrebirhan.eventpulse.data

import com.google.firebase.firestore.DocumentId

data class User(
    @DocumentId val userId: String = "",
    val fullName: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val profileImageUrl: String = "",
    val userType: String = "Guest"
)

data class Booking(
    @DocumentId val bookingId: String = "",
    val eventId: String = "",
    val userId: String = "",
    val bookingDate: Long = System.currentTimeMillis(),
    val status: String = "Pending"
)