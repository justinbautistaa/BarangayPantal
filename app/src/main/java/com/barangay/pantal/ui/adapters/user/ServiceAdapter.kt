package com.barangay.pantal.ui.adapters.user

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.barangay.pantal.databinding.ItemServiceBinding
import com.barangay.pantal.model.Service

class ServiceAdapter(
    private var services: List<Service>,
    private val isAdmin: Boolean = false,
    private val onEditClick: (Service) -> Unit = {},
    private val onDeleteClick: (Service) -> Unit = {},
    private val onItemClick: (Service) -> Unit = {}
) : RecyclerView.Adapter<ServiceAdapter.ServiceViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceViewHolder {
        val binding = ItemServiceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ServiceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ServiceViewHolder, position: Int) {
        holder.bind(services[position])
    }

    override fun getItemCount() = services.size

    fun updateData(newServices: List<Service>) {
        this.services = newServices
        notifyDataSetChanged()
    }

    inner class ServiceViewHolder(private val binding: ItemServiceBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(service: Service) {
            binding.serviceName.text = service.name
            binding.serviceDescription.text = service.description

            if (isAdmin) {
                binding.btnEditService.visibility = View.VISIBLE
                binding.btnDeleteService.visibility = View.VISIBLE

                binding.btnEditService.setOnClickListener { onEditClick(service) }
                binding.btnDeleteService.setOnClickListener { onDeleteClick(service) }
            } else {
                binding.btnEditService.visibility = View.GONE
                binding.btnDeleteService.visibility = View.GONE
                binding.root.setOnClickListener { onItemClick(service) }
            }
        }
    }
}
