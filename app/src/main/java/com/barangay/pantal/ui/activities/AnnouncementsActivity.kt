package com.barangay.pantal.ui.activities

import android.content.Intent
import android.os.Bundle
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

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)

        val query = FirebaseDatabase.getInstance().getReference("announcements").orderByChild("timestamp")
        val options = FirebaseRecyclerOptions.Builder<Announcement>()
            .setQuery(query, Announcement::class.java)
            .build()

        adapter = AnnouncementsAdapter(getUserRole() == "admin", options)
        val layoutManager = LinearLayoutManager(this)
        layoutManager.reverseLayout = true
        layoutManager.stackFromEnd = true
        binding.announcementsRecyclerView.layoutManager = layoutManager
        binding.announcementsRecyclerView.adapter = adapter
        binding.announcementsRecyclerView.itemAnimator = null

        binding.newAnnouncementButton.setOnClickListener {
            startActivity(Intent(this, AddAnnouncementActivity::class.java))
        }

        setupBottomNavigation(binding.bottomNavigation, R.id.navigation_announcements)
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
