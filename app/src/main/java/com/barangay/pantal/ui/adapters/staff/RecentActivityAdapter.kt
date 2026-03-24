package com.barangay.pantal.ui.adapters.staff

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.barangay.pantal.databinding.ItemRecentActivityBinding

class RecentActivityAdapter(private val activities: List<String>) : RecyclerView.Adapter<RecentActivityAdapter.RecentActivityViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentActivityViewHolder {
        val binding = ItemRecentActivityBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RecentActivityViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecentActivityViewHolder, position: Int) {
        holder.bind(activities[position])
    }

    override fun getItemCount() = activities.size

    inner class RecentActivityViewHolder(private val binding: ItemRecentActivityBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(activity: String) {
            binding.activityText.text = activity
        }
    }
}
