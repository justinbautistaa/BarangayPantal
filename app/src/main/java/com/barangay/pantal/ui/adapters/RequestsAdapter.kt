package com.barangay.pantal.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.barangay.pantal.databinding.ItemRequestAdminBinding
import com.barangay.pantal.model.Request

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
        val binding = ItemRequestAdminBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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

    inner class RequestViewHolder(private val binding: ItemRequestAdminBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(request: Request) {
            binding.tvRequestType.text = request.type
            binding.tvRequestStatus.text = request.status
            binding.tvRequesterName.text = request.name

            binding.btnApprove.setOnClickListener { listener.onApproveRequest(request) }
            binding.btnReject.setOnClickListener { listener.onRejectRequest(request) }
        }
    }
}
