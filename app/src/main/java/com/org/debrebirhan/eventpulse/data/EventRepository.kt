package com.org.debrebirhan.eventpulse.data

import com.google.firebase.firestore.FirebaseFirestore

class EventRepository {
    private val db = FirebaseFirestore.getInstance()

    fun getAllEvents(onResult: (List<Event>) -> Unit) {

        db.collection("events").addSnapshotListener { querySnapshot, _ ->
            val events = querySnapshot?.toObjects(Event::class.java) ?: emptyList()
            onResult(events)
        }
    }

    fun addEvent(event: Event, onComplete: (Boolean) -> Unit) {
        val documentRef = db.collection("events").document()
        val finalEvent = event.copy(id = documentRef.id)

        documentRef.set(finalEvent)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }
}