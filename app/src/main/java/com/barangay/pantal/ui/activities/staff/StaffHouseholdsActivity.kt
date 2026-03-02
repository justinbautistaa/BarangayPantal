package com.barangay.pantal.ui.activities.staff

import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import com.barangay.pantal.R
import com.barangay.pantal.databinding.ActivityStaffHouseholdsBinding
import com.barangay.pantal.model.Household
import com.barangay.pantal.ui.activities.BaseActivity
import com.barangay.pantal.ui.adapters.staff.StaffHouseholdAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.FirebaseDatabase

class StaffHouseholdsActivity : BaseActivity() {

    private lateinit var binding: ActivityStaffHouseholdsBinding
    private lateinit var adapter: StaffHouseholdAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStaffHouseholdsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val query = FirebaseDatabase.getInstance().getReference("households").orderByChild("name")
        val options = FirebaseRecyclerOptions.Builder<Household>()
            .setQuery(query, Household::class.java)
            .build()

        adapter = StaffHouseholdAdapter(options)
        binding.householdsRecyclerView.layoutManager = GridLayoutManager(this, 2)
        binding.householdsRecyclerView.adapter = adapter

        setupBottomNavigation(binding.bottomNavigation, R.id.navigation_households)
    }

    override fun onStart() {
        super.onStart()
        adapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter.stopListening()
    }
}
