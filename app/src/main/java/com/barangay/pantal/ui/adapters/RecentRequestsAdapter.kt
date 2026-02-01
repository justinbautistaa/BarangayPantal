package com.barangay.pantal.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.barangay.pantal.R
import com.barangay.pantal.model.Request

class RecentRequestsAdapter(private val context: Context, private val recentRequests: List<Request>) :
    RecyclerView.Adapter<RecentRequestsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recent_request, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val request = recentRequests[position]
        holder.bind(request)
    }

    override fun getItemCount(): Int = recentRequests.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tvName)
        private val tvRequestType: TextView = itemView.findViewById(R.id.tvRequestType)
        private val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)

        fun bind(request: Request) {
            tvName.text = request.name
            tvRequestType.text = request.type
            tvDate.text = request.date
            tvStatus.text = request.status
        }
    }
}