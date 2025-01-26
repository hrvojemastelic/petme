package com.example.petme.models

import java.util.*

data class NotificationModel(
    val title: String,
    val body: String,
    val timestamp: Date,
    val category: String
)
