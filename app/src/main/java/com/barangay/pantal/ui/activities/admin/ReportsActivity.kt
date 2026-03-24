package com.barangay.pantal.ui.activities.admin

import android.app.AlertDialog
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.barangay.pantal.databinding.ActivityReportsBinding
import com.barangay.pantal.model.Report
import com.barangay.pantal.network.SupabaseClient
import com.barangay.pantal.ui.activities.BaseActivity
import com.barangay.pantal.ui.adapters.admin.ReportAdapter
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch

class ReportsActivity : BaseActivity() {

    private lateinit var binding: ActivityReportsBinding
    private lateinit var adapter: ReportAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReportsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        adapter = ReportAdapter(emptyList()) { report ->
            showStatusUpdateDialog(report)
        }
        binding.reportsRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.reportsRecyclerView.adapter = adapter

        fetchReports()
    }

    override fun onResume() {
        super.onResume()
        fetchReports()
    }

    private fun fetchReports() {
        lifecycleScope.launch {
            try {
                val result = SupabaseClient.client.postgrest["reports"]
                    .select()
                    .decodeList<Report>()
                adapter.updateData(result.sortedByDescending { it.timestamp })
            } catch (e: Exception) {
                Toast.makeText(this@ReportsActivity, "Error fetching reports: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showStatusUpdateDialog(report: Report) {
        val statuses = arrayOf("Pending", "In Progress", "Resolved", "Cancelled")
        AlertDialog.Builder(this)
            .setTitle("Update Report Status")
            .setItems(statuses) { _, which ->
                updateReportStatus(report, statuses[which])
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateReportStatus(report: Report, newStatus: String) {
        lifecycleScope.launch {
            try {
                SupabaseClient.client.postgrest["reports"].update({
                    set("status", newStatus)
                }) {
                    filter {
                        eq("id", report.id)
                    }
                }
                Toast.makeText(this@ReportsActivity, "Status updated to $newStatus", Toast.LENGTH_SHORT).show()
                fetchReports()
            } catch (e: Exception) {
                Toast.makeText(this@ReportsActivity, "Failed to update: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
