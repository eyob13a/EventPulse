package com.org.debrebirhan.eventpulse.data

// ---------------- USER ----------------
data class User(
    val userId: String = "",
    val fullName: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val profileImageUrl: String = "",
    val role: String = "Attendee" // "Admin", "Organizer", "Attendee"
)

// ---------------- EVENT ----------------
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
    val status: String = "pending" // "pending", "approved", "rejected"
)

// ---------------- BOOKING / TICKET ----------------
data class Booking(
    val bookingId: String = "",
    val eventId: String = "",
    val userId: String = "",
    val eventTitle: String = "",
    val eventDate: String = "",      // ለቲኬት ዲዛይን ስለሚያስፈልግ
    val eventLocation: String = "",  // ለቲኬት ዲዛይን ስለሚያስፈልግ
    val price: String = "0",
    val bookingDate: Long = System.currentTimeMillis(),
    val status: String = "pending",  // "pending", "confirmed", "failed"
    val paymentId: String = "",      // ከ Chapa ወይም ሌላ የክፍያ ተቋም የሚመጣ
    val ticketNumber: String = ""    // ለምሳሌ "EP-12345" (ቲኬት ላይ የሚታይ)
)