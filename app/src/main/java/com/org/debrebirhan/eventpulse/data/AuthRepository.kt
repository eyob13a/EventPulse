package com.org.debrebirhan.eventpulse.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    fun getCurrentUser(): FirebaseUser? = auth.currentUser

    fun signUp(
        name: String,
        phone: String,
        email: String,
        pass: String,
        role: String,
        orgName: String? = null,
        orgPhone: String? = null,
        onResult: (Boolean, String?) -> Unit
    ) {

        auth.createUserWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->

                if (task.isSuccessful) {

                    val userId = auth.currentUser?.uid

                    val userMap = mutableMapOf<String, Any>(
                        "uid" to (userId ?: ""),
                        "fullName" to name,
                        "phone" to phone,
                        "email" to email,
                        "role" to role,
                        "isApproved" to (role == "Attendee"),
                        "createdAt" to System.currentTimeMillis()
                    )

                    if (role == "Organizer") {
                        userMap["orgName"] = orgName ?: ""
                        userMap["orgPhone"] = orgPhone ?: ""
                    }

                    userId?.let { id ->

                        db.collection("users")
                            .document(id)
                            .set(userMap)
                            .addOnSuccessListener {

                                onResult(true, null)
                            }
                            .addOnFailureListener { e ->

                                onResult(false, e.message)
                            }
                    }

                } else {

                    onResult(false, task.exception?.message)
                }
            }
    }

    fun signIn(
        email: String,
        password: String,
        onResult: (Boolean, String?) -> Unit
    ) {

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->

                if (task.isSuccessful) {

                    onResult(true, null)

                } else {

                    onResult(false, task.exception?.message)
                }
            }
    }

    fun signOut() {
        auth.signOut()
    }
}