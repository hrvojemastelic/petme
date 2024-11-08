package com.example.petme.models

import com.google.firebase.Timestamp

data class UserAccount(
    val userId: String = "",
    val username: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val address: String = "",
    val accountType: String = "",
    val dateOfCreation: Timestamp = Timestamp.now()
)
