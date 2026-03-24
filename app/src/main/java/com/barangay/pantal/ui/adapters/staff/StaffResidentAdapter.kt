package com.barangay.pantal.ui.adapters.staff

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.barangay.pantal.databinding.ItemStaffResidentBinding
import com.barangay.pantal.model.Resident

class StaffResidentAdapter(private var residents: List<Resident>) : RecyclerView.Adapter<StaffResidentAdapter.ResidentViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResidentViewHolder {
        val binding = ItemStaffResidentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ResidentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ResidentViewHolder, position: Int) {
        holder.bind(residents[position])
    }

    override fun getItemCount() = residents.size

    fun updateData(newResidents: List<Resident>) {
        this.residents = newResidents
        notifyDataSetChanged()
    }

    inner class ResidentViewHolder(private val binding: ItemStaffResidentBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(resident: Resident) {
            binding.residentName.text = resident.name
            binding.residentAge.text = resident.age.toString()
            binding.residentAddress.text = resident.address
            binding.residentContact.text = "09171234567" // Placeholder

            binding.voterStatus.visibility = if (resident.isVoter) View.VISIBLE else View.GONE
            binding.seniorStatus.visibility = if (resident.isSenior) View.VISIBLE else View.GONE
            binding.pwdStatus.visibility = if (resident.isPwd) View.VISIBLE else View.GONE

            binding.viewButton.setOnClickListener {
                // Handle view resident details
            }
        }
    }
}
