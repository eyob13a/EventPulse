package com.org.debrebirhan.eventpulse.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.org.debrebirhan.eventpulse.data.Event
import com.org.debrebirhan.eventpulse.data.Booking // Booking ሞዴልን እንጠቀማለን

class EventViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    // ---------------- EVENTS ----------------
    private val _events = mutableStateOf<List<Event>>(emptyList())
    val events: State<List<Event>> = _events

    // ---------------- MY TICKETS (Bookings) ----------------
    // 🚩 ዳታውን በ Booking ሞዴል መልክ ብንይዘው ለ UI ይቀላል
    private val _myTickets = mutableStateOf<List<Booking>>(emptyList())
    val myTickets: State<List<Booking>> = _myTickets

    // ---------------- LOADING ----------------
    var isFetching = mutableStateOf(false)
        private set

    // ---------------- SAFE FETCH EVENTS (Approved Only) ----------------
    fun fetchEvents() {
        isFetching.value = true

        db.collection("events")
            .whereEqualTo("status", "approved")
            .get()
            .addOnSuccessListener { result ->
                _events.value = result.documents.mapNotNull { doc ->
                    doc.toObject(Event::class.java)?.copy(id = doc.id)
                }
                isFetching.value = false
            }
            .addOnFailureListener {
                isFetching.value = false
                _events.value = emptyList()
            }
    }

    // ---------------- CATEGORY (Approved Only) ----------------
    fun fetchEventsByCategory(category: String) {
        isFetching.value = true

        db.collection("events")
            .whereEqualTo("category", category)
            .whereEqualTo("status", "approved")
            .get()
            .addOnSuccessListener { result ->
                _events.value = result.documents.mapNotNull { doc ->
                    doc.toObject(Event::class.java)?.copy(id = doc.id)
                }
                isFetching.value = false
            }
            .addOnFailureListener {
                isFetching.value = false
            }
    }

    // ---------------- TICKETS (FETCH FROM BOOKINGS) ----------------
    fun fetchMyTickets(userId: String) {
        if (userId.isEmpty()) return

        // 🚩 ማሳሰቢያ፡ በ PaymentScreen ላይ ዳታውን የምናስቀምጠው "bookings" ውስጥ ስለሆነ
        // እዚህም ከ "bookings" መፈለግ አለበት።
        db.collection("bookings")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { result ->
                _myTickets.value = result.documents.mapNotNull { doc ->
                    doc.toObject(Booking::class.java)
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
}