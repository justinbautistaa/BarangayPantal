package com.barangay.pantal.ui.activities

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.barangay.pantal.databinding.ActivityReportsBinding
import com.barangay.pantal.model.Report
import com.barangay.pantal.ui.adapters.ReportAdapter

class ReportsActivity : BaseActivity() {

    private lateinit var binding: ActivityReportsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReportsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val reports = listOf(
            Report("Blotter Report", "A blotter report filed by John Doe.", "2024-01-15"),
            Report("Financial Report", "Monthly financial report for January.", "2024-02-01"),
            Report("Incident Report", "An incident report regarding a traffic accident.", "2024-02-10")
        )

        val adapter = ReportAdapter(reports)
        binding.reportsRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.reportsRecyclerView.adapter = adapter
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
