package com.barangay.pantal.ui.adapters.common

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.barangay.pantal.databinding.ItemNotificationBinding
import com.barangay.pantal.ui.activities.common.NotificationUiModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NotificationsAdapter(
    private var items: List<NotificationUiModel>,
    private val onClick: (NotificationUiModel) -> Unit
) : RecyclerView.Adapter<NotificationsAdapter.NotificationViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val binding = ItemNotificationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NotificationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<NotificationUiModel>) {
        items = newItems
        notifyDataSetChanged()
    }

    inner class NotificationViewHolder(private val binding: ItemNotificationBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: NotificationUiModel) {
            binding.titleTextView.text = item.title
            binding.messageTextView.text = item.message
            binding.tagTextView.text = item.accent
            binding.timeTextView.text = SimpleDateFormat("MMM dd, yyyy h:mm a", Locale.getDefault())
                .format(Date(item.timestamp))

            binding.root.setOnClickListener {
                onClick(item)
            }
        }
    }
}
