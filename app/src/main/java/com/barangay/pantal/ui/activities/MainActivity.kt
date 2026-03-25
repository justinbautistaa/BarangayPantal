package com.barangay.pantal.ui.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.barangay.pantal.ui.activities.admin.AdminDashboardActivity
import com.barangay.pantal.ui.activities.auth.LoginActivity
import com.barangay.pantal.ui.activities.staff.StaffDashboardActivity
import com.barangay.pantal.ui.activities.user.UserDashboardActivity

class MainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPref = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val userRole = sharedPref.getString("user_role", null)

        if (userRole == "admin") {
            startActivity(Intent(this, AdminDashboardActivity::class.java))
        } else if (userRole == "user") {
            startActivity(Intent(this, UserDashboardActivity::class.java))
        } else if (userRole == "staff") {
            startActivity(Intent(this, StaffDashboardActivity::class.java))
        } else {
            startActivity(Intent(this, LoginActivity::class.java))
        }
        finish()
    }
}
