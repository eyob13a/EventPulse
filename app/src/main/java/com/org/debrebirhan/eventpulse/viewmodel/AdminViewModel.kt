package com.org.debrebirhan.eventpulse.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.org.debrebirhan.eventpulse.data.Event
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AdminViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    // ---------------- USERS ----------------
    private val _users = MutableStateFlow<List<Map<String, Any>>>(emptyList())
    val users: StateFlow<List<Map<String, Any>>> = _users

    // ---------------- PENDING EVENTS ----------------
    private val _pendingEvents = MutableStateFlow<List<Event>>(emptyList())
    val pendingEvents: StateFlow<List<Event>> = _pendingEvents

    // ---------------- STATS ----------------
    private val _totalEventsCount = MutableStateFlow(0)
    val totalEventsCount: StateFlow<Int> = _totalEventsCount

    private val _totalUsersCount = MutableStateFlow(0)
    val totalUsersCount: StateFlow<Int> = _totalUsersCount

    private val _pendingApprovalsCount = MutableStateFlow(0)
    val pendingApprovalsCount: StateFlow<Int> = _pendingApprovalsCount

    private val _reportedIssuesCount = MutableStateFlow(0)
    val reportedIssuesCount: StateFlow<Int> = _reportedIssuesCount

    // ---------------- FETCH STATS ----------------
    fun fetchStats() {
        // የጸደቁ ኢቨንቶች ብዛት
        db.collection("events")
            .whereEqualTo("status", "approved")
            .get()
            .addOnSuccessListener { snapshot ->
                _totalEventsCount.value = snapshot.size()
            }

        // ገና ያልጸደቁ (Pending) ኢቨንቶች ብዛት
        db.collection("events")
            .whereEqualTo("status", "pending")
            .get()
            .addOnSuccessListener { snapshot ->
                _pendingApprovalsCount.value = snapshot.size()
            }

        // ሪፖርቶች
        db.collection("reports")
            .get()
            .addOnSuccessListener { snapshot ->
                _reportedIssuesCount.value = snapshot.size()
            }

        // ተጠቃሚዎች
        db.collection("users")
            .get()
            .addOnSuccessListener { snapshot ->
                _totalUsersCount.value = snapshot.size()
            }
    }

    // ---------------- FETCH PENDING EVENTS ----------------
    fun fetchPendingEvents() {
        db.collection("events")
            .whereEqualTo("status", "pending")
            .get()
            .addOnSuccessListener { snapshot ->
                _pendingEvents.value = snapshot.documents.mapNotNull { doc ->
                    // Document ID ን ወደ Event object ውስጥ መክተት አስፈላጊ ነው
                    doc.toObject(Event::class.java)?.copy(id = doc.id)
                }
            }
            .addOnFailureListener {
                _pendingEvents.value = emptyList()
            }
    }

    // ---------------- UPDATE EVENT STATUS (Approve/Reject) ----------------
    fun updateEventStatus(eventId: String, newStatus: String, onSuccess: () -> Unit) {
        if (eventId.isEmpty()) return

        db.collection("events").document(eventId)
            .update("status", newStatus)
            .addOnSuccessListener {
                // ሁኔታው ሲቀየር ወዲያውኑ ዳሽቦርዱን እና ዝርዝሩን እናድሳለን
                fetchStats()
                fetchPendingEvents()
                onSuccess()
            }
            .addOnFailureListener {
                // ስህተት ካለ እዚህ ጋር መያዝ ይቻላል
            }
    }

    // ---------------- USERS MANAGEMENT ----------------
    fun fetchAllUsers() {
        db.collection("users")
            .get()
            .addOnSuccessListener { snapshot ->
                _users.value = snapshot.documents.mapNotNull { doc ->
                    val data = doc.data?.toMutableMap()
                    data?.put("uid", doc.id)
                    data
                }
            }
    }

    fun deleteUser(uid: String, onSuccess: () -> Unit) {
        db.collection("users").document(uid)
            .delete()
            .addOnSuccessListener {
                fetchStats()
                fetchAllUsers()
                onSuccess()
            }
    }

    // ---------------- DELETE EVENT ----------------
    fun deleteEvent(eventId: String, onSuccess: () -> Unit = {}) {
        db.collection("events").document(eventId)
            .delete()
            .addOnSuccessListener {
                fetchStats()
                fetchPendingEvents()
                onSuccess()
            }
    }
}