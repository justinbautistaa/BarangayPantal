package com.barangay.pantal.ui.adapters.staff

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.barangay.pantal.R
import com.barangay.pantal.databinding.ItemStaffAnnouncementBinding
import com.barangay.pantal.model.Announcement
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions

class StaffAnnouncementAdapter(options: FirebaseRecyclerOptions<Announcement>) : FirebaseRecyclerAdapter<Announcement, StaffAnnouncementAdapter.AnnouncementViewHolder>(options) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnnouncementViewHolder {
        val binding = ItemStaffAnnouncementBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AnnouncementViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AnnouncementViewHolder, position: Int, model: Announcement) {
        holder.bind(model)
    }

    inner class AnnouncementViewHolder(private val binding: ItemStaffAnnouncementBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(announcement: Announcement) {
            binding.announcementTitle.text = announcement.title
            binding.announcementContent.text = announcement.content
            binding.announcementDate.text = announcement.date
            binding.priorityText.text = announcement.priority

            val priorityColor = when (announcement.priority) {
                "high" -> R.drawable.status_high
                "medium" -> R.drawable.status_medium
                else -> R.drawable.status_low
            }
            binding.priorityLayout.setBackgroundResource(priorityColor)
        }
    }
}
