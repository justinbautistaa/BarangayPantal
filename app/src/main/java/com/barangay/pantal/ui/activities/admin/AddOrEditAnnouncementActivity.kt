package com.barangay.pantal.ui.activities.admin

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.barangay.pantal.databinding.ActivityAddOrEditAnnouncementBinding
import com.barangay.pantal.model.Announcement
import com.barangay.pantal.model.Priority
import com.barangay.pantal.network.SupabaseClient
import com.barangay.pantal.ui.activities.BaseActivity
import android.widget.ArrayAdapter
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class AddOrEditAnnouncementActivity : BaseActivity() {

    private lateinit var binding: ActivityAddOrEditAnnouncementBinding
    private var announcementId: String? = null
    private val priorities = listOf(Priority.Low.toString(), Priority.Medium.toString(), Priority.High.toString())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddOrEditAnnouncementBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, priorities)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerPriority.adapter = adapter

        announcementId = intent.getStringExtra("announcement_id")

        if (announcementId != null) {
            binding.toolbar.title = "Edit Announcement"
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
                    val position = priorities.indexOfFirst { priority -> priority.equals(it.priority, ignoreCase = true) }
                    if (position >= 0) binding.spinnerPriority.setSelection(position)
                }
            } catch (e: Exception) {
                Toast.makeText(this@AddOrEditAnnouncementActivity, "Error loading: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveAnnouncement() {
        val title = binding.announcementTitleEditText.text.toString().trim()
        val content = binding.announcementContentEditText.text.toString().trim()
        val priority = (binding.spinnerPriority.selectedItem?.toString() ?: Priority.Medium.toString()).lowercase(Locale.getDefault())

        if (title.isEmpty() || content.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                binding.postAnnouncementButton.isEnabled = false
                if (announcementId == null) {
                    val newAnnouncement = Announcement(
                        id = UUID.randomUUID().toString(),
                        title = title,
                        date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
                        content = content,
                        priority = priority,
                        timestamp = System.currentTimeMillis()
                    )
                    SupabaseClient.client.postgrest["announcements"].insert(newAnnouncement)
                    Toast.makeText(this@AddOrEditAnnouncementActivity, "Announcement Added", Toast.LENGTH_SHORT).show()
                } else {
                    SupabaseClient.client.postgrest["announcements"].update({
                        set("title", title)
                        set("content", content)
                        set("priority", priority)
                        set("date", SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))
                    }) {
                        filter {
                            eq("id", announcementId!!)
                        }
                    }
                    Toast.makeText(this@AddOrEditAnnouncementActivity, "Announcement Updated", Toast.LENGTH_SHORT).show()
                }
                finish()
            } catch (e: Exception) {
                Log.e("AddOrEditAnnouncement", "Save failed", e)
                Toast.makeText(this@AddOrEditAnnouncementActivity, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.postAnnouncementButton.isEnabled = true
            }
        }
    }
}
