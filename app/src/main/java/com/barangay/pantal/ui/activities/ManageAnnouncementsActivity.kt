package com.barangay.pantal.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import com.barangay.pantal.R
import com.barangay.pantal.databinding.ActivityManageAnnouncementsBinding
import com.barangay.pantal.model.Announcement
import com.barangay.pantal.ui.adapters.AnnouncementsAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.FirebaseDatabase

class ManageAnnouncementsActivity : BaseActivity() {

    private lateinit var binding: ActivityManageAnnouncementsBinding
    private lateinit var adapter: AnnouncementsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManageAnnouncementsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val query = FirebaseDatabase.getInstance().getReference("announcements")
        val options = FirebaseRecyclerOptions.Builder<Announcement>()
            .setQuery(query, Announcement::class.java)
            .build()

        adapter = AnnouncementsAdapter(true, options)
        binding.announcementsRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.announcementsRecyclerView.adapter = adapter

        binding.newAnnouncementButton.setOnClickListener {
            startActivity(Intent(this, AddAnnouncementActivity::class.java))
        }

        setupBottomNavigation(binding.bottomNavigation, R.id.navigation_announcements)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
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
