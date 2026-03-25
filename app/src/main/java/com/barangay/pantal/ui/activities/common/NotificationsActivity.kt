package com.barangay.pantal.ui.activities.common

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.barangay.pantal.R
import com.barangay.pantal.databinding.ActivityNotificationsBinding
import com.barangay.pantal.model.Announcement
import com.barangay.pantal.model.Request
import com.barangay.pantal.network.SupabaseClient
import com.barangay.pantal.ui.activities.BaseActivity
import com.barangay.pantal.ui.activities.admin.AdminAnnouncementsActivity
import com.barangay.pantal.ui.activities.admin.AdminRequestsActivity
import com.barangay.pantal.ui.activities.staff.StaffRequestsActivity
import com.barangay.pantal.ui.activities.user.AnnouncementsActivity
import com.barangay.pantal.ui.activities.user.RequestsActivity
import com.barangay.pantal.ui.adapters.common.NotificationsAdapter
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch

class NotificationsActivity : BaseActivity() {

    private lateinit var binding: ActivityNotificationsBinding
    private lateinit var adapter: NotificationsAdapter
    private val items = mutableListOf<NotificationUiModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificationsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        adapter = NotificationsAdapter(items) { item ->
            when (item.destination) {
                NotificationDestination.REQUESTS -> openRequests()
                NotificationDestination.ANNOUNCEMENTS -> openAnnouncements()
            }
        }

        binding.notificationsRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.notificationsRecyclerView.adapter = adapter

        loadNotifications()
    }

    private fun loadNotifications() {
        binding.progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val role = getUserRole()
                val authUser = SupabaseClient.client.auth.currentUserOrNull()
                val notificationItems = mutableListOf<NotificationUiModel>()

                val announcements = SupabaseClient.client.postgrest["announcements"]
                    .select()
                    .decodeList<Announcement>()
                    .sortedByDescending { it.timestamp }
                    .take(5)

                announcements.forEach { announcement ->
                    notificationItems.add(
                        NotificationUiModel(
                            title = announcement.title.ifBlank { getString(R.string.notifications_announcement_title) },
                            message = announcement.content.ifBlank { getString(R.string.notifications_announcement_message) },
                            timestamp = announcement.timestamp,
                            accent = announcement.priority.ifBlank { "Announcement" },
                            destination = NotificationDestination.ANNOUNCEMENTS
                        )
                    )
                }

                val requests = SupabaseClient.client.postgrest["requests"]
                    .select()
                    .decodeList<Request>()
                    .filter {
                        when (role) {
                            "admin", "staff" -> true
                            else -> authUser != null && it.userId == authUser.id
                        }
                    }
                    .sortedByDescending { it.timestamp ?: 0L }
                    .take(8)

                requests.forEach { request ->
                    val status = request.status.ifBlank { "Pending" }
                    val title = when (role) {
                        "admin", "staff" -> getString(R.string.notifications_request_title_staff, request.type.ifBlank { "Request" })
                        else -> getString(R.string.notifications_request_title_user, request.type.ifBlank { "Request" })
                    }
                    val message = when (role) {
                        "admin", "staff" -> getString(
                            R.string.notifications_request_message_staff,
                            request.name.ifBlank { "Resident" },
                            status
                        )
                        else -> getString(
                            R.string.notifications_request_message_user,
                            status
                        )
                    }

                    notificationItems.add(
                        NotificationUiModel(
                            title = title,
                            message = message,
                            timestamp = request.timestamp ?: 0L,
                            accent = status,
                            destination = NotificationDestination.REQUESTS
                        )
                    )
                }

                items.clear()
                items.addAll(notificationItems.sortedByDescending { it.timestamp })
                adapter.updateData(items.toList())

                binding.emptyStateText.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
                binding.notificationsRecyclerView.visibility = if (items.isEmpty()) View.GONE else View.VISIBLE
            } catch (e: Exception) {
                Toast.makeText(this@NotificationsActivity, getString(R.string.error_general, e.localizedMessage), Toast.LENGTH_SHORT).show()
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun openRequests() {
        val target = when (getUserRole().lowercase()) {
            "admin" -> AdminRequestsActivity::class.java
            "staff" -> StaffRequestsActivity::class.java
            else -> RequestsActivity::class.java
        }
        startActivity(Intent(this, target))
    }

    private fun openAnnouncements() {
        val target = when (getUserRole().lowercase()) {
            "admin" -> AdminAnnouncementsActivity::class.java
            else -> AnnouncementsActivity::class.java
        }
        startActivity(Intent(this, target))
    }
}

data class NotificationUiModel(
    val title: String,
    val message: String,
    val timestamp: Long,
    val accent: String,
    val destination: NotificationDestination
)

enum class NotificationDestination {
    REQUESTS,
    ANNOUNCEMENTS
}
