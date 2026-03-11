package com.barangay.pantal.ui.activities.staff

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.barangay.pantal.R
import com.barangay.pantal.databinding.ActivityStaffDashboardBinding
import com.barangay.pantal.network.SupabaseClient
import com.barangay.pantal.ui.activities.BaseActivity
import com.barangay.pantal.ui.activities.auth.LoginActivity
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.launch

class StaffDashboardActivity : BaseActivity() {

    private lateinit var binding: ActivityStaffDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStaffDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val user = SupabaseClient.client.auth.currentSessionOrNull()?.user
        if (user == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        setSupportActionBar(binding.toolbar)

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

        setupBottomNavigation(binding.bottomNavigation, R.id.navigation_dashboard)
    }

    private fun logout() {
        lifecycleScope.launch {
            SupabaseClient.client.auth.signOut()
            val intent = Intent(this@StaffDashboardActivity, LoginActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
            finish()
        }
    }
}
