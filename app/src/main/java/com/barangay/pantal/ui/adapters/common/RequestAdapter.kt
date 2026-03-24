package com.barangay.pantal.ui.adapters.common

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.barangay.pantal.R
import com.barangay.pantal.databinding.ItemRequestBinding
import com.barangay.pantal.model.Request
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RequestAdapter(
    private val context: Context,
    private var requests: MutableList<Request>,
    private val listener: OnRequestInteractionListener,
    private val isAdmin: Boolean
) : RecyclerView.Adapter<RequestAdapter.RequestViewHolder>() {

    interface OnRequestInteractionListener {
        fun onViewRequest(request: Request)
        fun onApproveRequest(request: Request)
        fun onRejectRequest(request: Request)
        fun onDownloadPdf(request: Request)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestViewHolder {
        val binding = ItemRequestBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RequestViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RequestViewHolder, position: Int) {
        val request = requests[position]
        holder.bind(request)
    }

    override fun getItemCount(): Int = requests.size

    fun updateList(newRequests: List<Request>) {
        val diffCallback = RequestDiffCallback(this.requests, newRequests)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        this.requests.clear()
        this.requests.addAll(newRequests)
        diffResult.dispatchUpdatesTo(this)
    }

    inner class RequestViewHolder(private val binding: ItemRequestBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(request: Request) {
            val tvInitial: TextView = itemView.findViewById(R.id.tvInitial)
            binding.tvName.text = request.name
            binding.tvRequestType.text = request.type
            
            val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            binding.tvDate.text = request.timestamp?.let { sdf.format(Date(it)) } ?: "N/A"

            binding.tvPurpose.text = request.purpose
            binding.tvStatus.text = request.status
            binding.tvRequestId.text = "Request ID: ${request.id.take(8)}"
            
            tvInitial.text = request.name.firstOrNull()?.toString() ?: "U"

            // Status color and Download button visibility
            when (request.status) {
                "Approved" -> {
                    binding.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.green_completed))
                    if (!request.pdfUrl.isNullOrEmpty() && !isAdmin) {
                        binding.btnDownload.visibility = View.VISIBLE
                    } else {
                        binding.btnDownload.visibility = View.GONE
                    }
                }
                "Rejected" -> {
                    binding.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.red_status))
                    binding.btnDownload.visibility = View.GONE
                }
                else -> {
                    binding.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.yellow_pending))
                    binding.btnDownload.visibility = View.GONE
                }
            }

            if (isAdmin) {
                binding.btnApprove.visibility = if (request.status == "Pending") View.VISIBLE else View.GONE
                binding.btnReject.visibility = if (request.status == "Pending") View.VISIBLE else View.GONE
                binding.btnApprove.setOnClickListener { listener.onApproveRequest(request) }
                binding.btnReject.setOnClickListener { listener.onRejectRequest(request) }
            } else {
                binding.btnApprove.visibility = View.GONE
                binding.btnReject.visibility = View.GONE
            }

            binding.btnView.setOnClickListener { listener.onViewRequest(request) }
            binding.btnDownload.setOnClickListener { listener.onDownloadPdf(request) }
        }
    }
}

class RequestDiffCallback(
    private val oldList: List<Request>,
    private val newList: List<Request>
) : DiffUtil.Callback() {
    override fun getOldListSize(): Int = oldList.size
    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].id == newList[newItemPosition].id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
}
