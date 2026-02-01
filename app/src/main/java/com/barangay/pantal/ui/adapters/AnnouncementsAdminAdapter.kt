package com.barangay.pantal.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.barangay.pantal.R
import com.barangay.pantal.model.Announcement

class AnnouncementsAdminAdapter(private val onDeleteClick: (Announcement) -> Unit) : ListAdapter<Announcement, AnnouncementsAdminAdapter.AnnouncementAdminViewHolder>(AnnouncementAdminDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnnouncementAdminViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_announcement_admin, parent, false)
        return AnnouncementAdminViewHolder(view, onDeleteClick)
    }

    override fun onBindViewHolder(holder: AnnouncementAdminViewHolder, position: Int) {
        val announcement = getItem(position)
        holder.bind(announcement)
    }

    class AnnouncementAdminViewHolder(itemView: View, private val onDeleteClick: (Announcement) -> Unit) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.announcementTitle)
        private val contentTextView: TextView = itemView.findViewById(R.id.announcementContent)
        private val deleteButton: Button = itemView.findViewById(R.id.deleteAnnouncementButton)

        fun bind(announcement: Announcement) {
            titleTextView.text = announcement.title
            contentTextView.text = announcement.content
            deleteButton.setOnClickListener { onDeleteClick(announcement) }
        }
    }
}

class AnnouncementAdminDiffCallback : DiffUtil.ItemCallback<Announcement>() {
    override fun areItemsTheSame(oldItem: Announcement, newItem: Announcement): Boolean {
        return oldItem.timestamp == newItem.timestamp
    }

    override fun areContentsTheSame(oldItem: Announcement, newItem: Announcement): Boolean {
        return oldItem == newItem
    }
}
