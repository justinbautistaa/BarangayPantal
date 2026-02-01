package com.barangay.pantal.ui.activities

import android.content.Intent
import android.os.Bundle
import com.barangay.pantal.R
import com.barangay.pantal.databinding.ActivityUserDashboardBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class UserDashboardActivity : BaseActivity() {

    private lateinit var binding: ActivityUserDashboardBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.cardAnnouncements.setOnClickListener {
            startActivity(Intent(this, AnnouncementsActivity::class.java))
        }

        binding.cardRequests.setOnClickListener {
            startActivity(Intent(this, RequestsActivity::class.java))
        }

        binding.cardServices.setOnClickListener {
            startActivity(Intent(this, ServicesActivity::class.java))
        }

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_dashboard -> {
                    true
                }
                R.id.navigation_announcements -> {
                    startActivity(Intent(this, AnnouncementsActivity::class.java))
                    true
                }
                R.id.navigation_requests -> {
                    startActivity(Intent(this, RequestsActivity::class.java))
                    true
                }
                R.id.navigation_services -> {
                    startActivity(Intent(this, ServicesActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    override fun onResume() {
        super.onResume()
        fetchAndWelcomeUser()
    }

    private fun fetchAndWelcomeUser() {
        val user = auth.currentUser
        if (user != null) {
            val database = FirebaseDatabase.getInstance().reference.child("users").child(user.uid)
            database.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val fullName = snapshot.child("fullName").getValue(String::class.java)
                    if (fullName != null) {
                        binding.welcomeTextView.text = "Welcome, $fullName!"
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                }
            })
        }
    }
}