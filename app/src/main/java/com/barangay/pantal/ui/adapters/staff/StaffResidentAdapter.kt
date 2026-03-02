package com.barangay.pantal.ui.adapters.staff

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.barangay.pantal.R
import com.barangay.pantal.databinding.ItemStaffResidentBinding
import com.barangay.pantal.model.Resident
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions

class StaffResidentAdapter(options: FirebaseRecyclerOptions<Resident>) : FirebaseRecyclerAdapter<Resident, StaffResidentAdapter.ResidentViewHolder>(options) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResidentViewHolder {
        val binding = ItemStaffResidentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ResidentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ResidentViewHolder, position: Int, model: Resident) {
        holder.bind(model)
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
