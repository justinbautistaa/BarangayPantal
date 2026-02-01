package com.barangay.pantal.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.barangay.pantal.R
import com.barangay.pantal.databinding.ActivityHouseholdsBinding
import com.barangay.pantal.model.Household
import com.barangay.pantal.ui.adapters.HouseholdAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.FirebaseDatabase

class HouseholdsActivity : BaseActivity() {

    private lateinit var binding: ActivityHouseholdsBinding
    private lateinit var adapter: HouseholdAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHouseholdsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()

        binding.addHouseholdButton.setOnClickListener {
            startActivity(Intent(this, AddHouseholdActivity::class.java))
        }

        val dashboardClass = when (getUserRole()) {
            "admin" -> AdminDashboardActivity::class.java
            else -> UserDashboardActivity::class.java
        }
        setupBottomNavigation(binding.bottomNavigation, R.id.navigation_households, dashboardClass)
    }

    private fun setupRecyclerView() {
        val query = FirebaseDatabase.getInstance().reference.child("households")
        val options = FirebaseRecyclerOptions.Builder<Household>()
            .setQuery(query, Household::class.java)
            .build()

        adapter = HouseholdAdapter(options)
        binding.householdsRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.householdsRecyclerView.adapter = adapter
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
