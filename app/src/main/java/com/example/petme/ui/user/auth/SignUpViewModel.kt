package com.example.petme.ui.user.auth

import androidx.lifecycle.ViewModel
import com.example.petme.models.UserAccount
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class SignUpViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    fun signUpUser(
        email: String, password: String, accountType: String,
        phoneNumber: String, address: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit
    ) {
        // Create user in Firebase Auth
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = task.result?.user?.uid
                    userId?.let {
                        saveUserToFirestore(it, email, phoneNumber, address, accountType, onSuccess, onFailure)
                    }
                } else {
                    task.exception?.let {
                        onFailure(it)
                    }
                }
            }
    }

    private fun saveUserToFirestore(
        userId: String, email: String, phoneNumber: String,
        address: String, accountType: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit
    ) {
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val userAccount = UserAccount(
            userId = userId,
            email = email,
            phoneNumber = phoneNumber,
            address = address,
            accountType = accountType,
            dateOfCreation = currentDate
        )

        // Save user data in Firestore under a "users" collection
        firestore.collection("users").document(userId).set(userAccount)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }
}
