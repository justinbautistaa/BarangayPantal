package com.barangay.pantal

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.barangay.pantal.databinding.ActivityDashboardBinding

class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Logout Button Click Listener
        binding.btnLogout.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        // Quick Actions Click Listeners
        binding.cardAddResident.setOnClickListener {
            startActivity(Intent(this, ResidentsActivity::class.java))
        }
        binding.cardReports.setOnClickListener {
            showToast("Reports feature")
        }
        binding.cardRequests.setOnClickListener {
            startActivity(Intent(this, RequestsActivity::class.java))
        }
        binding.cardAnnounce.setOnClickListener {
            startActivity(Intent(this, AnnouncementsActivity::class.java))
        }

        // Load sample data
        loadRecentActivity()
        loadRecentRequests()
    }

    private fun loadRecentActivity() {
        val layout = binding.layoutActivityList

        val activities = listOf(
            "New resident added\nJustin Bautista • 2h ago",
            "Certificate approved\nCyril Sarenas • 5h ago",
            "Household updated\nGarcia Family • 1d ago"
        )

        activities.forEach { activity ->
            val textView = TextView(this).apply {
                text = activity
                textSize = 14f
                setPadding(0, 8, 0, 8)
            }
            layout.addView(textView)
        }
    }

    private fun loadRecentRequests() {
        val layout = binding.layoutRequestList

        val requests = listOf(
            "Juan dela Cruz\nBarangay Clearance • Job Application • Completed",
            "Ana Reyes\nCertificate of Residency • Bank Requirement • Processing",
            "Roberto Garcia\nBusiness Permit • New Business • Pending"
        )

        requests.forEach { request ->
            val textView = TextView(this).apply {
                text = request
                textSize = 14f
                setPadding(0, 8, 0, 8)
            }
            layout.addView(textView)
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, "$message coming soon", Toast.LENGTH_SHORT).show()
    }
}
