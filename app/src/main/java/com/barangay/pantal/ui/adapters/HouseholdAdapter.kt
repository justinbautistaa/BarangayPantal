package com.barangay.pantal.ui.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.barangay.pantal.R
import com.barangay.pantal.model.Household
import com.barangay.pantal.ui.activities.AddHouseholdActivity
import com.barangay.pantal.ui.activities.ViewHouseholdActivity
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions

class HouseholdAdapter(options: FirebaseRecyclerOptions<Household>) :
    FirebaseRecyclerAdapter<Household, HouseholdAdapter.ViewHolder>(options) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_household, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, model: Household) {
        holder.bind(model)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val householdName: TextView = itemView.findViewById(R.id.household_name)
        private val householdAddress: TextView = itemView.findViewById(R.id.household_address)
        private val totalMembers: TextView = itemView.findViewById(R.id.total_members)
        private val householdId: TextView = itemView.findViewById(R.id.household_id)
        private val membersRecyclerView: RecyclerView = itemView.findViewById(R.id.members_recycler_view)
        private val membersLayout: LinearLayout = itemView.findViewById(R.id.members_layout)
        private val viewButton: Button = itemView.findViewById(R.id.view_button)
        private val editButton: Button = itemView.findViewById(R.id.edit_button)

        fun bind(household: Household) {
            householdName.text = household.name
            householdAddress.text = household.address
            totalMembers.text = household.members.size.toString()
            householdId.text = household.id

            membersRecyclerView.adapter = HouseholdMemberAdapter(household.members)

            itemView.setOnClickListener {
                membersLayout.visibility = if (membersLayout.visibility == View.VISIBLE) View.GONE else View.VISIBLE
            }

            viewButton.setOnClickListener {
                val intent = Intent(itemView.context, ViewHouseholdActivity::class.java)
                intent.putExtra("householdId", household.id)
                itemView.context.startActivity(intent)
            }

            editButton.setOnClickListener {
                val intent = Intent(itemView.context, AddHouseholdActivity::class.java)
                intent.putExtra("householdId", household.id)
                itemView.context.startActivity(intent)
            }
        }
    }
}
