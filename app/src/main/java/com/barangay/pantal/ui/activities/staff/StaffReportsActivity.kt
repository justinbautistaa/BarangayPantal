package com.barangay.pantal.ui.activities.staff

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.barangay.pantal.R
import com.barangay.pantal.databinding.ActivityStaffReportsBinding
import com.barangay.pantal.model.Blotter
import com.barangay.pantal.model.Report
import com.barangay.pantal.model.reportToBlotter
import com.barangay.pantal.network.SupabaseClient
import com.barangay.pantal.ui.activities.BaseActivity
import com.barangay.pantal.ui.adapters.common.BlotterAdapter
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch

class StaffReportsActivity : BaseActivity() {

    private lateinit var binding: ActivityStaffReportsBinding
    private lateinit var adapter: BlotterAdapter
    private var currentBlotters: List<Blotter> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStaffReportsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.title = "Reports"
        binding.toolbar.setNavigationOnClickListener { finish() }

        adapter = BlotterAdapter(emptyList()) { blotter -> showBlotterActions(blotter) }
        binding.reportsRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.reportsRecyclerView.adapter = adapter

        fetchBlotters()
    }

    override fun onResume() {
        super.onResume()
        fetchBlotters()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.reports_menu, menu)
        menu?.findItem(R.id.action_manage_reports)?.isVisible = false
        menu?.findItem(R.id.action_new_report)?.isVisible = false
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_export_reports -> {
                exportReportsCsv()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun fetchBlotters() {
        lifecycleScope.launch {
            try {
                val result = SupabaseClient.client.postgrest["reports"]
                    .select()
                    .decodeList<Report>()
                currentBlotters = result.map(::reportToBlotter).sortedByDescending { it.createdAt ?: "" }
                adapter.updateData(currentBlotters)
            } catch (e: Exception) {
                Toast.makeText(this@StaffReportsActivity, "Error fetching reports: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showBlotterActions(blotter: Blotter) {
        val actions = arrayOf("View Details", "Update Status")
        AlertDialog.Builder(this)
            .setTitle("Report #${blotter.blotterNumber.ifBlank { blotter.id.take(8) }}")
            .setItems(actions) { _, which ->
                when (which) {
                    0 -> showBlotterDetails(blotter)
                    1 -> showStatusUpdateDialog(blotter)
                }
            }
            .setNegativeButton("Close", null)
            .show()
    }

    private fun showBlotterDetails(blotter: Blotter) {
        val message = buildString {
            appendLine("Complainant: ${blotter.complainantName.ifBlank { "Resident" }}")
            appendLine("Subject: ${blotter.respondent.ifBlank { "General concern" }}")
            appendLine("Status: ${blotter.status}")
            appendLine()
            append(blotter.complaint)
        }

        AlertDialog.Builder(this)
            .setTitle("Report Details")
            .setMessage(message)
            .setPositiveButton("Close", null)
            .show()
    }

    private fun showStatusUpdateDialog(blotter: Blotter) {
        val statuses = arrayOf("Pending", "In Progress", "Resolved", "Cancelled")
        AlertDialog.Builder(this)
            .setTitle("Update Report Status")
            .setItems(statuses) { _, which ->
                updateBlotterStatus(blotter, statuses[which])
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateBlotterStatus(blotter: Blotter, newStatus: String) {
        lifecycleScope.launch {
            try {
                SupabaseClient.client.postgrest["reports"].update({
                    set("status", newStatus)
                }) {
                    filter { eq("id", blotter.id) }
                }
                Toast.makeText(this@StaffReportsActivity, "Status updated to $newStatus", Toast.LENGTH_SHORT).show()
                fetchBlotters()
            } catch (e: Exception) {
                Toast.makeText(this@StaffReportsActivity, "Failed to update: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun exportReportsCsv() {
        if (currentBlotters.isEmpty()) {
            Toast.makeText(this, "No reports available to export", Toast.LENGTH_SHORT).show()
            return
        }

        val csvRows = buildList {
            add("Report Number,Complainant,Respondent,Status,Complaint,Created At")
            currentBlotters.forEach { blotter ->
                add(
                    listOf(
                        blotter.blotterNumber,
                        blotter.complainantName,
                        blotter.respondent,
                        blotter.status,
                        blotter.complaint,
                        blotter.createdAt ?: ""
                    ).joinToString(",") { value ->
                        "\"${value.replace("\"", "\"\"")}\""
                    }
                )
            }
        }.joinToString("\n")

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_SUBJECT, "Barangay Pantal Reports")
            putExtra(Intent.EXTRA_TEXT, csvRows)
        }
        startActivity(Intent.createChooser(shareIntent, "Export reports"))
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
