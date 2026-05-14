package com.org.debrebirhan.eventpulse.viewmodel

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.org.debrebirhan.eventpulse.data.Booking
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class AuthViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val client = OkHttpClient()

    private val _userData = MutableStateFlow<Map<String, Any>?>(null)
    val userData: StateFlow<Map<String, Any>?> = _userData.asStateFlow()

    // የገዛናቸውን ቲኬቶች ዝርዝር ለመያዝ
    private val _userTickets = MutableStateFlow<List<Booking>>(emptyList())
    val userTickets: StateFlow<List<Booking>> = _userTickets.asStateFlow()

    var loginError by mutableStateOf<String?>(null)
        private set

    init {
        // አፑ ሲጀመር ተጠቃሚው ገብቶ ከሆነ ዳታውን ያመጣል
        if (isUserLoggedIn()) {
            fetchUserProfile()
            fetchUserTickets()
        }
    }

    val currentUserId: String?
        get() = auth.currentUser?.uid

    val currentUserEmail: String?
        get() = auth.currentUser?.email

    fun isUserLoggedIn(): Boolean = auth.currentUser != null

    fun clearErrors() {
        loginError = null
    }

    fun fetchUserProfile() {
        val uid = auth.currentUser?.uid ?: return

        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    _userData.value = document.data
                }
            }
            .addOnFailureListener {
                _userData.value = null
            }
    }

    // ተጠቃሚው የገዛቸውን ቲኬቶች ከ Firestore ማምጫ
    fun fetchUserTickets() {
        val uid = auth.currentUser?.uid ?: return

        db.collection("bookings")
            .whereEqualTo("userId", uid)
            .get()
            .addOnSuccessListener { result ->
                // Booking ሞዴልህ ከ Firestore ፊልዶች ጋር መመሳሰሉን እርግጠኛ ሁን
                val tickets = result.mapNotNull { it.toObject(Booking::class.java) }
                _userTickets.value = tickets
            }
            .addOnFailureListener {
                _userTickets.value = emptyList()
            }
    }

    // --- ምስልን ወደ ImgBB ለመጫን (ለ Organizer ዝግጅት ሲፈጥር) ---
    fun uploadImage(context: Context, imageUri: Uri, onResult: (String?) -> Unit) {
        val apiKey = "60b3c6cbf5294deb0a5aea8b1c90a7d0"
        try {
            val inputStream = context.contentResolver.openInputStream(imageUri)
            val bytes = inputStream?.readBytes()
            inputStream?.close()
            if (bytes == null) { onResult(null); return }

            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("key", apiKey)
                .addFormDataPart("image", "event_image_${System.currentTimeMillis()}.jpg",
                    bytes.toRequestBody("image/*".toMediaTypeOrNull(), 0, bytes.size))
                .build()

            val request = Request.Builder().url("https://api.imgbb.com/1/upload").post(requestBody).build()
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) { onResult(null) }
                override fun onResponse(call: Call, response: Response) {
                    response.use {
                        if (!response.isSuccessful) onResult(null)
                        else {
                            val json = JSONObject(response.body?.string() ?: "")
                            onResult(json.getJSONObject("data").getString("url"))
                        }
                    }
                }
            })
        } catch (e: Exception) { onResult(null) }
    }

    fun signUp(
        fullName: String,
        phoneNumber: String,
        email: String,
        password: String,
        role: String,
        orgName: String?,
        orgPhone: String?,
        onResult: (Boolean, String?) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid ?: ""

                    val userMap = mutableMapOf(
                        "uid" to uid,
                        "fullName" to fullName,
                        "phoneNumber" to phoneNumber,
                        "email" to email,
                        "role" to role,
                        "isApproved" to (role == "Attendee")
                    )

                    if (role == "Organizer") {
                        userMap["orgName"] = orgName ?: ""
                        userMap["orgPhone"] = orgPhone ?: ""
                    }

                    db.collection("users").document(uid).set(userMap)
                        .addOnSuccessListener {
                            fetchUserProfile()
                            onResult(true, null)
                        }
                        .addOnFailureListener { e ->
                            onResult(false, e.message)
                        }
                } else {
                    onResult(false, task.exception?.localizedMessage ?: "Registration failed")
                }
            }
    }

    fun signIn(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        loginError = null
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    fetchUserProfile()
                    fetchUserTickets() // መግባት ሲሳካ ቲኬቶችንም አምጣ
                    onResult(true, null)
                } else {
                    val errorMsg = task.exception?.localizedMessage ?: "Login failed"
                    loginError = errorMsg
                    onResult(false, errorMsg)
                }
            }
    }

    fun logout() {
        auth.signOut()
        _userData.value = null
        _userTickets.value = emptyList() // ሲወጣ ቲኬቶችን አጽዳ
    }
}