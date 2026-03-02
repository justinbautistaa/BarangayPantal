package com.barangay.pantal.ui.adapters.staff

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.barangay.pantal.R
import com.barangay.pantal.databinding.ItemActivityLogBinding
import com.barangay.pantal.model.FirebaseActivityLog
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ActivityLogAdapter(private val activities: List<FirebaseActivityLog>) : RecyclerView.Adapter<ActivityLogAdapter.ActivityLogViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActivityLogViewHolder {
        val binding = ItemActivityLogBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ActivityLogViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ActivityLogViewHolder, position: Int) {
        holder.bind(activities[position])
    }

    override fun getItemCount() = activities.size

    inner class ActivityLogViewHolder(private val binding: ItemActivityLogBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(activity: FirebaseActivityLog) {
            binding.activityTitle.text = activity.title
            binding.activityDescription.text = activity.description

            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            val date = Date(activity.timestamp)
            binding.activityDetails.text = "${activity.user} • ${sdf.format(date)}"

            val iconRes = when (activity.title) {
                "Completed Request" -> R.drawable.ic_check
                "Processing Request" -> R.drawable.ic_processing
                "Added Announcement" -> R.drawable.ic_announcement
                "Updated Resident" -> R.drawable.ic_edit
                "Added New Resident" -> R.drawable.ic_person
                else -> R.drawable.ic_dot
            }
            binding.activityIcon.setImageResource(iconRes)
        }
    }
}
