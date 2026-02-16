package com.barangay.pantal.ui.activities

import android.os.Bundle
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.Toast
import com.barangay.pantal.databinding.ActivityAddAnnouncementBinding
import com.barangay.pantal.model.Announcement
import com.barangay.pantal.model.Priority
import com.google.firebase.database.FirebaseDatabase

class AddAnnouncementActivity : BaseActivity() {

    private lateinit var binding: ActivityAddAnnouncementBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddAnnouncementBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

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
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun postAnnouncement() {
        val title = binding.announcementTitleEditText.text.toString().trim()
        val content = binding.announcementContentEditText.text.toString().trim()
        val priority = binding.spinnerPriority.selectedItem.toString()
        val date = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
        val timestamp = System.currentTimeMillis()

        if (title.isEmpty() || content.isEmpty()) {
            Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val announcement = Announcement(title, date, content, priority, timestamp)
        val database = FirebaseDatabase.getInstance().getReference("announcements")
        database.push().setValue(announcement)
            .addOnSuccessListener {
                Toast.makeText(this, "Announcement posted", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to post announcement", Toast.LENGTH_SHORT).show()
            }
    }
}
