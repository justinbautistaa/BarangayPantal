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
import com.barangay.pantal.model.RequestAdmin

class RequestsAdminAdapter(
    private val onApproveClick: (RequestAdmin) -> Unit,
    private val onRejectClick: (RequestAdmin) -> Unit
) : ListAdapter<RequestAdmin, RequestsAdminAdapter.RequestAdminViewHolder>(RequestAdminDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestAdminViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_request_admin, parent, false)
        return RequestAdminViewHolder(view, onApproveClick, onRejectClick)
    }

    override fun onBindViewHolder(holder: RequestAdminViewHolder, position: Int) {
        val request = getItem(position)
        holder.bind(request)
    }

    class RequestAdminViewHolder(
        itemView: View,
        private val onApproveClick: (RequestAdmin) -> Unit,
        private val onRejectClick: (RequestAdmin) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val requestTypeTextView: TextView = itemView.findViewById(R.id.tvRequestType)
        private val requestStatusTextView: TextView = itemView.findViewById(R.id.tvRequestStatus)
        private val requesterNameTextView: TextView = itemView.findViewById(R.id.tvRequesterName)
        private val approveButton: Button = itemView.findViewById(R.id.btnApprove)
        private val rejectButton: Button = itemView.findViewById(R.id.btnReject)

        fun bind(request: RequestAdmin) {
            // Assuming RequestAdmin has properties like 'serviceName', 'status', and 'userName'
            // You might need to adjust these based on your actual RequestAdmin model
            requesterNameTextView.text = request.userId
            requestTypeTextView.text = request.serviceName
            requestStatusTextView.text = request.status
            approveButton.setOnClickListener { onApproveClick(request) }
            rejectButton.setOnClickListener { onRejectClick(request) }
        }
    }
}

class RequestAdminDiffCallback : DiffUtil.ItemCallback<RequestAdmin>() {
    override fun areItemsTheSame(oldItem: RequestAdmin, newItem: RequestAdmin): Boolean {
        return oldItem.key == newItem.key
    }

    override fun areContentsTheSame(oldItem: RequestAdmin, newItem: RequestAdmin): Boolean {
        return oldItem == newItem
    }
}
