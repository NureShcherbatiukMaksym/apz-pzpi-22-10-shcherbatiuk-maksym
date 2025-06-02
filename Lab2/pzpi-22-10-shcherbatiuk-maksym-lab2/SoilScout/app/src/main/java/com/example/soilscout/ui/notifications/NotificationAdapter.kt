package com.example.soilscout.ui.notifications

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.soilscout.R
import com.example.soilscout.model.Notification
import java.text.SimpleDateFormat
import java.util.*

class NotificationAdapter : ListAdapter<Notification, NotificationAdapter.NotificationViewHolder>(NotificationDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_notification, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val notification = getItem(position)
        holder.bind(notification)
    }

    class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageTextView: TextView = itemView.findViewById(R.id.notification_message)
        private val pointInfoTextView: TextView = itemView.findViewById(R.id.notification_point_info)
        private val timestampTextView: TextView = itemView.findViewById(R.id.notification_timestamp)
        private val statusTextView: TextView = itemView.findViewById(R.id.notification_status)

        fun bind(notification: Notification) {
            messageTextView.text = notification.message
            pointInfoTextView.text = notification.pointInfo
            statusTextView.text = notification.status

            val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault())
            timestampTextView.text = sdf.format(Date(notification.timestamp))
        }
    }

    private class NotificationDiffCallback : DiffUtil.ItemCallback<Notification>() {
        override fun areItemsTheSame(oldItem: Notification, newItem: Notification): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Notification, newItem: Notification): Boolean {
            return oldItem == newItem
        }
    }
}