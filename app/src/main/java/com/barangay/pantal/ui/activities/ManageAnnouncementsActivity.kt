package com.barangay.pantal.ui.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.barangay.pantal.databinding.ActivityManageAnnouncementsBinding
import com.barangay.pantal.model.Announcement
import com.google.firebase.database.FirebaseDatabase

class ManageAnnouncementsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityManageAnnouncementsBinding
    private var announcementId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManageAnnouncementsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        announcementId = intent.getStringExtra("announcement_id")

        if (announcementId != null) {
            loadAnnouncementData()
        }

        binding.saveAnnouncementButton.setOnClickListener {
            saveAnnouncement()
        }
    }

    private fun loadAnnouncementData() {
        val database = FirebaseDatabase.getInstance().getReference("announcements").child(announcementId!!)
        database.get().addOnSuccessListener {
            val announcement = it.getValue(Announcement::class.java)
            if (announcement != null) {
                binding.announcementTitleEditText.setText(announcement.title)
                binding.announcementContentEditText.setText(announcement.content)
                if (announcement.priority == "High") {
                    binding.highPriorityRadioButton.isChecked = true
                } else {
                    binding.mediumPriorityRadioButton.isChecked = true
                }
            }
        }
    }

    private fun saveAnnouncement() {
        val title = binding.announcementTitleEditText.text.toString().trim()
        val content = binding.announcementContentEditText.text.toString().trim()
        val priority = if (binding.highPriorityRadioButton.isChecked) "High" else "Medium"
        val timestamp = if (announcementId != null) announcementId!!.toLong() else System.currentTimeMillis()

        if (title.isNotEmpty() && content.isNotEmpty()) {
            val database = FirebaseDatabase.getInstance().getReference("announcements")
            val newAnnouncement = Announcement(title, content, timestamp.toString(), priority, timestamp)

            database.child(timestamp.toString()).setValue(newAnnouncement).addOnCompleteListener {
                if (it.isSuccessful) {
                    finish()
                }
            }
        }
    }
}