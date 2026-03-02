package com.barangay.pantal.ui.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.barangay.pantal.R
import com.barangay.pantal.ui.activities.admin.AdminAnnouncementsActivity
import com.barangay.pantal.ui.activities.admin.AdminDashboardActivity
import com.barangay.pantal.ui.activities.admin.AdminRequestsActivity
import com.barangay.pantal.ui.activities.admin.HouseholdsActivity
import com.barangay.pantal.ui.activities.admin.ResidentsActivity
import com.barangay.pantal.ui.activities.admin.StaffManagementActivity
import com.barangay.pantal.ui.activities.staff.StaffActivityLogActivity
import com.barangay.pantal.ui.activities.staff.StaffAnnouncementsActivity
import com.barangay.pantal.ui.activities.staff.StaffDashboardActivity
import com.barangay.pantal.ui.activities.staff.StaffHouseholdsActivity
import com.barangay.pantal.ui.activities.staff.StaffRequestsActivity
import com.barangay.pantal.ui.activities.staff.StaffResidentsActivity
import com.barangay.pantal.ui.activities.user.AnnouncementsActivity
import com.barangay.pantal.ui.activities.user.MoreActivity
import com.barangay.pantal.ui.activities.user.RequestsActivity
import com.barangay.pantal.ui.activities.user.ServicesActivity
import com.barangay.pantal.ui.activities.user.UserDashboardActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
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
        navView.selectedItemId = selectedItemId

        navView.setOnNavigationItemSelectedListener { item ->
            if (item.itemId == selectedItemId) {
                return@setOnNavigationItemSelectedListener false
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
                R.id.navigation_households -> when (userRole) {
                    "admin" -> HouseholdsActivity::class.java
                    "staff" -> StaffHouseholdsActivity::class.java
                    else -> null
                }
                R.id.navigation_announcements -> when (userRole) {
                    "admin" -> AdminAnnouncementsActivity::class.java
                    "staff" -> StaffAnnouncementsActivity::class.java
                    else -> AnnouncementsActivity::class.java
                }
                R.id.navigation_staff_management -> when (userRole) {
                    "admin" -> StaffManagementActivity::class.java
                    else -> null
                }
                R.id.navigation_services -> when (userRole) {
                    "user" -> ServicesActivity::class.java
                    else -> null
                }
                R.id.navigation_more -> when (userRole) {
                    "user" -> MoreActivity::class.java
                    else -> null
                }
                R.id.navigation_activity_log -> when (userRole) {
                    "staff" -> StaffActivityLogActivity::class.java
                    else -> null
                }
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
}
