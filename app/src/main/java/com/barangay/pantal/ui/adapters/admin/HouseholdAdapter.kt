package com.barangay.pantal.ui.adapters.admin

import android.app.AlertDialog
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.barangay.pantal.R
import com.barangay.pantal.databinding.ItemHouseholdBinding
import com.barangay.pantal.model.Household
import com.barangay.pantal.ui.activities.admin.AddHouseholdActivity
import com.barangay.pantal.ui.activities.admin.ViewHouseholdActivity

class HouseholdAdapter(
    private var households: List<Household>,
    private val onDeleteClick: (Household) -> Unit = {}
) : RecyclerView.Adapter<HouseholdAdapter.ViewHolder>() {

    private val viewPool = RecyclerView.RecycledViewPool()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemHouseholdBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val viewHolder = ViewHolder(binding)
        viewHolder.binding.membersRecyclerView.setRecycledViewPool(viewPool)
        return viewHolder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(households[position])
    }

    override fun getItemCount(): Int = households.size

    fun updateData(newHouseholds: List<Household>) {
        this.households = newHouseholds
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: ItemHouseholdBinding) : RecyclerView.ViewHolder(binding.root) {
        private val memberAdapter = HouseholdMemberAdapter(emptyList())

        init {
            binding.membersRecyclerView.apply {
                layoutManager = LinearLayoutManager(itemView.context)
                adapter = memberAdapter
            }
        }

        fun bind(household: Household) {
            binding.householdName.text = household.name
            binding.householdAddress.text = household.address
            binding.totalMembers.text = household.members.size.toString()
            binding.householdId.text = household.id

            binding.membersRecyclerView.isVisible = household.members.isNotEmpty()
            binding.membersLayout.isVisible = false
            binding.arrowIcon.setImageResource(R.drawable.ic_arrow_drop_down)
            memberAdapter.updateData(household.members)

            itemView.setOnClickListener {
                binding.membersLayout.isVisible = !binding.membersLayout.isVisible
                binding.arrowIcon.setImageResource(
                    if (binding.membersLayout.isVisible) R.drawable.ic_arrow_drop_up 
                    else R.drawable.ic_arrow_drop_down
                )
            }

            binding.viewButton.setOnClickListener {
                val intent = Intent(itemView.context, ViewHouseholdActivity::class.java)
                intent.putExtra("householdId", household.id)
                itemView.context.startActivity(intent)
            }

            binding.editButton.setOnClickListener {
                val intent = Intent(itemView.context, AddHouseholdActivity::class.java)
                intent.putExtra("household_id", household.id)
                itemView.context.startActivity(intent)
            }

            binding.deleteButton.setOnClickListener {
                AlertDialog.Builder(itemView.context)
                    .setTitle("Delete Household")
                    .setMessage("Are you sure you want to delete this household?")
                    .setPositiveButton("Delete") { _, _ ->
                        onDeleteClick(household)
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        }
    }
}
