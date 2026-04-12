package com.blueray.marasy.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.blueray.marasy.databinding.NotificationsItemBinding
import com.blueray.marasy.interfaces.NotificationListener
import com.blueray.marasy.model.NotificationData

class NotificationsAdapter(
    val listener: NotificationListener
) : ListAdapter<NotificationData, NotificationsAdapter.NotificationViewHolder>(DiffCallback) {

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<NotificationData>() {
            override fun areItemsTheSame(
                oldItem: NotificationData,
                newItem: NotificationData
            ): Boolean {
                return oldItem.notification_id == newItem.notification_id
            }

            override fun areContentsTheSame(
                oldItem: NotificationData,
                newItem: NotificationData
            ): Boolean {
                return oldItem == newItem
            }
        }
    }

    inner class NotificationViewHolder(val binding: NotificationsItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val binding =
            NotificationsItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NotificationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val data = getItem(position)

        holder.binding.apply {
            title.text = data.title
//            body.text = data.body
            holder.itemView.setOnClickListener {
                if (data.product_id != null) {
                    listener.onNotificationClick(data.product_id, "product")
                } else if (data.category_id != null) {
                    listener.onNotificationClick(data.category_id, "category")
                } else if (data.brand_id != null) {
                    listener.onNotificationClick(data.brand_id, "brand")
                }
            }

        }
    }
}