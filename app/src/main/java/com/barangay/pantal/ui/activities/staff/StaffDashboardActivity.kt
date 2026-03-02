package com.barangay.pantal.ui.activities.staff

import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.barangay.pantal.R
import com.barangay.pantal.databinding.ActivityStaffDashboardBinding
import com.barangay.pantal.model.Request
import com.barangay.pantal.ui.activities.BaseActivity
import com.barangay.pantal.ui.adapters.staff.RecentActivityAdapter
import com.barangay.pantal.ui.adapters.staff.TaskAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class StaffDashboardActivity : BaseActivity() {

    private lateinit var binding: ActivityStaffDashboardBinding
    private lateinit var auth: FirebaseAuth
    private val database = FirebaseDatabase.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStaffDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        setSupportActionBar(binding.toolbar)

        binding.welcomeText.text = "Welcome back, ${currentUser?.displayName ?: "Staff"}!"

        setupDashboardCounts()
        setupRecyclerViews()
        setupClickListeners()

        setupBottomNavigation(binding.bottomNavigation, R.id.navigation_dashboard)
    }

    private fun setupDashboardCounts() {
        // ... (dashboard count logic)
    }

    private fun setupRecyclerViews() {
        // ... (recycler view setup)
    }

    private fun setupClickListeners() {
        binding.processRequestButton.setOnClickListener {
            startActivity(Intent(this, StaffRequestsActivity::class.java))
        }
        binding.viewResidentsButton.setOnClickListener {
            startActivity(Intent(this, StaffResidentsActivity::class.java))
        }
        binding.addAnnouncementButton.setOnClickListener {
            startActivity(Intent(this, StaffAddAnnouncementActivity::class.java))
        }
        binding.viewReportsButton.setOnClickListener {
            startActivity(Intent(this, StaffReportsActivity::class.java))
        }
    }
}
