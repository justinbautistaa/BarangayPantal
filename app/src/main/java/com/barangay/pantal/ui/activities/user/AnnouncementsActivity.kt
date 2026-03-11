package com.barangay.pantal.ui.activities.user

import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.barangay.pantal.R
import com.barangay.pantal.databinding.ActivityAnnouncementsBinding
import com.barangay.pantal.model.Announcement
import com.barangay.pantal.network.SupabaseClient
import com.barangay.pantal.ui.activities.BaseActivity
import com.barangay.pantal.ui.adapters.common.AnnouncementsAdapter
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch

class AnnouncementsActivity : BaseActivity() {

    private lateinit var binding: ActivityAnnouncementsBinding
    private lateinit var adapter: AnnouncementsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAnnouncementsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)

        adapter = AnnouncementsAdapter(false, emptyList()) { /* No delete for users */ }
        val layoutManager = LinearLayoutManager(this)
        layoutManager.reverseLayout = true
        layoutManager.stackFromEnd = true
        binding.announcementsRecyclerView.layoutManager = layoutManager
        binding.announcementsRecyclerView.adapter = adapter

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
                Toast.makeText(this@AnnouncementsActivity, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
