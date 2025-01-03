package com.example.petme.session

object UserSession {
    var userId: String? = null
    var username: String? = null
    var email: String? = null
    var phoneNumber: String? = null
    var address: String? = null

    fun clearSession() {
        userId = null
        username = null
        email = null
        phoneNumber = null
        address = null
    }
}
