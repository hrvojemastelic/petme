package com.example.petme.notifications

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.petme.R
import com.example.petme.adapters.NotificationAdapter
import com.example.petme.models.NotificationModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class NotificationsActivity : AppCompatActivity() {

    private val notifications = mutableListOf<NotificationModel>()
    private lateinit var notificationAdapter: NotificationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications)

        // Set up RecyclerView
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        notificationAdapter = NotificationAdapter(notifications)
        recyclerView.adapter = notificationAdapter

        // Load notifications from local storage
        loadNotifications()
    }

    private fun loadNotifications() {
        val sharedPreferences = getSharedPreferences("notifications", MODE_PRIVATE)
        val gson = Gson()
        val type = object : TypeToken<MutableList<NotificationModel>>() {}.type

        val notificationsJson = sharedPreferences.getString("notification_list", "[]")
        val savedNotifications: MutableList<NotificationModel> = gson.fromJson(notificationsJson, type)

        notifications.clear()
        notifications.addAll(savedNotifications)
        notificationAdapter.notifyDataSetChanged()
    }
}
