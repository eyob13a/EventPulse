package com.org.debrebirhan.eventpulse.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.org.debrebirhan.eventpulse.data.Event
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AdminViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    // ---------------- USERS STATE ----------------
    private val _users = MutableStateFlow<List<Map<String, Any>>>(emptyList())
    val users: StateFlow<List<Map<String, Any>>> = _users

    // ---------------- PENDING EVENTS STATE ----------------
    private val _pendingEvents = MutableStateFlow<List<Event>>(emptyList())
    val pendingEvents: StateFlow<List<Event>> = _pendingEvents

    // ---------------- ALL APPROVED EVENTS STATE (For CRUD) ----------------
    private val _allApprovedEvents = MutableStateFlow<List<Event>>(emptyList())
    val allApprovedEvents: StateFlow<List<Event>> = _allApprovedEvents

    // ---------------- STATISTICS STATES (Real-time Counts) ----------------
    private val _totalEventsCount = MutableStateFlow(0)
    val totalEventsCount: StateFlow<Int> = _totalEventsCount

    private val _totalUsersCount = MutableStateFlow(0)
    val totalUsersCount: StateFlow<Int> = _totalUsersCount

    private val _pendingApprovalsCount = MutableStateFlow(0)
    val pendingApprovalsCount: StateFlow<Int> = _pendingApprovalsCount

    // ---------------- FETCH STATS (Real-time Database Count) ----------------
    fun fetchStats() {
        // 1. የጸደቁ ኢቨንቶች ብዛት (Total Approved Events)
        db.collection("events")
            .whereEqualTo("status", "approved")
            .addSnapshotListener { snapshot, error ->
                if (error == null) {
                    _totalEventsCount.value = snapshot?.size() ?: 0
                }
            }

        // 2. በመጠባበቅ ላይ ያሉ ጥያቄዎች (Pending Approvals)
        db.collection("events")
            .whereEqualTo("status", "pending")
            .addSnapshotListener { snapshot, error ->
                if (error == null) {
                    _pendingApprovalsCount.value = snapshot?.size() ?: 0
                }
            }

        // 3. ጠቅላላ የተጠቃሚዎች ብዛት (Total Users from Database)
        db.collection("users")
            .addSnapshotListener { snapshot, error ->
                if (error == null) {
                    _totalUsersCount.value = snapshot?.size() ?: 0
                }
            }
    }

    // ---------------- FETCH PENDING EVENTS LIST ----------------
    fun fetchPendingEvents() {
        db.collection("events")
            .whereEqualTo("status", "pending")
            .get()
            .addOnSuccessListener { snapshot ->
                _pendingEvents.value = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Event::class.java)?.copy(id = doc.id)
                }
            }
            .addOnFailureListener {
                _pendingEvents.value = emptyList()
            }
    }

    // ---------------- FETCH ALL APPROVED EVENTS (For All Events Screen) ----------------
    fun fetchAllApprovedEvents() {
        db.collection("events")
            .whereEqualTo("status", "approved")
            .orderBy("date", Query.Direction.DESCENDING) // በቅርብ ቀን ያሉትን ለማስቀደም
            .addSnapshotListener { snapshot, error ->
                if (error == null) {
                    _allApprovedEvents.value = snapshot?.documents?.mapNotNull { doc ->
                        doc.toObject(Event::class.java)?.copy(id = doc.id)
                    } ?: emptyList()
                }
            }
    }

    // ---------------- UPDATE EVENT STATUS (Approve/Reject) ----------------
    fun updateEventStatus(eventId: String, newStatus: String, onSuccess: () -> Unit) {
        if (eventId.isEmpty()) return

        db.collection("events").document(eventId)
            .update("status", newStatus)
            .addOnSuccessListener {
                // ቁጥሮቹ በ SnapshotListener አማካኝነት በራሳቸው ይዘመናሉ
                fetchPendingEvents()
                onSuccess()
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
                fetchAllUsers()
                onSuccess()
            }
    }

    // ---------------- DELETE EVENT (CRUD Action) ----------------
    fun deleteEvent(eventId: String, onSuccess: () -> Unit = {}) {
        db.collection("events").document(eventId)
            .delete()
            .addOnSuccessListener {
                // ዝርዝሮቹን ማደስ
                fetchPendingEvents()
                fetchAllApprovedEvents()
                onSuccess()
            }
    }
}