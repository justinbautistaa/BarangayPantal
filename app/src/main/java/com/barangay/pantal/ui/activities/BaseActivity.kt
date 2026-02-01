package com.barangay.pantal.ui.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.barangay.pantal.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth

open class BaseActivity : AppCompatActivity() {

    fun getUserRole(): String {
        // This is a placeholder. In a real app, you'd have a more robust way
        // of determining the user's role.
        val user = FirebaseAuth.getInstance().currentUser
        return if (user?.email?.endsWith("@admin.com") == true) {
            "admin"
        } else {
            "user"
        }
    }

    fun setupBottomNavigation(navView: BottomNavigationView, selectedItemId: Int, dashboardClass: Class<*>) {
        navView.selectedItemId = selectedItemId
        navView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_dashboard -> {
                    val intent = Intent(this, dashboardClass)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    startActivity(intent)
                    true
                }
                R.id.navigation_announcements -> {
                    startActivity(Intent(this, AnnouncementsActivity::class.java))
                    true
                }
                R.id.navigation_requests -> {
                    if (getUserRole() == "admin") {
                        startActivity(Intent(this, AdminRequestsActivity::class.java))
                    } else {
                        startActivity(Intent(this, RequestsActivity::class.java))
                    }
                    true
                }
                R.id.navigation_households -> {
                    startActivity(Intent(this, HouseholdsActivity::class.java))
                    true
                }
                R.id.navigation_residents -> {
                    startActivity(Intent(this, ResidentsActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }
}