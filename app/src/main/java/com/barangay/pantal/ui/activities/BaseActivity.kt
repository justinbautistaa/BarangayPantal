package com.barangay.pantal.ui.activities

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.barangay.pantal.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth

open class BaseActivity : AppCompatActivity() {

    fun getUserRole(): String {
        val sharedPref = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        return sharedPref.getString("user_role", "user") ?: "user"
    }

    fun setupBottomNavigation(navView: BottomNavigationView, selectedItemId: Int) {
        val isAdmin = getUserRole() == "admin"
        val menuRes = if (isAdmin) {
            R.menu.bottom_navigation_menu_admin
        } else {
            R.menu.user_nav_menu
        }

        if (navView.menu.size() == 0) {
            navView.inflateMenu(menuRes)
        }

        navView.selectedItemId = selectedItemId

        navView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_dashboard -> {
                    val intent = if (isAdmin) {
                        Intent(this, AdminDashboardActivity::class.java)
                    } else {
                        Intent(this, UserDashboardActivity::class.java)
                    }
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    startActivity(intent)
                    true
                }
                R.id.navigation_announcements -> {
                    startActivity(Intent(this, AnnouncementsActivity::class.java))
                    true
                }
                R.id.navigation_requests -> {
                    if (isAdmin) {
                        startActivity(Intent(this, AdminRequestsActivity::class.java))
                    } else {
                        startActivity(Intent(this, RequestsActivity::class.java))
                    }
                    true
                }
                R.id.navigation_households -> {
                    if (isAdmin) {
                        startActivity(Intent(this, HouseholdsActivity::class.java))
                    }
                    true
                }
                R.id.navigation_residents -> {
                    if (isAdmin) {
                        startActivity(Intent(this, ResidentsActivity::class.java))
                    }
                    true
                }
                R.id.navigation_services -> {
                    if (!isAdmin) {
                        startActivity(Intent(this, ServicesActivity::class.java))
                    }
                    true
                }
                R.id.navigation_reports -> {
                    if (!isAdmin) {
                        startActivity(Intent(this, ReportsActivity::class.java))
                    }
                    true
                }
                else -> false
            }
        }
    }
}
