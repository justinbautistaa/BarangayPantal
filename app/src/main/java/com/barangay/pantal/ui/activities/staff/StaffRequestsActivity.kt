package com.barangay.pantal.ui.activities.staff

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.barangay.pantal.R
import com.barangay.pantal.databinding.ActivityStaffRequestsBinding
import com.barangay.pantal.model.RequestAdmin
import com.barangay.pantal.network.SupabaseClient
import com.barangay.pantal.ui.activities.admin.PrintCertificateActivity
import com.barangay.pantal.ui.activities.BaseActivity
import com.barangay.pantal.ui.adapters.RequestsAdminAdapter
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class StaffRequestsActivity : BaseActivity() {

    private lateinit var binding: ActivityStaffRequestsBinding
    private lateinit var adapter: RequestsAdminAdapter
    private val allRequests = mutableListOf<RequestAdmin>()
    private val requestsChannel by lazy { SupabaseClient.client.realtime.channel("staff_requests_channel") }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStaffRequestsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        setupRecyclerView()
        setupFilters()
        fetchRequests()
        setupRealtimeSync()
        setupBottomNavigation(binding.bottomNavigation, R.id.navigation_requests)
    }

    override fun onResume() {
        super.onResume()
        fetchRequests()
    }

    private fun setupRecyclerView() {
        adapter = RequestsAdminAdapter(
            onApproveClick = { request -> approveRequest(request) },
            onRejectClick = { request -> updateRequestStatus(request, "Rejected") },
            onViewClick = { request -> openCertificatePreview(request) }
        )
        binding.rvRequests.layoutManager = LinearLayoutManager(this)
        binding.rvRequests.adapter = adapter
    }

    private fun setupFilters() {
        binding.requestFilterChipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            val checkedId = checkedIds.firstOrNull()
            val status = when (checkedId) {
                R.id.chipPending -> "Pending"
                R.id.chipApproved -> "Approved"
                R.id.chipRejected -> "Rejected"
                else -> "All"
            }
            filterRequests(status)
        }
        binding.chipAll.isChecked = true
    }

    private fun filterRequests(status: String) {
        val filtered = if (status == "All") {
            allRequests
        } else {
            allRequests.filter { it.status.equals(status, ignoreCase = true) }
        }
        adapter.submitList(filtered)
        binding.emptyStateText.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
        binding.rvRequests.visibility = if (filtered.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun fetchRequests() {
        lifecycleScope.launch {
            try {
                val result = SupabaseClient.client.postgrest["requests"]
                    .select()
                    .decodeList<RequestAdmin>()

                allRequests.clear()
                allRequests.addAll(result.sortedByDescending { it.timestamp ?: 0L })

                val selectedStatus = when (binding.requestFilterChipGroup.checkedChipId) {
                    R.id.chipPending -> "Pending"
                    R.id.chipApproved -> "Approved"
                    R.id.chipRejected -> "Rejected"
                    else -> "All"
                }
                filterRequests(selectedStatus)
            } catch (e: Exception) {
                Toast.makeText(this@StaffRequestsActivity, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupRealtimeSync() {
        val changeFlow = requestsChannel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = "requests"
        }

        changeFlow.onEach {
            Log.d("StaffRequests", "Change detected in requests table: $it")
            fetchRequests()
        }.launchIn(lifecycleScope)

        lifecycleScope.launch {
            requestsChannel.subscribe()
        }
    }

    private fun updateRequestStatus(request: RequestAdmin, status: String) {
        lifecycleScope.launch {
            try {
                SupabaseClient.client.postgrest["requests"].update({
                    set("status", status)
                    if (status == "Rejected") {
                        set("pdf_url", null as String?)
                    }
                }) {
                    filter {
                        eq("id", request.id) 
                    }
                }
                Toast.makeText(this@StaffRequestsActivity, "Status updated to $status", Toast.LENGTH_SHORT).show()
                fetchRequests()
            } catch (e: Exception) {
                Toast.makeText(this@StaffRequestsActivity, "Failed: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun approveRequest(request: RequestAdmin) {
        startActivity(Intent(this, PrintCertificateActivity::class.java).putExtra("request", request))
    }

    private fun openCertificatePreview(request: RequestAdmin) {
        startActivity(
            Intent(this, PrintCertificateActivity::class.java)
                .putExtra("request", request)
                .putExtra("view_only", true)
        )
    }
}
