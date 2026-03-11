package com.barangay.pantal.ui.activities.admin

import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.barangay.pantal.databinding.ActivityAddOrEditAnnouncementBinding
import com.barangay.pantal.model.Announcement
import com.barangay.pantal.network.SupabaseClient
import com.barangay.pantal.ui.activities.BaseActivity
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class AddOrEditAnnouncementActivity : BaseActivity() {

    private lateinit var binding: ActivityAddOrEditAnnouncementBinding
    private var announcementId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddOrEditAnnouncementBinding.inflate(layoutInflater)
        setContentView(binding.root)

        announcementId = intent.getStringExtra("announcement_id")

        if (announcementId != null) {
            loadAnnouncementData()
        }

        binding.postAnnouncementButton.setOnClickListener {
            saveAnnouncement()
        }
    }

    private fun loadAnnouncementData() {
        lifecycleScope.launch {
            try {
                val announcement = SupabaseClient.client.postgrest["announcements"].select {
                    filter {
                        eq("id", announcementId!!)
                    }
                }.decodeSingleOrNull<Announcement>()

                announcement?.let {
                    binding.announcementTitleEditText.setText(it.title)
                    binding.announcementContentEditText.setText(it.content)
                }
            } catch (e: Exception) {
                Toast.makeText(this@AddOrEditAnnouncementActivity, "Error loading: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveAnnouncement() {
        val title = binding.announcementTitleEditText.text.toString().trim()
        val content = binding.announcementContentEditText.text.toString().trim()

        if (title.isEmpty() || content.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                if (announcementId == null) {
                    val newAnnouncement = Announcement(
                        id = UUID.randomUUID().toString(),
                        title = title,
                        content = content,
                        priority = "Normal",
                        date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
                        timestamp = System.currentTimeMillis()
                    )
                    SupabaseClient.client.postgrest["announcements"].insert(newAnnouncement)
                    Toast.makeText(this@AddOrEditAnnouncementActivity, "Announcement Added", Toast.LENGTH_SHORT).show()
                } else {
                    SupabaseClient.client.postgrest["announcements"].update({
                        set("title", title)
                        set("content", content)
                    }) {
                        filter {
                            eq("id", announcementId!!)
                        }
                    }
                    Toast.makeText(this@AddOrEditAnnouncementActivity, "Announcement Updated", Toast.LENGTH_SHORT).show()
                }
                finish()
            } catch (e: Exception) {
                Toast.makeText(this@AddOrEditAnnouncementActivity, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
