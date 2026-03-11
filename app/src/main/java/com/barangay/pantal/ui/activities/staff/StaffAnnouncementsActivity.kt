package com.barangay.pantal.ui.activities.staff

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.barangay.pantal.R
import com.barangay.pantal.databinding.ActivityStaffAnnouncementsBinding
import com.barangay.pantal.model.Announcement
import com.barangay.pantal.network.SupabaseClient
import com.barangay.pantal.ui.activities.BaseActivity
import com.barangay.pantal.ui.adapters.common.AnnouncementsAdapter
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch

class StaffAnnouncementsActivity : BaseActivity() {

    private lateinit var binding: ActivityStaffAnnouncementsBinding
    private lateinit var adapter: AnnouncementsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStaffAnnouncementsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)

        adapter = AnnouncementsAdapter(false, emptyList()) { /* No delete for staff */ }
        val layoutManager = LinearLayoutManager(this)
        layoutManager.reverseLayout = true
        layoutManager.stackFromEnd = true
        binding.announcementsRecyclerView.layoutManager = layoutManager
        binding.announcementsRecyclerView.adapter = adapter

        binding.newAnnouncementButton.setOnClickListener {
            startActivity(Intent(this, StaffAddAnnouncementActivity::class.java))
        }

        setupBottomNavigation(binding.bottomNavigation, R.id.navigation_announcements)

        fetchAnnouncements()
    }

    private fun fetchAnnouncements() {
        lifecycleScope.launch {
            try {
                val result = SupabaseClient.client.postgrest["announcements"]
                    .select()
                    .decodeList<Announcement>()
                adapter.updateData(result.sortedByDescending { it.timestamp })
            } catch (e: Exception) {
                Toast.makeText(this@StaffAnnouncementsActivity, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
