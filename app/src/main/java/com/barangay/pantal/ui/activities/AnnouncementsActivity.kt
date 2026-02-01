package com.barangay.pantal.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.barangay.pantal.R
import com.barangay.pantal.databinding.ActivityAnnouncementsBinding
import com.barangay.pantal.model.Announcement
import com.barangay.pantal.ui.adapters.AnnouncementsAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.FirebaseDatabase

class AnnouncementsActivity : BaseActivity() {

    private lateinit var binding: ActivityAnnouncementsBinding
    private lateinit var adapter: AnnouncementsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAnnouncementsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val query = FirebaseDatabase.getInstance().getReference("announcements")
        val options = FirebaseRecyclerOptions.Builder<Announcement>()
            .setQuery(query, Announcement::class.java)
            .build()

        adapter = AnnouncementsAdapter(isAdmin = getUserRole() == "admin", options)
        binding.announcementsRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.announcementsRecyclerView.adapter = adapter

        if (getUserRole() == "admin") {
            binding.newAnnouncementButton.visibility = View.VISIBLE
            binding.newAnnouncementButton.setOnClickListener {
                startActivity(Intent(this, ManageAnnouncementsActivity::class.java))
            }
        }

        val dashboardClass = when (getUserRole()) {
            "admin" -> AdminDashboardActivity::class.java
            else -> UserDashboardActivity::class.java
        }
        setupBottomNavigation(binding.bottomNavigation, R.id.navigation_announcements, dashboardClass)
    }

    override fun onStart() {
        super.onStart()
        adapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter.stopListening()
    }
}
