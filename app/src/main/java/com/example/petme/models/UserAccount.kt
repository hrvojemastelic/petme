package com.example.petme.models

data class UserAccount(
    val userId: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val address: String = "",
    val accountType: String = "", // "private" or "business"
    val dateOfCreation: String = "", // Date in String format
)
