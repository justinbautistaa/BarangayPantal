package com.barangay.pantal.ui.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.barangay.pantal.databinding.ItemHouseholdBinding
import com.barangay.pantal.model.Household
import com.barangay.pantal.ui.activities.ViewHouseholdActivity
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions

class HouseholdAdapter(options: FirebaseRecyclerOptions<Household>) :
    FirebaseRecyclerAdapter<Household, HouseholdAdapter.ViewHolder>(options) {

    private val viewPool = RecyclerView.RecycledViewPool()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemHouseholdBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val viewHolder = ViewHolder(binding)
        viewHolder.binding.membersRecyclerView.setRecycledViewPool(viewPool)
        return viewHolder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, model: Household) {
        holder.bind(model)
    }

    inner class ViewHolder(val binding: ItemHouseholdBinding) : RecyclerView.ViewHolder(binding.root) {
        private lateinit var household: Household
        private val memberAdapter = HouseholdMemberAdapter(emptyList())

        init {
            binding.membersRecyclerView.apply {
                layoutManager = LinearLayoutManager(itemView.context)
                adapter = memberAdapter
            }
        }

        fun bind(household: Household) {
            if (adapterPosition == RecyclerView.NO_POSITION) {
                return
            }
            this.household = household
            binding.householdName.text = household.name
            binding.householdAddress.text = household.address
            binding.totalMembers.text = household.members?.size?.toString() ?: "0"
            binding.householdId.text = household.id

            if (household.members.isNullOrEmpty()) {
                binding.membersRecyclerView.visibility = View.GONE
            } else {
                binding.membersRecyclerView.visibility = View.VISIBLE
                memberAdapter.updateData(household.members)
            }

            itemView.setOnClickListener {
                binding.membersLayout.visibility = if (binding.membersLayout.visibility == View.VISIBLE) View.GONE else View.VISIBLE
            }

            binding.viewButton.setOnClickListener {
                val intent = Intent(itemView.context, ViewHouseholdActivity::class.java)
                intent.putExtra("householdId", household.id)
                itemView.context.startActivity(intent)
            }
        }
    }
}
