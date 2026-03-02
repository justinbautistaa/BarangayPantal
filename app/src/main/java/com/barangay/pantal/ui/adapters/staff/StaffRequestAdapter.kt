package com.barangay.pantal.ui.adapters.staff

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.barangay.pantal.R
import com.barangay.pantal.databinding.ItemStaffRequestBinding
import com.barangay.pantal.model.Request
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions

class StaffRequestAdapter(options: FirebaseRecyclerOptions<Request>) : FirebaseRecyclerAdapter<Request, StaffRequestAdapter.RequestViewHolder>(options) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestViewHolder {
        val binding = ItemStaffRequestBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RequestViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RequestViewHolder, position: Int, model: Request) {
        holder.bind(model)
    }

    inner class RequestViewHolder(private val binding: ItemStaffRequestBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(request: Request) {
            binding.requestTitle.text = request.type
            binding.residentName.text = request.name
            binding.purposeText.text = request.purpose
            binding.dateRequestedText.text = request.date
            binding.processedByText.text = "Maria Santos" // Placeholder
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
