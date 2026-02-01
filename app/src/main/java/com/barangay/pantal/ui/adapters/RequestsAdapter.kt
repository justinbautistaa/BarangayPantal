package com.barangay.pantal.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.barangay.pantal.R
import com.barangay.pantal.databinding.ItemRequestBinding
import com.barangay.pantal.model.Request
import java.text.SimpleDateFormat
import java.util.Locale

class RequestsAdapter(
    private val context: Context,
    private var requests: List<Request>,
    private val listener: OnRequestInteractionListener
) : RecyclerView.Adapter<RequestsAdapter.RequestViewHolder>() {

    interface OnRequestInteractionListener {
        fun onViewRequest(request: Request)
        fun onApproveRequest(request: Request)
        fun onRejectRequest(request: Request)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestViewHolder {
        val binding = ItemRequestBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RequestViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RequestViewHolder, position: Int) {
        holder.bind(requests[position])
    }

    override fun getItemCount(): Int = requests.size

    fun updateList(newList: List<Request>) {
        requests = newList
        notifyDataSetChanged()
    }

    inner class RequestViewHolder(private val binding: ItemRequestBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(request: Request) {
            binding.tvName.text = request.name
            binding.tvRequestType.text = request.type
            binding.tvPurpose.text = request.purpose
            binding.tvStatus.text = request.status
            binding.tvRequestId.text = "Request ID: ${request.id}"

            val sdf = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
            request.timestamp?.let { binding.tvDate.text = sdf.format(it) }

            val statusColor = when (request.status) {
                "Completed" -> R.color.green_status
                "Processing" -> R.color.blue_status
                "Pending" -> R.color.yellow_status
                else -> R.color.gray_status
            }
            binding.tvStatus.setBackgroundColor(ContextCompat.getColor(context, statusColor))

            when (request.status) {
                "Pending" -> {
                    binding.btnView.visibility = View.VISIBLE
                    binding.btnApprove.visibility = View.VISIBLE
                    binding.btnReject.visibility = View.VISIBLE
                }
                "Processing" -> {
                    binding.btnView.visibility = View.VISIBLE
                    binding.btnApprove.visibility = View.GONE
                    binding.btnReject.visibility = View.GONE
                }
                "Completed" -> {
                    binding.btnView.visibility = View.VISIBLE
                    binding.btnApprove.visibility = View.GONE
                    binding.btnReject.visibility = View.GONE
                }
            }

            binding.btnView.setOnClickListener { listener.onViewRequest(request) }
            binding.btnApprove.setOnClickListener { listener.onApproveRequest(request) }
            binding.btnReject.setOnClickListener { listener.onRejectRequest(request) }
        }
    }
}