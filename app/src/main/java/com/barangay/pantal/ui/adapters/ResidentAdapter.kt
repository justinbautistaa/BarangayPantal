package com.barangay.pantal.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.barangay.pantal.R
import com.barangay.pantal.databinding.ItemResidentBinding
import com.barangay.pantal.model.Resident
import com.google.android.material.chip.Chip

class ResidentAdapter(
    private val residents: List<Resident>,
    private val onViewClick: (Resident) -> Unit,
    private val onEditClick: (Resident) -> Unit,
    private val onDeleteClick: (String) -> Unit
) :
    RecyclerView.Adapter<ResidentAdapter.ResidentViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResidentViewHolder {
        val binding = ItemResidentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ResidentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ResidentViewHolder, position: Int) {
        val resident = residents[position]
        holder.bind(resident)
    }

    override fun getItemCount(): Int = residents.size

    inner class ResidentViewHolder(private val binding: ItemResidentBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(resident: Resident) {
            binding.initial.text = resident.name.first().toString()
            binding.name.text = resident.name
            binding.details.text = "${resident.age} years • ${resident.gender}"
            binding.address.text = resident.address
            binding.occupation.text = resident.occupation

            binding.tags.removeAllViews()

            if (resident.isVoter) {
                binding.tags.addView(createTagChip("Voter"))
            }
            if (resident.isSenior) {
                binding.tags.addView(createTagChip("Senior"))
            }
            if (resident.isPwd) {
                binding.tags.addView(createTagChip("PWD"))
            }

            binding.root.setOnClickListener {
                onViewClick(resident)
            }

            binding.moreOptions.setOnClickListener { view ->
                val popup = PopupMenu(binding.root.context, view)
                popup.inflate(R.menu.resident_item_menu)
                popup.setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.menu_edit_resident -> {
                            onEditClick(resident)
                            true
                        }
                        R.id.menu_delete_resident -> {
                            onDeleteClick(resident.id)
                            true
                        }
                        else -> false
                    }
                }
                popup.show()
            }
        }

        private fun createTagChip(tag: String): Chip {
            return Chip(binding.root.context).apply {
                text = tag
            }
        }
    }
}