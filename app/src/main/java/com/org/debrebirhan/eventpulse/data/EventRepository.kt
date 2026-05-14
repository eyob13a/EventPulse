package com.org.debrebirhan.eventpulse.data

import com.google.firebase.firestore.FirebaseFirestore

class EventRepository {

    private val db = FirebaseFirestore.getInstance()

    // ---------------- GET ALL EVENTS (SAFE VERSION) ----------------
    fun getAllEvents(onResult: (List<Event>) -> Unit) {

        db.collection("events")
            .get()
            .addOnSuccessListener { snapshot ->

                val events = snapshot.documents.mapNotNull { doc ->

                    try {
                        Event(
                            id = doc.id,
                            title = doc.getString("title") ?: "",
                            description = doc.getString("description") ?: "",
                            date = doc.getString("date") ?: "",
                            time = doc.getString("time") ?: "",
                            location = doc.getString("location") ?: "",
                            organizerId = doc.getString("organizerId") ?: "",
                            category = doc.getString("category") ?: "",
                            imageUrl = doc.getString("imageUrl") ?: "",
                            price = doc.getString("price") ?: "0",
                            capacity = doc.getString("capacity") ?: "0",
                            status = doc.getString("status") ?: "pending"
                        )
                    } catch (e: Exception) {
                        null
                    }
                }

                onResult(events)
            }
            .addOnFailureListener {
                onResult(emptyList())
            }
    }

    // ---------------- ADD EVENT ----------------
    fun addEvent(event: Event, onComplete: (Boolean) -> Unit) {

        val docRef = if (event.id.isNotEmpty()) {
            db.collection("events").document(event.id)
        } else {
            db.collection("events").document()
        }

        val finalEvent = event.copy(id = docRef.id)

        docRef.set(finalEvent)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    // ---------------- UPDATE EVENT ----------------
    fun updateEvent(event: Event, onComplete: (Boolean) -> Unit) {

        if (event.id.isBlank()) {
            onComplete(false)
            return
        }

        db.collection("events")
            .document(event.id)
            .set(event)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    // ---------------- DELETE EVENT ----------------
    fun deleteEvent(eventId: String, onComplete: (Boolean) -> Unit) {

        db.collection("events")
            .document(eventId)
            .delete()
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }
}