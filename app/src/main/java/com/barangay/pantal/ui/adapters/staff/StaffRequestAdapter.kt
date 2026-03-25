package com.barangay.pantal.ui.adapters.staff

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.barangay.pantal.R
import com.barangay.pantal.databinding.ItemStaffRequestBinding
import com.barangay.pantal.model.Request
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class StaffRequestAdapter(private var requests: List<Request>) : RecyclerView.Adapter<StaffRequestAdapter.RequestViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestViewHolder {
        val binding = ItemStaffRequestBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RequestViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RequestViewHolder, position: Int) {
        holder.bind(requests[position])
    }

    override fun getItemCount() = requests.size

    fun updateData(newRequests: List<Request>) {
        this.requests = newRequests
        notifyDataSetChanged()
    }

    inner class RequestViewHolder(private val binding: ItemStaffRequestBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(request: Request) {
            binding.requestTitle.text = request.type
            binding.residentName.text = request.name
            binding.purposeText.text = request.purpose
            
            val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            binding.dateRequestedText.text = request.timestamp?.let { sdf.format(Date(it)) } ?: "N/A"

            binding.processedByText.text = request.name.ifBlank { "Pending assignment" }
            binding.statusText.text = request.status

            when (request.status) {
                "pending" -> {
                    binding.statusText.setBackgroundResource(R.drawable.status_pending)
                    binding.startProcessingButton.visibility = View.VISIBLE
                    binding.completeButton.visibility = View.GONE
                    binding.rejectButton.visibility = View.GONE
                }
                "processing" -> {
                    binding.statusText.setBackgroundResource(R.drawable.status_processing)
                    binding.startProcessingButton.visibility = View.GONE
                    binding.completeButton.visibility = View.VISIBLE
                    binding.rejectButton.visibility = View.VISIBLE
                }
                "completed" -> {
                    binding.statusText.setBackgroundResource(R.drawable.status_completed)
                    binding.actionButtons.visibility = View.GONE
                }
                else -> {
                    binding.statusText.visibility = View.GONE
                    binding.actionButtons.visibility = View.GONE
                }
            }
        }
    }
}
