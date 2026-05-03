package com.org.debrebirhan.eventpulse.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.org.debrebirhan.eventpulse.data.Event
import com.org.debrebirhan.eventpulse.data.EventRepository

class EventViewModel : ViewModel() {

    private val repository = EventRepository()
    private val db = FirebaseFirestore.getInstance()


    var events = mutableStateOf<List<Event>>(emptyList())
        private set


    private val _myTickets = mutableStateOf<List<Map<String, Any>>>(emptyList())
    val myTickets: State<List<Map<String, Any>>> = _myTickets

    var isFetching = mutableStateOf(false)
        private set

    init {
        fetchEvents()
    }


    fun fetchEvents() {
        isFetching.value = true
        repository.getAllEvents { eventList ->
            events.value = eventList
            isFetching.value = false
        }
    }


    fun fetchEventsByCategory(category: String) {
        isFetching.value = true
        db.collection("events")
            .whereEqualTo("category", category)
            .get()
            .addOnSuccessListener { result ->
                events.value = result.toObjects(Event::class.java)
                isFetching.value = false
            }
            .addOnFailureListener {
                isFetching.value = false
            }
    }


    fun fetchMyTickets(userId: String) {
        db.collection("tickets")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { result ->
                _myTickets.value = result.documents.mapNotNull { it.data }
            }
            .addOnFailureListener {
                _myTickets.value = emptyList()
            }
    }


    fun addEvent(event: Event, onSuccess: () -> Unit, onError: (String) -> Unit) {
        repository.addEvent(event) { success ->
            if (success) {
                fetchEvents()
                onSuccess()
            } else {
                onError("Failed to add event")
            }
        }
    }
}