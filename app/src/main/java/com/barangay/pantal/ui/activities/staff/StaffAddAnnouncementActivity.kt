package com.barangay.pantal.ui.activities.staff

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.barangay.pantal.databinding.ActivityAddOrEditAnnouncementBinding
import com.barangay.pantal.model.Announcement
import com.barangay.pantal.model.Priority
import com.barangay.pantal.network.SupabaseClient
import com.barangay.pantal.ui.activities.BaseActivity
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch
import java.util.UUID

class StaffAddAnnouncementActivity : BaseActivity() {

    private lateinit var binding: ActivityAddOrEditAnnouncementBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddOrEditAnnouncementBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Add Announcement"

        val priorities = listOf(Priority.Low.toString(), Priority.Medium.toString(), Priority.High.toString())
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, priorities)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerPriority.adapter = adapter

        binding.postAnnouncementButton.setOnClickListener {
            postAnnouncement()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun postAnnouncement() {
        val title = binding.announcementTitleEditText.text.toString().trim()
        val content = binding.announcementContentEditText.text.toString().trim()
        val priority = binding.spinnerPriority.selectedItem.toString().lowercase(java.util.Locale.getDefault())
        val date = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
        val timestamp = System.currentTimeMillis()

        if (title.isEmpty() || content.isEmpty()) {
            Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                binding.postAnnouncementButton.isEnabled = false
                val id = UUID.randomUUID().toString()
                val announcement = Announcement(id, title, date, content, priority, timestamp)
                
                SupabaseClient.client.postgrest["announcements"].insert(announcement)
                
                Toast.makeText(this@StaffAddAnnouncementActivity, "Announcement posted", Toast.LENGTH_SHORT).show()
                finish()
            } catch (e: Exception) {
                Log.e("StaffAddAnnouncement", "Post failed", e)
                Toast.makeText(this@StaffAddAnnouncementActivity, "Failed to post announcement: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.postAnnouncementButton.isEnabled = true
            }
        }
    }
}
