package com.barangay.pantal.ui.adapters.staff

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.barangay.pantal.databinding.ItemAnnouncementBinding
import com.barangay.pantal.model.Announcement

class StaffAnnouncementAdapter(private var announcements: List<Announcement>) :
    RecyclerView.Adapter<StaffAnnouncementAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAnnouncementBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(announcements[position])
    }

    override fun getItemCount(): Int = announcements.size

    fun updateData(newAnnouncements: List<Announcement>) {
        this.announcements = newAnnouncements
        notifyDataSetChanged()
    }

    inner class ViewHolder(private val binding: ItemAnnouncementBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(announcement: Announcement) {
            binding.tvTitle.text = announcement.title
            binding.tvDate.text = announcement.date
            binding.tvContent.text = announcement.content
            // Admin buttons are hidden by default in the XML for non-admins
        }
    }
}
