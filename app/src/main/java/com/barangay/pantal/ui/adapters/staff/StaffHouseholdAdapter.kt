package com.barangay.pantal.ui.adapters.staff

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.barangay.pantal.databinding.ItemStaffHouseholdBinding
import com.barangay.pantal.model.Household
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions

class StaffHouseholdAdapter(options: FirebaseRecyclerOptions<Household>) : FirebaseRecyclerAdapter<Household, StaffHouseholdAdapter.HouseholdViewHolder>(options) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HouseholdViewHolder {
        val binding = ItemStaffHouseholdBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HouseholdViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HouseholdViewHolder, position: Int, model: Household) {
        holder.bind(model)
    }

    inner class HouseholdViewHolder(private val binding: ItemStaffHouseholdBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(household: Household) {
            binding.householdHeadName.text = household.name
            binding.addressText.text = household.address
            binding.memberCountText.text = household.members?.size.toString() ?: "0"

            binding.membersRecyclerView.layoutManager = LinearLayoutManager(binding.root.context)
            binding.membersRecyclerView.adapter = household.members?.let { StaffHouseholdMemberAdapter(it) }
        }
    }
}
