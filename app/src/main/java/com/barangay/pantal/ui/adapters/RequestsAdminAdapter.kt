package com.barangay.pantal.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.barangay.pantal.R
import com.barangay.pantal.model.RequestAdmin
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RequestsAdminAdapter(
    private val onApproveClick: (RequestAdmin) -> Unit,
    private val onRejectClick: (RequestAdmin) -> Unit,
    private val onViewClick: (RequestAdmin) -> Unit = {}
) : ListAdapter<RequestAdmin, RequestsAdminAdapter.RequestAdminViewHolder>(RequestAdminDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestAdminViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_request_admin, parent, false)
        return RequestAdminViewHolder(view, onApproveClick, onRejectClick, onViewClick)
    }

    override fun onBindViewHolder(holder: RequestAdminViewHolder, position: Int) {
        val request = getItem(position)
        holder.bind(request)
    }

    class RequestAdminViewHolder(
        itemView: View,
        private val onApproveClick: (RequestAdmin) -> Unit,
        private val onRejectClick: (RequestAdmin) -> Unit,
        private val onViewClick: (RequestAdmin) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val requestTypeTextView: TextView = itemView.findViewById(R.id.tvRequestType)
        private val requestStatusTextView: TextView = itemView.findViewById(R.id.tvRequestStatus)
        private val requesterNameTextView: TextView = itemView.findViewById(R.id.tvRequesterName)
        private val requestDateTextView: TextView = itemView.findViewById(R.id.tvRequestDate)
        private val approveButton: Button = itemView.findViewById(R.id.btnApprove)
        private val rejectButton: Button = itemView.findViewById(R.id.btnReject)
        private val viewButton: Button? = itemView.findViewById(R.id.btnView)

        fun bind(request: RequestAdmin) {
            requesterNameTextView.text = request.userName.ifEmpty { "User ID: ${request.userId.take(8)}" }
            requestTypeTextView.text = request.serviceName
            requestStatusTextView.text = request.status
            
            val dateStr = request.timestamp?.let {
                SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(it))
            } ?: request.date ?: "No date"
            requestDateTextView.text = dateStr

            // Status color
            val statusColor = when (request.status) {
                "Approved" -> R.color.green_completed
                "Rejected" -> R.color.red_status
                else -> R.color.yellow_pending
            }
            requestStatusTextView.setTextColor(ContextCompat.getColor(itemView.context, statusColor))

            // Hide/Show buttons
            viewButton?.visibility = View.VISIBLE
            if (request.status == "Pending") {
                approveButton.visibility = View.VISIBLE
                rejectButton.visibility = View.VISIBLE
            } else {
                approveButton.visibility = View.GONE
                rejectButton.visibility = View.GONE
            }

            approveButton.setOnClickListener { onApproveClick(request) }
            rejectButton.setOnClickListener { onRejectClick(request) }
            viewButton?.setOnClickListener { onViewClick(request) }
            
            // Allow clicking the item to view if it's already approved
            if (request.status == "Approved") {
                itemView.setOnClickListener { onViewClick(request) }
            } else {
                itemView.setOnClickListener(null)
            }
        }
    }
}

class RequestAdminDiffCallback : DiffUtil.ItemCallback<RequestAdmin>() {
    override fun areItemsTheSame(oldItem: RequestAdmin, newItem: RequestAdmin): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: RequestAdmin, newItem: RequestAdmin): Boolean {
        return oldItem == newItem
    }
}
