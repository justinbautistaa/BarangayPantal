package com.barangay.pantal.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.barangay.pantal.R
import com.barangay.pantal.databinding.ItemResidentBinding
import com.barangay.pantal.model.Resident
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.android.material.chip.Chip

class ResidentsAdapter(
    options: FirebaseRecyclerOptions<Resident>,
    private val onEditClick: (Resident) -> Unit,
    private val onDeleteClick: (String) -> Unit
) : FirebaseRecyclerAdapter<Resident, ResidentsAdapter.ViewHolder>(options) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemResidentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, model: Resident) {
        holder.bind(model)
    }

    inner class ViewHolder(private val binding: ItemResidentBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(resident: Resident) {
            binding.name.text = resident.name
            binding.details.text = "${resident.age} years • ${resident.gender}"
            binding.address.text = resident.address
            binding.occupation.text = resident.occupation
            binding.initial.text = resident.name.first().toString()

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
