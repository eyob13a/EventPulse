package com.org.debrebirhan.eventpulse.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.org.debrebirhan.eventpulse.data.Event
import com.org.debrebirhan.eventpulse.data.Booking

class EventViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    // ---------------- EVENTS ----------------
    private val _events = mutableStateOf<List<Event>>(emptyList())
    val events: State<List<Event>> = _events

    // ---------------- MY TICKETS ----------------
    private val _myTickets = mutableStateOf<List<Booking>>(emptyList())
    val myTickets: State<List<Booking>> = _myTickets

    // ---------------- LOADING ----------------
    var isFetching = mutableStateOf(false)
        private set

    // ---------------- FETCH EVENTS ----------------
    fun fetchEvents() {
        isFetching.value = true

        db.collection("events")
            .whereEqualTo("status", "approved")
            .get()
            .addOnSuccessListener { result ->

                _events.value = result.documents.mapNotNull { doc ->
                    doc.toObject(Event::class.java)?.let { event ->
                        event.copy(id = doc.id)
                    }
                }

                isFetching.value = false
            }
            .addOnFailureListener {
                _events.value = emptyList()
                isFetching.value = false
            }
    }

    // ---------------- FETCH BY CATEGORY ----------------
    fun fetchEventsByCategory(category: String) {
        isFetching.value = true

        db.collection("events")
            .whereEqualTo("category", category)
            .whereEqualTo("status", "approved")
            .get()
            .addOnSuccessListener { result ->

                _events.value = result.documents.mapNotNull { doc ->
                    doc.toObject(Event::class.java)?.let { event ->
                        event.copy(id = doc.id)
                    }
                }

                isFetching.value = false
            }
            .addOnFailureListener {
                _events.value = emptyList()
                isFetching.value = false
            }
    }

    // ---------------- FETCH MY TICKETS ----------------
    fun fetchMyTickets(userId: String) {
        if (userId.isBlank()) return

        db.collection("bookings")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { result ->

                _myTickets.value = result.documents.mapNotNull { doc ->
                    doc.toObject(Booking::class.java)?.let { booking ->
                        booking.copy(bookingId = doc.id)
                    }
                }
            }
            .addOnFailureListener {
                _myTickets.value = emptyList()
            }
    }

    // ---------------- ADD EVENT ----------------
    fun addEvent(
        event: Event,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        db.collection("events")
            .add(event)
            .addOnSuccessListener {
                fetchEvents()
                onSuccess()
            }
            .addOnFailureListener { e ->
                onError(e.message ?: "Failed to add event")
            }
    }

    // ---------------- UPDATE EVENT ----------------
    fun updateEvent(
        event: Event,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (event.id.isBlank()) {
            onError("Event ID is missing")
            return
        }

        db.collection("events")
            .document(event.id)
            .set(event)
            .addOnSuccessListener {
                fetchEvents()
                onSuccess()
            }
            .addOnFailureListener { e ->
                onError(e.message ?: "Failed to update event")
            }
    }

    // ---------------- DELETE EVENT ----------------
    fun deleteEvent(
        eventId: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        if (eventId.isBlank()) {
            onError("Event ID is missing")
            return
        }

        db.collection("events")
            .document(eventId)
            .delete()
            .addOnSuccessListener {
                fetchEvents()
                onSuccess()
            }
            .addOnFailureListener { e ->
                onError(e.message ?: "Failed to delete event")
            }
    }

    // ---------------- DELETE TICKET ----------------
    fun deleteTicket(
        bookingId: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        if (bookingId.isBlank()) {
            onError("Booking ID is missing")
            return
        }

        db.collection("bookings")
            .document(bookingId)
            .delete()
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { e ->
                onError(e.message ?: "Failed to delete ticket")
            }
    }

    // ---------------- CREATE BOOKING ----------------
    fun createBooking(
        booking: Booking,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        db.collection("bookings")
            .add(booking)
            .addOnSuccessListener {
                fetchMyTickets(booking.userId)
                onSuccess()
            }
            .addOnFailureListener { e ->
                onError(e.message ?: "Failed to book ticket")
            }
    }
}