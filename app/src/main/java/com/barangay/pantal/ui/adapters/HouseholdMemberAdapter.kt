package com.barangay.pantal.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.barangay.pantal.R
import com.barangay.pantal.model.HouseholdMember

class HouseholdMemberAdapter(private val members: List<HouseholdMember>) :
    RecyclerView.Adapter<HouseholdMemberAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_household_member, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(members[position])
    }

    override fun getItemCount() = members.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val initial: TextView = itemView.findViewById(R.id.initial)
        private val name: TextView = itemView.findViewById(R.id.name)
        private val details: TextView = itemView.findViewById(R.id.details)
        private val role: TextView = itemView.findViewById(R.id.role)

        fun bind(member: HouseholdMember) {
            initial.text = member.name.first().toString()
            name.text = member.name
            details.text = "${member.age} yrs • ${member.occupation}"
            role.text = member.role

            if (member.role == "Head") {
                role.visibility = View.VISIBLE
            } else {
                role.visibility = View.GONE
            }
        }
    }
}
