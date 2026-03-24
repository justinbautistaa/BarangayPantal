package com.barangay.pantal.ui.adapters.staff

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.barangay.pantal.databinding.ItemStaffHouseholdMemberBinding
import com.barangay.pantal.model.HouseholdMember

class StaffHouseholdMemberAdapter(private val members: List<HouseholdMember>) : RecyclerView.Adapter<StaffHouseholdMemberAdapter.MemberViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemberViewHolder {
        val binding = ItemStaffHouseholdMemberBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MemberViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MemberViewHolder, position: Int) {
        holder.bind(members[position])
    }

    override fun getItemCount() = members.size

    inner class MemberViewHolder(private val binding: ItemStaffHouseholdMemberBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(member: HouseholdMember) {
            binding.memberName.text = member.name
            binding.memberDetails.text = "${member.age} years old • ${member.occupation}"
        }
    }
}
