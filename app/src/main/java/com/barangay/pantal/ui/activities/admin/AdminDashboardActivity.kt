package com.barangay.pantal.ui.activities.admin

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.barangay.pantal.R
import com.barangay.pantal.databinding.ActivityAdminDashboardBinding
import com.barangay.pantal.model.Request
import com.barangay.pantal.network.SupabaseClient
import com.barangay.pantal.ui.activities.BaseActivity
import com.barangay.pantal.ui.activities.auth.LoginActivity
import com.barangay.pantal.ui.activities.common.NotificationsActivity
import com.barangay.pantal.ui.adapters.RecentRequestsAdapter
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
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
        // Ensure the title is empty if we're using a custom layout in the toolbar
        supportActionBar?.setDisplayShowTitleEnabled(false)

        setupDashboardCounts()

        // Card clicks
        binding.cardResidents.setOnClickListener {
            startActivity(Intent(this, ResidentsActivity::class.java))
        }
        binding.cardHouseholds.setOnClickListener {
            startActivity(Intent(this, HouseholdsActivity::class.java))
        }
        binding.cardRequests.setOnClickListener {
            startActivity(Intent(this, AdminRequestsActivity::class.java))
        }
        binding.cardSeniorCitizens.setOnClickListener {
            // Can open residents filtered by senior
            val intent = Intent(this, ResidentsActivity::class.java)
            intent.putExtra("filter", "senior")
            startActivity(intent)
        }

        // Quick Actions
        binding.btnAddResident.setOnClickListener {
            startActivity(Intent(this, AddResidentActivity::class.java))
        }
        binding.btnReports.setOnClickListener {
            startActivity(Intent(this, ReportsActivity::class.java))
        }
        binding.btnRequests.setOnClickListener {
            startActivity(Intent(this, AdminRequestsActivity::class.java))
        }
        binding.btnManageAnnouncements.setOnClickListener {
            startActivity(Intent(this, AdminAnnouncementsActivity::class.java))
        }
        binding.notificationIcon.setOnClickListener {
            startActivity(Intent(this, NotificationsActivity::class.java))
        }

        setupBottomNavigation(binding.bottomNavigation, R.id.navigation_dashboard)
    }

    override fun onResume() {
        super.onResume()
        setupDashboardCounts()
    }

    private fun setupDashboardCounts() {
        lifecycleScope.launch {
            var hadError = false

            val residentsResult = runCatching {
                SupabaseClient.client.postgrest["residents"]
                    .select(Columns.list("id", "is_senior"))
                    .decodeList<com.barangay.pantal.model.Resident>()
            }
            residentsResult.onSuccess { residents ->
                binding.tvTotalResidents.text = residents.size.toString()
                binding.tvSeniorCitizens.text = residents.count { it.isSenior }.toString()
            }.onFailure { error ->
                hadError = true
                binding.tvTotalResidents.text = "0"
                binding.tvSeniorCitizens.text = "0"
                Log.e("AdminDashboard", "Residents count failed", error)
            }

            val householdsResult = runCatching {
                SupabaseClient.client.postgrest["households"]
                    .select(Columns.list("id", "name"))
                    .decodeList<com.barangay.pantal.model.Household>()
                    .size
            }
            householdsResult.onSuccess { count ->
                binding.tvTotalHouseholds.text = count.toString()
            }.onFailure { error ->
                hadError = true
                binding.tvTotalHouseholds.text = "0"
                Log.e("AdminDashboard", "Households count failed", error)
            }

            val requestsResult = runCatching {
                SupabaseClient.client.postgrest["requests"]
                    .select(Columns.list("id", "name", "type", "status", "timestamp", "date", "purpose", "pdf_url", "user_id")) {
                        filter { eq("status", "Pending") }
                    }
                    .decodeList<com.barangay.pantal.model.Request>()
            }
            requestsResult.onSuccess { requests ->
                binding.tvPendingRequests.text = requests.size.toString()
                bindRecentRequests(requests.sortedByDescending { it.timestamp }.take(5))
            }.onFailure { error ->
                hadError = true
                binding.tvPendingRequests.text = "0"
                bindRecentRequests(emptyList())
                Log.e("AdminDashboard", "Pending requests count failed", error)
            }

            if (hadError) {
                Toast.makeText(this@AdminDashboardActivity, getString(R.string.error_fetching_counts), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun bindRecentRequests(requests: List<Request>) {
        binding.rvRecentRequests.adapter = RecentRequestsAdapter(this, requests)
    }
}
