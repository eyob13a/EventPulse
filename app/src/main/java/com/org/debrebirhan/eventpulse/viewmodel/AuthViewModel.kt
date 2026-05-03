package com.org.debrebirhan.eventpulse.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Base64
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class AuthViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val client = OkHttpClient()


    private val _userData = MutableStateFlow<Map<String, Any>?>(null)
    val userData: StateFlow<Map<String, Any>?> = _userData.asStateFlow()

    val currentUserId: String?
        get() = auth.currentUser?.uid

    val currentUserEmail: String?
        get() = auth.currentUser?.email

    init {

        fetchUserProfile()
    }

    fun fetchUserProfile() {
        val uid = currentUserId
        if (uid != null) {
            db.collection("users").document(uid).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        _userData.value = document.data
                    }
                }
                .addOnFailureListener {
                    _userData.value = null
                }
        }
    }

    fun uploadImage(context: Context, imageUri: Uri, onResult: (String?) -> Unit) {
        val apiKey = "027481396146c396e600c5741563cd72"
        try {
            val inputStream = context.contentResolver.openInputStream(imageUri)
            val bytes = inputStream?.readBytes()
            inputStream?.close()

            if (bytes == null) {
                onResult(null)
                return
            }

            val base64Image = Base64.encodeToString(bytes, Base64.DEFAULT)

            val formBody = FormBody.Builder()
                .add("key", apiKey)
                .add("image", base64Image)
                .build()

            val request = Request.Builder()
                .url("https://api.imgbb.com/1/upload")
                .post(formBody)
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    onResult(null)
                }

                override fun onResponse(call: Call, response: Response) {
                    response.use {
                        if (!response.isSuccessful) {
                            onResult(null)
                        } else {
                            val responseData = response.body?.string()
                            val json = JSONObject(responseData ?: "")
                            val imageUrl = json.getJSONObject("data").getString("url")
                            onResult(imageUrl)
                        }
                    }
                }
            })
        } catch (e: Exception) {
            onResult(null)
        }
    }

    fun signUp(fullName: String, phoneNumber: String, email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid ?: ""
                    val userMap = hashMapOf(
                        "uid" to uid,
                        "fullName" to fullName,
                        "phoneNumber" to phoneNumber,
                        "email" to email
                    )
                    db.collection("users").document(uid).set(userMap)
                        .addOnSuccessListener {
                            fetchUserProfile()
                            onResult(true, null)
                        }
                        .addOnFailureListener { e -> onResult(false, e.message) }
                } else {
                    onResult(false, task.exception?.localizedMessage ?: "Registration failed")
                }
            }
    }

    fun signIn(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    fetchUserProfile()
                    onResult(true, null)
                }
                else onResult(false, task.exception?.localizedMessage ?: "Login failed")
            }
    }

    fun isUserLoggedIn(): Boolean = auth.currentUser != null

    fun logout() {
        auth.signOut()
        _userData.value = null
    }
}