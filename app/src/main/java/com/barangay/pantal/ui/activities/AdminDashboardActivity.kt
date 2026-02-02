package com.barangay.pantal.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.barangay.pantal.R
import com.barangay.pantal.databinding.ActivityAdminDashboardBinding
import com.google.firebase.auth.FirebaseAuth

class AdminDashboardActivity : BaseActivity() {

    private lateinit var binding: ActivityAdminDashboardBinding
    private lateinit var auth: FirebaseAuth

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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.options_menu, menu)
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
