package com.barangay.pantal.ui.activities.admin

import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.barangay.pantal.databinding.ActivityAddAnnouncementBinding
import com.barangay.pantal.model.Announcement
import com.barangay.pantal.network.SupabaseClient
import com.barangay.pantal.ui.activities.BaseActivity
import com.barangay.pantal.util.ValidationUtils
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class AddAnnouncementActivity : BaseActivity() {

    private lateinit var binding: ActivityAddAnnouncementBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddAnnouncementBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.postAnnouncementButton.setOnClickListener {
            saveAnnouncement()
        }
    }

    private fun saveAnnouncement() {
        val title = ValidationUtils.cleanText(binding.announcementTitleEditText.text.toString(), 120)
        val content = ValidationUtils.cleanText(binding.announcementContentEditText.text.toString(), 4000)
        val priority = binding.spinnerPriority.selectedItem.toString()

        if (title.length < 5 || content.length < 10) {
            Toast.makeText(this, "Title and content must be complete", Toast.LENGTH_SHORT).show()
            return
        }

        val newAnnouncement = Announcement(
            id = UUID.randomUUID().toString(),
            title = title,
            content = content,
            priority = priority,
            date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
            timestamp = System.currentTimeMillis()
        )

        lifecycleScope.launch {
            try {
                SupabaseClient.client.postgrest["announcements"].insert(newAnnouncement)
                Toast.makeText(this@AddAnnouncementActivity, "Announcement Added", Toast.LENGTH_SHORT).show()
                finish()
            } catch (e: Exception) {
                Toast.makeText(this@AddAnnouncementActivity, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
