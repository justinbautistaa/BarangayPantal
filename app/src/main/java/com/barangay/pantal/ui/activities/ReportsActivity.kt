package com.barangay.pantal.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import com.barangay.pantal.R
import com.barangay.pantal.databinding.ActivityReportsBinding
import com.barangay.pantal.model.Report
import com.barangay.pantal.ui.adapters.ReportAdapter
import com.google.firebase.auth.FirebaseAuth

class ReportsActivity : BaseActivity() {

    private lateinit var binding: ActivityReportsBinding
    private lateinit var reportAdapter: ReportAdapter
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReportsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        auth = FirebaseAuth.getInstance()

        // Load reports
        loadReports()
    }

    private fun loadReports() {
        // Sample reports data
        val reports = listOf(
            Report(title = "Blotter Report", date = "March 2024"),
            Report(title = "Financial Report", date = "Q1 2024"),
            Report(title = "Ordinance Violations", date = "March 2024")
        )

        // Set up RecyclerView
        reportAdapter = ReportAdapter(this, reports)
        binding.rvReports.apply {
            layoutManager = LinearLayoutManager(this@ReportsActivity)
            adapter = reportAdapter
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.options_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                logout()
                true
            }
            R.id.action_reports -> {
                // We are already in reports, do nothing.
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun logout() {
        auth.signOut()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}