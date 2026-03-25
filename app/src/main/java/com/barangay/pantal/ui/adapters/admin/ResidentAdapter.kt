package com.barangay.pantal.ui.adapters.admin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.barangay.pantal.R
import com.barangay.pantal.databinding.ItemResidentBinding
import com.barangay.pantal.model.Resident
import com.google.android.material.chip.Chip
import com.squareup.picasso.Picasso

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
            val context = binding.root.context
            
            binding.name.text = resident.name

            // Initial and Image Handling
            if (!resident.imageUrl.isNullOrEmpty()) {
                binding.initial.visibility = View.GONE
                binding.residentImage.visibility = View.VISIBLE
                Picasso.get()
                    .load(resident.imageUrl)
                    .placeholder(R.drawable.ic_profile_placeholder)
                    .error(R.drawable.ic_profile_placeholder)
                    .into(binding.residentImage)
            } else {
                binding.initial.visibility = View.VISIBLE
                binding.residentImage.visibility = View.GONE
                binding.initial.text = resident.name.firstOrNull()?.toString()?.uppercase() ?: "?"
                
                // Set a consistent background color for the initial circle
                val colors = intArrayOf(
                    R.color.blue_500, R.color.green_500, R.color.purple_500, R.color.orange_500
                )
                val colorIndex = Math.abs(resident.name.hashCode()) % colors.size
                binding.initial.setBackgroundTintList(ContextCompat.getColorStateList(context, colors[colorIndex]))
            }

            val ageValue = resident.age ?: 0
            val genderValue = resident.gender ?: "N/A"
            val addressValue = resident.address ?: "N/A"
            val occupationValue = resident.occupation ?: "N/A"

            val yearsLabel = context.resources.getQuantityString(
                R.plurals.resident_years,
                ageValue,
                ageValue
            )

            binding.details.text = context.getString(
                R.string.resident_details,
                yearsLabel,
                genderValue
            )
            binding.address.text = addressValue
            binding.occupation.text = occupationValue

            binding.tags.removeAllViews()

            if (resident.isVoter) {
                binding.tags.addView(createTagChip(R.string.voter, R.color.blue_500))
            }
            if (resident.isSenior) {
                binding.tags.addView(createTagChip(R.string.senior, R.color.orange_500))
            }
            if (resident.isPwd) {
                binding.tags.addView(createTagChip(R.string.pwd, R.color.purple_500))
            }

            binding.root.setOnClickListener {
                onViewClick(resident)
            }

            binding.moreOptions.setOnClickListener { view ->
                val popup = PopupMenu(context, view)
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

        private fun createTagChip(tagResId: Int, colorResId: Int): Chip {
            val context = binding.root.context
            return Chip(context).apply {
                text = context.getString(tagResId)
                chipMinHeight = 24f
                setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_LabelSmall)
                setChipBackgroundColorResource(android.R.color.transparent)
                setChipStrokeColorResource(colorResId)
                setChipStrokeWidth(2f)
                setTextColor(ContextCompat.getColor(context, colorResId))
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
