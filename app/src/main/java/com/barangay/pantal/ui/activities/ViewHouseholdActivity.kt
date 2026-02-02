package com.barangay.pantal.ui.activities

import android.os.Bundle
import com.barangay.pantal.databinding.ActivityViewHouseholdBinding
import com.barangay.pantal.model.Household
import com.barangay.pantal.ui.adapters.HouseholdMemberAdapter
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ViewHouseholdActivity : BaseActivity() {

    private lateinit var binding: ActivityViewHouseholdBinding
    private lateinit var householdId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewHouseholdBinding.inflate(layoutInflater)
        setContentView(binding.root)

        householdId = intent.getStringExtra("householdId")!!

        binding.toolbar.setNavigationOnClickListener { finish() }

        fetchHouseholdData()
    }

    private fun fetchHouseholdData() {
        val ref = FirebaseDatabase.getInstance().getReference("households").child(householdId)
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val household = snapshot.getValue(Household::class.java)
                if (household != null) {
                    binding.householdName.text = household.name
                    binding.householdAddress.text = household.address
                    binding.membersRecyclerView.adapter = HouseholdMemberAdapter(household.members)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }
}