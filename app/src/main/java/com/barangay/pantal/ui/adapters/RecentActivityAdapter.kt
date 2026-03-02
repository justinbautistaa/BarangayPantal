package com.barangay.pantal.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.barangay.pantal.databinding.ItemRecentActivityBinding
import com.barangay.pantal.model.RecentActivity

class RecentActivityAdapter(private val context: Context, private val recentActivity: List<RecentActivity>) :
    RecyclerView.Adapter<RecentActivityAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRecentActivityBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val activity = recentActivity[position]
        holder.bind(activity)
    }

    override fun getItemCount(): Int = recentActivity.size

    inner class ViewHolder(private val binding: ItemRecentActivityBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(activity: RecentActivity) {
            val activityText = "${activity.title} by ${activity.user} on ${activity.timestamp}"
            binding.activityText.text = activityText
        }
    }
}
