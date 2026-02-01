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
import com.barangay.pantal.model.Service

class ServicesAdminAdapter(private val onDeleteClick: (Service) -> Unit) : ListAdapter<Service, ServicesAdminAdapter.ServiceAdminViewHolder>(ServiceAdminDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceAdminViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_service_admin, parent, false)
        return ServiceAdminViewHolder(view, onDeleteClick)
    }

    override fun onBindViewHolder(holder: ServiceAdminViewHolder, position: Int) {
        val service = getItem(position)
        holder.bind(service)
    }

    class ServiceAdminViewHolder(itemView: View, private val onDeleteClick: (Service) -> Unit) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.serviceName)
        private val descriptionTextView: TextView = itemView.findViewById(R.id.serviceDescription)
        private val deleteButton: Button = itemView.findViewById(R.id.deleteServiceButton)

        fun bind(service: Service) {
            nameTextView.text = service.name
            descriptionTextView.text = service.description
            deleteButton.setOnClickListener { onDeleteClick(service) }
        }
    }
}

class ServiceAdminDiffCallback : DiffUtil.ItemCallback<Service>() {
    override fun areItemsTheSame(oldItem: Service, newItem: Service): Boolean {
        return oldItem.name == newItem.name
    }

    override fun areContentsTheSame(oldItem: Service, newItem: Service): Boolean {
        return oldItem == newItem
    }
}
