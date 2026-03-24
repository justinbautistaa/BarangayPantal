package com.barangay.pantal.ui.activities.admin

import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.barangay.pantal.R
import com.barangay.pantal.databinding.ActivityManageReportsBinding
import com.barangay.pantal.model.Report
import com.barangay.pantal.network.SupabaseClient
import com.barangay.pantal.ui.activities.BaseActivity
import com.barangay.pantal.ui.adapters.admin.ReportAdapter
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch

class ManageReportsActivity : BaseActivity() {

    private lateinit var binding: ActivityManageReportsBinding
    private lateinit var adapter: ReportAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManageReportsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // If you decide to add a toolbar to the XML later, uncomment these:
        // setSupportActionBar(binding.toolbar)
        // supportActionBar?.setDisplayHomeAsUpEnabled(true)
        // supportActionBar?.title = getString(R.string.title_reports)

        adapter = ReportAdapter(emptyList())
        binding.reportsRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.reportsRecyclerView.adapter = adapter

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
                Toast.makeText(this@ManageReportsActivity, getString(R.string.error_fetching_reports, e.localizedMessage), Toast.LENGTH_SHORT).show()
            }
        }
    }
}
