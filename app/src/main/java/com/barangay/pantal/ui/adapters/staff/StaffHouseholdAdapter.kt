package com.barangay.pantal.ui.adapters.staff

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.barangay.pantal.databinding.ItemStaffHouseholdBinding
import com.barangay.pantal.model.Household

class StaffHouseholdAdapter(private var households: List<Household>) : RecyclerView.Adapter<StaffHouseholdAdapter.HouseholdViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HouseholdViewHolder {
        val binding = ItemStaffHouseholdBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HouseholdViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HouseholdViewHolder, position: Int) {
        holder.bind(households[position])
    }

    override fun getItemCount() = households.size

    fun updateData(newHouseholds: List<Household>) {
        this.households = newHouseholds
        notifyDataSetChanged()
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
