package com.barangay.pantal.ui.activities.user

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.barangay.pantal.R
import com.barangay.pantal.databinding.ActivityReportsBinding
import com.barangay.pantal.model.Blotter
import com.barangay.pantal.model.Report
import com.barangay.pantal.model.reportToBlotter
import com.barangay.pantal.network.SupabaseClient
import com.barangay.pantal.ui.activities.BaseActivity
import com.barangay.pantal.ui.activities.admin.AddReportActivity
import com.barangay.pantal.ui.adapters.common.BlotterAdapter
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch

class UserReportsActivity : BaseActivity() {

    private lateinit var binding: ActivityReportsBinding
    private lateinit var adapter: BlotterAdapter
    private var currentBlotters: List<Blotter> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReportsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.title = "My Reports"
        binding.toolbar.setNavigationOnClickListener { finish() }

        adapter = BlotterAdapter(emptyList()) { blotter -> showBlotterDetails(blotter) }
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
        menu?.findItem(R.id.action_export_reports)?.isVisible = false
        menu?.findItem(R.id.action_new_report)?.isVisible = true
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_new_report -> {
                startActivity(Intent(this, AddReportActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun fetchBlotters() {
        val authUser = SupabaseClient.client.auth.currentUserOrNull()
        if (authUser == null) {
            Toast.makeText(this, "You must be logged in to view reports", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val result = SupabaseClient.client.postgrest["reports"]
                    .select {
                        filter { eq("reporter_id", authUser.id) }
                    }
                    .decodeList<Report>()
                currentBlotters = result.map(::reportToBlotter).sortedByDescending { it.createdAt ?: "" }
                adapter.updateData(currentBlotters)
            } catch (e: Exception) {
                Toast.makeText(this@UserReportsActivity, "Error fetching reports: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showBlotterDetails(blotter: Blotter) {
        val message = buildString {
            appendLine("Subject: ${blotter.respondent.ifBlank { "General concern" }}")
            appendLine("Status: ${blotter.status}")
            appendLine()
            append(blotter.complaint)
        }

        AlertDialog.Builder(this)
            .setTitle("Report #${blotter.blotterNumber.ifBlank { blotter.id.take(8) }}")
            .setMessage(message)
            .setPositiveButton("Close", null)
            .show()
    }
}
