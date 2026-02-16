package com.barangay.pantal.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.barangay.pantal.R
import com.barangay.pantal.databinding.ActivityAdminDashboardBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AdminDashboardActivity : BaseActivity() {

    private lateinit var binding: ActivityAdminDashboardBinding
    private lateinit var auth: FirebaseAuth
    private val database = FirebaseDatabase.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        if (auth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        setSupportActionBar(binding.toolbar)

        setupDashboardCounts()

        binding.cardRequests.setOnClickListener {
            startActivity(Intent(this, AdminRequestsActivity::class.java))
        }

        binding.btnRequests.setOnClickListener {
            startActivity(Intent(this, AdminRequestsActivity::class.java))
        }

        binding.cardHouseholds.setOnClickListener {
            startActivity(Intent(this, HouseholdsActivity::class.java))
        }

        binding.btnManageAnnouncements.setOnClickListener {
            startActivity(Intent(this, ManageAnnouncementsActivity::class.java))
        }

        setupBottomNavigation(binding.bottomNavigation, R.id.navigation_dashboard)
    }

    private fun setupDashboardCounts() {
        // Get Total Residents
//        database.getReference("residents").addValueEventListener(object : ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                binding.tvTotalResidents.text = snapshot.childrenCount.toString()
//
//                // Get Senior Citizens
//                var seniorCount = 0
//                for (residentSnapshot in snapshot.children) {
//                    val age = residentSnapshot.child("age").getValue(Long::class.java)?.toInt() ?: 0
//                    if (age >= 60) {
//                        seniorCount++
//                    }
//                }
//                binding.tvSeniorCitizens.text = seniorCount.toString()
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                // Handle error
//            }
//        })
//
//        // Get Total Households
//        database.getReference("households").addValueEventListener(object : ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                binding.tvTotalHouseholds.text = snapshot.childrenCount.toString()
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                // Handle error
//            }
//        })
//
//        // Get Pending Requests
//        database.getReference("requests").orderByChild("status").equalTo("Pending")
//            .addValueEventListener(object : ValueEventListener {
//                override fun onDataChange(snapshot: DataSnapshot) {
//                    binding.tvPendingRequests.text = snapshot.childrenCount.toString()
//                }
//
//                override fun onCancelled(error: DatabaseError) {
//                    // Handle error
//                }
//            })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.overflow_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                logout()
                true
            }
            R.id.action_reports -> {
                startActivity(Intent(this, ReportsActivity::class.java))
                true
            }
            R.id.action_services -> {
                startActivity(Intent(this, ServicesActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun logout() {
        auth.signOut()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
