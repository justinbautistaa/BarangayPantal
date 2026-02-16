package com.barangay.pantal.ui.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.barangay.pantal.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth

open class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    fun getUserRole(): String {
        val sharedPref = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        return sharedPref.getString("user_role", "user") ?: "user"
    }

    fun setupBottomNavigation(navView: BottomNavigationView, selectedItemId: Int) {
        val isAdmin = getUserRole() == "admin"
        val menuRes = if (isAdmin) {
            R.menu.admin_bottom_navigation_menu
        } else {
            R.menu.user_bottom_navigation_menu
        }

        navView.menu.clear()
        navView.inflateMenu(menuRes)
        navView.selectedItemId = selectedItemId

        navView.setOnNavigationItemSelectedListener { item ->
            if (item.itemId == selectedItemId) {
                return@setOnNavigationItemSelectedListener false
            }

            val targetActivity = when (item.itemId) {
                R.id.navigation_dashboard -> if (isAdmin) AdminDashboardActivity::class.java else UserDashboardActivity::class.java
                R.id.navigation_announcements -> AnnouncementsActivity::class.java
                R.id.navigation_requests -> if (isAdmin) AdminRequestsActivity::class.java else RequestsActivity::class.java
                R.id.navigation_households -> if (isAdmin) HouseholdsActivity::class.java else null
                R.id.navigation_residents -> if (isAdmin) ResidentsActivity::class.java else null
                R.id.navigation_more -> if (isAdmin) MoreActivity::class.java else null
                else -> null
            }

            if (targetActivity != null) {
                val intent = Intent(this, targetActivity)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                true
            } else {
                false
            }
        }
    }
}
