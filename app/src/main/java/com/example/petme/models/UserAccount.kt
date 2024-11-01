package com.example.petme.models

data class UserAccount(
    val userId: String = "",
    val username: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val address: String = "",
    val accountType: String = "",
    val dateOfCreation: String = ""
)
