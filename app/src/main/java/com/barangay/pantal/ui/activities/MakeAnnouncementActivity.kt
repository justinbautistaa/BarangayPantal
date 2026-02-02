package com.barangay.pantal.ui.activities

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import com.barangay.pantal.databinding.ActivityMakeAnnouncementBinding
import com.barangay.pantal.model.Announcement
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MakeAnnouncementActivity : BaseActivity() {

    private lateinit var binding: ActivityMakeAnnouncementBinding
    private var announcementId: String? = null
    private var isEditMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMakeAnnouncementBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        val priorities = arrayOf("High", "Medium", "Low")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, priorities)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.prioritySpinner.adapter = adapter

        announcementId = intent.getStringExtra("announcementId")
        isEditMode = announcementId != null

        if (isEditMode) {
            loadAnnouncementData()
            binding.postAnnouncementButton.text = "Update Announcement"
        } else {
            binding.postAnnouncementButton.text = "Post Announcement"
        }

        binding.postAnnouncementButton.setOnClickListener {
            val announcementText = binding.announcementEditText.text.toString().trim()
            val announcementTitle = binding.titleEditText.text.toString().trim()
            val selectedPriority = binding.prioritySpinner.selectedItem.toString()


            if (announcementText.isNotEmpty() && announcementTitle.isNotEmpty()) {
                postAnnouncement(announcementTitle, announcementText, selectedPriority)
            } else {
                Toast.makeText(this, "Please enter a title and announcement", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadAnnouncementData() {
        val database = FirebaseDatabase.getInstance().reference.child("announcements").child(announcementId!!)
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val announcement = snapshot.getValue(Announcement::class.java)
                if (announcement != null) {
                    binding.titleEditText.setText(announcement.title)
                    binding.announcementEditText.setText(announcement.content)
                    val priorityPosition = (binding.prioritySpinner.adapter as ArrayAdapter<String>).getPosition(announcement.priority)
                    binding.prioritySpinner.setSelection(priorityPosition)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MakeAnnouncementActivity, "Failed to load announcement data", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun postAnnouncement(title: String, announcementText: String, priority: String) {
        val database = FirebaseDatabase.getInstance().reference.child("announcements")
        val id = if (isEditMode) announcementId!! else database.push().key!!

        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val currentDate = sdf.format(Date())

        val announcement = Announcement(
            title = title,
            content = announcementText,
            priority = priority,
            date = currentDate,
            timestamp = System.currentTimeMillis()
        )

        database.child(id).setValue(announcement).addOnCompleteListener {
            if (it.isSuccessful) {
                val message = if (isEditMode) "Announcement updated" else "Announcement posted"
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                finish()
            } else {
                val message = if (isEditMode) "Failed to update announcement" else "Failed to post announcement"
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            }
        }
    }
}
