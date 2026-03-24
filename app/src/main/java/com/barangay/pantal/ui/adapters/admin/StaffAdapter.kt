package com.barangay.pantal.ui.adapters.admin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.barangay.pantal.databinding.ItemStaffBinding
import com.barangay.pantal.model.User

class StaffAdapter(
    private var staffList: List<User>,
    private val onDeleteClick: (User) -> Unit
) : RecyclerView.Adapter<StaffAdapter.StaffViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StaffViewHolder {
        val binding = ItemStaffBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StaffViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StaffViewHolder, position: Int) {
        holder.bind(staffList[position])
    }

    override fun getItemCount(): Int = staffList.size

    fun updateData(newList: List<User>) {
        staffList = newList
        notifyDataSetChanged()
    }

    inner class StaffViewHolder(private val binding: ItemStaffBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(staff: User) {
            binding.tvStaffName.text = staff.fullName
            binding.tvStaffEmail.text = staff.email
            binding.btnDeleteStaff.setOnClickListener { onDeleteClick(staff) }
        }
    }
}
