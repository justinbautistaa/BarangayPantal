package com.barangay.pantal.ui.activities.admin

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.barangay.pantal.R
import com.barangay.pantal.databinding.ActivityAdminAnnouncementsBinding
import com.barangay.pantal.model.Announcement
import com.barangay.pantal.network.SupabaseClient
import com.barangay.pantal.ui.activities.BaseActivity
import com.barangay.pantal.ui.adapters.common.AnnouncementsAdapter
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch

class AdminAnnouncementsActivity : BaseActivity() {

    private lateinit var binding: ActivityAdminAnnouncementsBinding
    private lateinit var adapter: AnnouncementsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminAnnouncementsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        adapter = AnnouncementsAdapter(true, emptyList()) { announcement ->
            deleteAnnouncement(announcement)
        }
        
        binding.announcementsRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.announcementsRecyclerView.adapter = adapter

        binding.newAnnouncementButton.setOnClickListener {
            startActivity(Intent(this, AddOrEditAnnouncementActivity::class.java))
        }

        setupBottomNavigation(binding.bottomNavigation, View.NO_ID)
    }

    override fun onResume() {
        super.onResume()
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
                Toast.makeText(this@AdminAnnouncementsActivity, getString(R.string.error_fetching_announcements, e.localizedMessage), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun deleteAnnouncement(announcement: Announcement) {
        lifecycleScope.launch {
            try {
                SupabaseClient.client.postgrest["announcements"].delete {
                    filter {
                        eq("id", announcement.id)
                    }
                }
                Toast.makeText(this@AdminAnnouncementsActivity, getString(R.string.announcement_deleted), Toast.LENGTH_SHORT).show()
                fetchAnnouncements()
            } catch (e: Exception) {
                Toast.makeText(this@AdminAnnouncementsActivity, getString(R.string.error_delete_announcement, e.localizedMessage), Toast.LENGTH_SHORT).show()
            }
        }
    }
}
