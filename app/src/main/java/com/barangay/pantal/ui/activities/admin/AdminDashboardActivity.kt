package com.barangay.pantal.ui.activities.admin

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.barangay.pantal.R
import com.barangay.pantal.databinding.ActivityAdminDashboardBinding
import com.barangay.pantal.network.SupabaseClient
import com.barangay.pantal.ui.activities.BaseActivity
import com.barangay.pantal.ui.activities.auth.LoginActivity
import com.barangay.pantal.ui.activities.user.ServicesActivity
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch

class AdminDashboardActivity : BaseActivity() {

    private lateinit var binding: ActivityAdminDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val user = SupabaseClient.client.auth.currentSessionOrNull()?.user
        if (user == null) {
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
            startActivity(Intent(this, AdminAnnouncementsActivity::class.java))
        }

        setupBottomNavigation(binding.bottomNavigation, R.id.navigation_dashboard)
    }

    private fun setupDashboardCounts() {
        lifecycleScope.launch {
            try {
                // Get Total Residents
                val residents = SupabaseClient.client.postgrest["residents"]
                    .select()
                    .decodeList<com.barangay.pantal.model.Resident>()
                binding.tvTotalResidents.text = residents.size.toString()

                // Get Senior Citizens (Age >= 60)
                val seniorCount = residents.count { it.age >= 60 }
                binding.tvSeniorCitizens.text = seniorCount.toString()

                // Get Total Households
                val households = SupabaseClient.client.postgrest["households"]
                    .select()
                    .decodeList<com.barangay.pantal.model.Household>()
                binding.tvTotalHouseholds.text = households.size.toString()

                // Note: Add logic for Pending Requests if you have a requests table
                
            } catch (e: Exception) {
                // Handle error silenty or with a toast
            }
        }
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
        lifecycleScope.launch {
            SupabaseClient.client.auth.signOut()
            val intent = Intent(this@AdminDashboardActivity, LoginActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
            finish()
        }
    }
}
