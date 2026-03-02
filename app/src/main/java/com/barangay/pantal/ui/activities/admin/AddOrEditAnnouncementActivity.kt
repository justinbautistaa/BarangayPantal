package com.barangay.pantal.ui.activities.admin

import android.os.Bundle
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.Toast
import com.barangay.pantal.databinding.ActivityAddOrEditAnnouncementBinding
import com.barangay.pantal.model.Announcement
import com.barangay.pantal.model.Priority
import com.barangay.pantal.ui.activities.BaseActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AddOrEditAnnouncementActivity : BaseActivity() {

    private lateinit var binding: ActivityAddOrEditAnnouncementBinding
    private val database = FirebaseDatabase.getInstance().reference
    private var announcementId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddOrEditAnnouncementBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val priorities = listOf(Priority.Low.toString(), Priority.Medium.toString(), Priority.High.toString())
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, priorities)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerPriority.adapter = adapter

        announcementId = intent.getStringExtra("announcement_id")

        if (announcementId != null) {
            loadAnnouncementData()
            binding.postAnnouncementButton.text = "Update Announcement"
            supportActionBar?.title = "Edit Announcement"
        } else {
            binding.postAnnouncementButton.text = "Add Announcement"
            supportActionBar?.title = "New Announcement"
        }

        binding.postAnnouncementButton.setOnClickListener {
            saveAnnouncement()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun loadAnnouncementData() {
        val announcementRef = database.child("announcements").child(announcementId!!)
        announcementRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val announcement = snapshot.getValue(Announcement::class.java)
                if (announcement != null) {
                    binding.announcementTitleEditText.setText(announcement.title)
                    binding.announcementContentEditText.setText(announcement.content)
                    val priorityIndex = (binding.spinnerPriority.adapter as ArrayAdapter<String>).getPosition(announcement.priority)
                    binding.spinnerPriority.setSelection(priorityIndex)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@AddOrEditAnnouncementActivity, "Failed to load announcement.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun saveAnnouncement() {
        val title = binding.announcementTitleEditText.text.toString().trim()
        val content = binding.announcementContentEditText.text.toString().trim()
        val priority = binding.spinnerPriority.selectedItem.toString()
        val date = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
        val timestamp = System.currentTimeMillis()

        if (title.isEmpty() || content.isEmpty()) {
            Toast.makeText(this, "Please fill out all fields.", Toast.LENGTH_SHORT).show()
            return
        }

        val announcement = Announcement(announcementId ?: "", title, date, content, priority, timestamp)

        val announcementRef = if (announcementId != null) {
            database.child("announcements").child(announcementId!!)
        } else {
            database.child("announcements").push()
        }

        announcementRef.setValue(announcement)
            .addOnSuccessListener {
                Toast.makeText(this, "Announcement saved successfully.", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to save announcement.", Toast.LENGTH_SHORT).show()
            }
    }
}
