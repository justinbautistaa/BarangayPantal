package com.barangay.pantal.ui.activities.staff

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.barangay.pantal.databinding.ActivityStaffReportsBinding
import com.barangay.pantal.model.Report
import com.barangay.pantal.ui.activities.BaseActivity
import com.barangay.pantal.ui.adapters.admin.ReportAdapter

class StaffReportsActivity : BaseActivity() {

    private lateinit var binding: ActivityStaffReportsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStaffReportsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val reports = listOf(
            Report(reporterId = "system", details = "Blotter Report filed by John Doe.", timestamp = System.currentTimeMillis()),
            Report(reporterId = "system", details = "Monthly financial report for January.", timestamp = System.currentTimeMillis()),
            Report(reporterId = "system", details = "Incident Report regarding a traffic accident.", timestamp = System.currentTimeMillis())
        )

        val adapter = ReportAdapter(reports)
        binding.reportsRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.reportsRecyclerView.adapter = adapter
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
