package com.barangay.pantal.ui.adapters

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
import com.barangay.pantal.ui.activities.ManageAnnouncementsActivity
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions

class AnnouncementsAdapter(private val isAdmin: Boolean, options: FirebaseRecyclerOptions<Announcement>) :
    FirebaseRecyclerAdapter<Announcement, AnnouncementsAdapter.AnnouncementViewHolder>(options) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnnouncementViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_announcement, parent, false)
        return AnnouncementViewHolder(view)
    }

    override fun onBindViewHolder(holder: AnnouncementViewHolder, position: Int, model: Announcement) {
        holder.bind(model, isAdmin)
    }

    inner class AnnouncementViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        private val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        private val tvContent: TextView = itemView.findViewById(R.id.tvContent)
        private val tvPriority: TextView = itemView.findViewById(R.id.tvPriority)
        private val adminButtons: LinearLayout = itemView.findViewById(R.id.admin_buttons)
        private val btnEdit: Button = itemView.findViewById(R.id.btnEdit)
        private val btnDelete: Button = itemView.findViewById(R.id.btnDelete)

        fun bind(announcement: Announcement, isAdmin: Boolean) {
            tvTitle.text = announcement.title
            tvDate.text = announcement.date
            tvContent.text = announcement.content
            tvPriority.text = announcement.priority

            val priorityColor = when (announcement.priority) {
                "high" -> R.color.priority_high
                "medium" -> R.color.priority_medium
                else -> R.color.gray_light
            }
            tvPriority.background.setTint(ContextCompat.getColor(itemView.context, priorityColor))

            if (isAdmin) {
                adminButtons.visibility = View.VISIBLE
                btnEdit.setOnClickListener {
                    val intent = Intent(itemView.context, ManageAnnouncementsActivity::class.java)
                    intent.putExtra("announcement_id", announcement.timestamp.toString())
                    itemView.context.startActivity(intent)
                }

                btnDelete.setOnClickListener {
                    AlertDialog.Builder(itemView.context)
                        .setTitle("Delete Announcement")
                        .setMessage("Are you sure you want to delete this announcement?")
                        .setPositiveButton("Delete") { _, _ ->
                            val position = adapterPosition
                            if (position != RecyclerView.NO_POSITION) {
                                snapshots.getSnapshot(position).ref.removeValue()
                                    .addOnSuccessListener {
                                        Log.d("AnnouncementsAdapter", "Announcement deleted successfully.")
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e("AnnouncementsAdapter", "Failed to delete announcement.", e)
                                    }
                            }
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