package com.barangay.pantal.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.barangay.pantal.databinding.ItemRequestBinding
import com.barangay.pantal.model.Request

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
        requests.clear()
        requests.addAll(newRequests)
        notifyDataSetChanged()
    }

    inner class RequestViewHolder(private val binding: ItemRequestBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(request: Request) {
            binding.tvName.text = request.name
            binding.tvRequestType.text = request.type
            binding.tvDate.text = request.date
            binding.tvPurpose.text = request.purpose
            binding.tvStatus.text = request.status

            if (isAdmin) {
                binding.btnApprove.visibility = View.VISIBLE
                binding.btnReject.visibility = View.VISIBLE
                binding.btnApprove.setOnClickListener { listener.onApproveRequest(request) }
                binding.btnReject.setOnClickListener { listener.onRejectRequest(request) }
            } else {
                binding.btnApprove.visibility = View.GONE
                binding.btnReject.visibility = View.GONE
            }

            binding.btnView.setOnClickListener { listener.onViewRequest(request) }
        }
    }
}