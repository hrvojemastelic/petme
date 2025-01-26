package com.example.petme.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.petme.R
import com.example.petme.models.NotificationModel
import java.text.SimpleDateFormat
import java.util.Locale

class NotificationAdapter(private val notifications: List<NotificationModel>) :
    RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.notificationTitle)
        val bodyTextView: TextView = itemView.findViewById(R.id.notificationBody)
        val timestampTextView: TextView = itemView.findViewById(R.id.notificationTimestamp)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_notification, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val notification = notifications[position]
        holder.titleTextView.text = notification.title
        holder.bodyTextView.text = notification.body

        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        val formattedDate = sdf.format(notification.timestamp)
        holder.timestampTextView.text = formattedDate
    }

    override fun getItemCount() = notifications.size
}