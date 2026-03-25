package com.barangay.pantal.ui.adapters.common

import android.app.AlertDialog
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.barangay.pantal.R
import com.barangay.pantal.model.Announcement
import com.barangay.pantal.ui.activities.admin.AddOrEditAnnouncementActivity

class AnnouncementsAdapter(
    private val canManage: Boolean,
    private var announcements: List<Announcement>,
    private val onDeleteClick: (Announcement) -> Unit
) : RecyclerView.Adapter<AnnouncementsAdapter.AnnouncementViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnnouncementViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_announcement, parent, false)
        return AnnouncementViewHolder(view)
    }

    override fun onBindViewHolder(holder: AnnouncementViewHolder, position: Int) {
        holder.bind(announcements[position], canManage)
    }

    override fun getItemCount(): Int = announcements.size

    fun updateData(newAnnouncements: List<Announcement>) {
        this.announcements = newAnnouncements
        notifyDataSetChanged()
    }

    inner class AnnouncementViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        private val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        private val tvContent: TextView = itemView.findViewById(R.id.tvContent)
        private val tvPriority: TextView = itemView.findViewById(R.id.tvPriority)
        private val adminButtons: LinearLayout = itemView.findViewById(R.id.admin_buttons)
        private val btnEdit: Button = itemView.findViewById(R.id.btnEdit)
        private val btnDelete: Button = itemView.findViewById(R.id.btnDelete)

        fun bind(announcement: Announcement, canManage: Boolean) {
            tvTitle.text = announcement.title
            tvDate.text = announcement.date
            tvContent.text = announcement.content
            tvPriority.text = announcement.priority

            val priorityColor = when (announcement.priority.lowercase()) {
                "high" -> R.color.priority_high
                "medium" -> R.color.priority_medium
                else -> R.color.gray_light
            }
            tvPriority.background.setTint(ContextCompat.getColor(itemView.context, priorityColor))

            if (canManage) {
                adminButtons.visibility = View.VISIBLE
                btnEdit.setOnClickListener {
                    val intent = Intent(itemView.context, AddOrEditAnnouncementActivity::class.java)
                    intent.putExtra("announcement_id", announcement.id)
                    itemView.context.startActivity(intent)
                }

                btnDelete.setOnClickListener {
                    AlertDialog.Builder(itemView.context)
                        .setTitle("Delete Announcement")
                        .setMessage("Are you sure you want to delete this announcement?")
                        .setPositiveButton("Delete") { _, _ ->
                            onDeleteClick(announcement)
                        }
                        .setNegativeButton("Cancel", null)
                        .show()
                }
            } else {
                adminButtons.visibility = View.GONE
            }
        }
    }
}
