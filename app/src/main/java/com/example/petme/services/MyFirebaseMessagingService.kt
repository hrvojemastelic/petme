package com.example.petme.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.petme.R
import com.example.petme.notifications.NotificationsActivity
import com.example.petme.models.NotificationModel
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val title = remoteMessage.data["title"] ?: remoteMessage.notification?.title ?: "Default Title"
        val body = remoteMessage.data["body"] ?: remoteMessage.notification?.body ?: "Default Body"
        val category = remoteMessage.data["category"] ?: ""
        val timestamp = Date()

        Log.d("FirebaseMessage", "Notification Received: title=$title, body=$body, category=$category")

        // Save notification locally
        saveNotificationLocally(NotificationModel(title, body, timestamp, category))

        // Display the notification
        sendNotification(title, body)
    }

    private fun saveNotificationLocally(notification: NotificationModel) {
        val sharedPreferences = getSharedPreferences("notifications", Context.MODE_PRIVATE)
        val gson = Gson()
        val type = object : TypeToken<MutableList<NotificationModel>>() {}.type

        // Get existing notifications
        val notificationsJson = sharedPreferences.getString("notification_list", "[]")
        val notifications: MutableList<NotificationModel> = gson.fromJson(notificationsJson, type)

        // Maintain max size of 20
        if (notifications.size >= 20) {
            notifications.removeAt(0) // Remove oldest
        }
        notifications.add(notification)

        // Save updated list back to SharedPreferences
        sharedPreferences.edit()
            .putString("notification_list", gson.toJson(notifications))
            .apply()
    }

    private fun sendNotification(title: String, messageBody: String) {
        val intent = Intent(this, NotificationsActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(this, "default_channel_id")
            .setSmallIcon(R.drawable.ic_notifications)
            .setContentTitle(title)
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "default_channel_id",
                "Default Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(0, notificationBuilder.build())
    }
}
