package com.barangay.pantal.ui.adapters.admin

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.barangay.pantal.R
import com.barangay.pantal.databinding.ItemResidentBinding
import com.barangay.pantal.model.Resident
import com.google.android.material.chip.Chip

class ResidentAdapter(
    private var residents: List<Resident>,
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

    fun updateList(newResidents: List<Resident>) {
        val diffCallback = ResidentDiffCallback(this.residents, newResidents)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        this.residents = newResidents
        diffResult.dispatchUpdatesTo(this)
    }

    inner class ResidentViewHolder(private val binding: ItemResidentBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(resident: Resident) {
            binding.initial.text = resident.name.first().toString()
            binding.name.text = resident.name
            val yearsLabel = binding.root.context.resources.getQuantityString(
                R.plurals.resident_years,
                resident.age,
                resident.age
            )
            binding.details.text = binding.root.context.getString(R.string.resident_details, yearsLabel, resident.gender)
            binding.address.text = resident.address
            binding.occupation.text = resident.occupation

            binding.tags.removeAllViews()

            if (resident.isVoter) {
                binding.tags.addView(createTagChip(R.string.voter))
            }
            if (resident.isSenior) {
                binding.tags.addView(createTagChip(R.string.senior))
            }
            if (resident.isPwd) {
                binding.tags.addView(createTagChip(R.string.pwd))
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

        private fun createTagChip(tagResId: Int): Chip {
            return Chip(binding.root.context).apply {
                text = binding.root.context.getString(tagResId)
            }
        }
    }
}

class ResidentDiffCallback(
    private val oldList: List<Resident>,
    private val newList: List<Resident>
) : DiffUtil.Callback() {
    override fun getOldListSize(): Int = oldList.size
    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].id == newList[newItemPosition].id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
}
