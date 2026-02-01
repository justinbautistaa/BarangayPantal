package com.barangay.pantal.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.barangay.pantal.databinding.ItemServiceBinding
import com.barangay.pantal.model.Service

class ServiceAdapter(
    private val services: List<Service>,
    private val onRequestServiceClick: (Service) -> Unit
) : RecyclerView.Adapter<ServiceAdapter.ServiceViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceViewHolder {
        val binding = ItemServiceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ServiceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ServiceViewHolder, position: Int) {
        holder.bind(services[position])
    }

    override fun getItemCount(): Int = services.size

    inner class ServiceViewHolder(private val binding: ItemServiceBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(service: Service) {
            binding.serviceNameTextView.text = service.name
            binding.serviceDescriptionTextView.text = service.description

            binding.requirementsLinearLayout.removeAllViews()
            for (requirement in service.requirements) {
                val textView = TextView(binding.root.context).apply {
                    text = "• $requirement"
                }
                binding.requirementsLinearLayout.addView(textView)
            }

            binding.requestServiceButton.setOnClickListener {
                onRequestServiceClick(service)
            }
        }
    }
}