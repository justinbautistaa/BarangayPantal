package com.barangay.pantal.ui.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.barangay.pantal.R
import com.barangay.pantal.ui.activities.admin.AdminDashboardActivity
import com.barangay.pantal.ui.activities.admin.AdminAnnouncementsActivity
import com.barangay.pantal.ui.activities.admin.AdminRequestsActivity
import com.barangay.pantal.ui.activities.admin.HouseholdsActivity
import com.barangay.pantal.ui.activities.admin.ManageServicesActivity
import com.barangay.pantal.ui.activities.admin.ReportsActivity
import com.barangay.pantal.ui.activities.admin.ResidentsActivity
import com.barangay.pantal.ui.activities.admin.StaffManagementActivity
import com.barangay.pantal.ui.activities.auth.LoginActivity
import com.barangay.pantal.ui.activities.staff.StaffAnnouncementsActivity
import com.barangay.pantal.ui.activities.staff.StaffDashboardActivity
import com.barangay.pantal.ui.activities.staff.StaffRequestsActivity
import com.barangay.pantal.ui.activities.staff.StaffResidentsActivity
import com.barangay.pantal.ui.activities.user.AnnouncementsActivity
import com.barangay.pantal.ui.activities.user.ProfileActivity
import com.barangay.pantal.ui.activities.user.RequestsActivity
import com.barangay.pantal.ui.activities.user.ServicesActivity
import com.barangay.pantal.ui.activities.user.UserDashboardActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.launch
import java.util.Locale

open class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    fun getUserRole(): String {
        val sharedPref = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        return sharedPref.getString("user_role", "user") ?: "user"
    }

    fun setupBottomNavigation(navView: BottomNavigationView, selectedItemId: Int) {
        val userRole = getUserRole().lowercase(Locale.getDefault())
        val menuRes = when (userRole) {
            "admin" -> R.menu.admin_bottom_navigation_menu
            "staff" -> R.menu.staff_bottom_navigation_menu
            else -> R.menu.user_bottom_navigation_menu
        }

        navView.menu.clear()
        navView.inflateMenu(menuRes)
        if (navView.menu.findItem(selectedItemId) != null) {
            navView.selectedItemId = selectedItemId
        }

        navView.setOnItemSelectedListener { item ->
            if (item.itemId == selectedItemId) {
                return@setOnItemSelectedListener false
            }

            val targetActivity = when (item.itemId) {
                R.id.navigation_dashboard -> when (userRole) {
                    "admin" -> AdminDashboardActivity::class.java
                    "staff" -> StaffDashboardActivity::class.java
                    else -> UserDashboardActivity::class.java
                }
                R.id.navigation_requests -> when (userRole) {
                    "admin" -> AdminRequestsActivity::class.java
                    "staff" -> StaffRequestsActivity::class.java
                    else -> RequestsActivity::class.java
                }
                R.id.navigation_residents -> when (userRole) {
                    "admin" -> ResidentsActivity::class.java
                    "staff" -> StaffResidentsActivity::class.java
                    else -> null
                }
                R.id.navigation_announcements -> when (userRole) {
                    "admin" -> AdminAnnouncementsActivity::class.java
                    "staff" -> StaffAnnouncementsActivity::class.java
                    else -> AnnouncementsActivity::class.java
                }
                R.id.navigation_services -> when (userRole) {
                    "admin" -> ManageServicesActivity::class.java
                    "staff" -> null
                    else -> ServicesActivity::class.java
                }
                R.id.navigation_profile -> when (userRole) {
                    "admin", "staff" -> null
                    else -> ProfileActivity::class.java
                }
                // Overflow items (if called via navigation)
                R.id.navigation_households -> HouseholdsActivity::class.java
                R.id.navigation_staff_management -> StaffManagementActivity::class.java
                R.id.action_services -> ManageServicesActivity::class.java
                R.id.action_reports -> ReportsActivity::class.java
                else -> null
            }

            if (targetActivity != null) {
                val intent = Intent(this, targetActivity)
                startActivity(intent)
                true
            } else {
                false
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (getUserRole().lowercase(Locale.getDefault()) == "admin") {
            menuInflater.inflate(R.menu.overflow_menu, menu)
            return true
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (getUserRole().lowercase(Locale.getDefault()) != "admin") {
            return super.onOptionsItemSelected(item)
        }

        val targetActivity = when (item.itemId) {
            R.id.navigation_households -> HouseholdsActivity::class.java
            R.id.navigation_announcements -> AdminAnnouncementsActivity::class.java
            R.id.navigation_staff_management -> StaffManagementActivity::class.java
            R.id.action_reports -> ReportsActivity::class.java
            R.id.action_services -> ManageServicesActivity::class.java
            else -> null
        }

        if (targetActivity != null) {
            startActivity(Intent(this, targetActivity))
            return true
        }

        if (item.itemId == R.id.action_logout) {
            lifecycleScope.launch {
                try {
                    com.barangay.pantal.network.SupabaseClient.client.auth.signOut()
                    val intent = Intent(this@BaseActivity, LoginActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    }
                    startActivity(intent)
                    finish()
                } catch (e: Exception) {
                    Toast.makeText(
                        this@BaseActivity,
                        getString(R.string.logout_failed),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            return true
        }

        return super.onOptionsItemSelected(item)
    }
}
