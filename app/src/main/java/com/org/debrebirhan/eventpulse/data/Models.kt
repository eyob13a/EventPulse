package com.org.debrebirhan.eventpulse.data

// USER
data class User(
    val userId: String = "",
    val fullName: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val profileImageUrl: String = "",
    val role: String = "Attendee"
)

// EVENT
data class Event(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val date: String = "",
    val time: String = "",
    val location: String = "",
    val organizerId: String = "",
    val category: String = "",
    val imageUrl: String = "",
    val price: String = "0",
    val capacity: String = "0",
    val status: String = "pending"
)

// BOOKING / TICKET
data class Booking(
    val bookingId: String = "",
    val eventId: String = "",
    val userId: String = "",
    val eventTitle: String = "",
    val eventDate: String = "",
    val eventLocation: String = "",
    val price: String = "0",
    val bookingDate: Long = System.currentTimeMillis(),
    val status: String = "pending",
    val paymentId: String = "",
    val ticketNumber: String = ""
)