package com.barangay.pantal.ui.activities

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.barangay.pantal.R
import com.barangay.pantal.databinding.ActivityReportsBinding
import com.barangay.pantal.model.Report
import com.barangay.pantal.ui.adapters.ReportAdapter

class ReportsActivity : BaseActivity() {

    private lateinit var binding: ActivityReportsBinding
    private lateinit var reportAdapter: ReportAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReportsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        val dashboardClass = when (getUserRole()) {
            "admin" -> AdminDashboardActivity::class.java
            else -> UserDashboardActivity::class.java
        }
        setupBottomNavigation(binding.bottomNavigation, R.id.navigation_reports, dashboardClass)

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
}