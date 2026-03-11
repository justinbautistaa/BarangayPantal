package com.barangay.pantal.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.barangay.pantal.databinding.ItemResidentBinding
import com.barangay.pantal.model.Resident

class ResidentsAdapter(
    private var residents: List<Resident>,
    private val onEditClick: (Resident) -> Unit,
    private val onDeleteClick: (Resident) -> Unit
) : RecyclerView.Adapter<ResidentsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemResidentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(residents[position])
    }

    override fun getItemCount(): Int = residents.size

    fun updateData(newResidents: List<Resident>) {
        this.residents = newResidents
        notifyDataSetChanged()
    }

    inner class ViewHolder(private val binding: ItemResidentBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(resident: Resident) {
            binding.name.text = resident.name
            binding.details.text = "${resident.age} years • ${resident.gender}"
            binding.address.text = resident.address
            binding.occupation.text = resident.occupation

            // You can add logic here for the more_options button if needed
            // For now, we assume edit/delete are handled elsewhere or via a long press.
        }
    }
}
