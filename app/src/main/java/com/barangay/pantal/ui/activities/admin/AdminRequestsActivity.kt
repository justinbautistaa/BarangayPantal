package com.barangay.pantal.ui.activities.admin

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.barangay.pantal.R
import com.barangay.pantal.databinding.ActivityAdminRequestsBinding
import com.barangay.pantal.model.RequestAdmin
import com.barangay.pantal.network.SupabaseClient
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

class AdminRequestsActivity : BaseActivity() {

    private lateinit var binding: ActivityAdminRequestsBinding
    private lateinit var adapter: RequestsAdminAdapter
    private val allRequests = mutableListOf<RequestAdmin>()
    private val requestsChannel by lazy { SupabaseClient.client.realtime.channel("admin_requests_channel") }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminRequestsBinding.inflate(layoutInflater)
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
    }

    private fun filterRequests(status: String) {
        val filtered = if (status == "All") {
            allRequests
        } else {
            allRequests.filter { it.status == status }
        }
        adapter.submitList(filtered)
    }

    private fun fetchRequests() {
        lifecycleScope.launch {
            try {
                val result = SupabaseClient.client.postgrest["requests"]
                    .select()
                    .decodeList<RequestAdmin>()
                
                allRequests.clear()
                allRequests.addAll(result.sortedByDescending { it.timestamp })
                filterRequests("All")
            } catch (e: Exception) {
                Toast.makeText(this@AdminRequestsActivity, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupRealtimeSync() {
        val changeFlow = requestsChannel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = "requests"
        }

        changeFlow.onEach {
            Log.d("AdminRequests", "Requests table changed: $it")
            fetchRequests()
        }.launchIn(lifecycleScope)

        lifecycleScope.launch {
            requestsChannel.subscribe()
        }
    }

    private fun approveRequest(request: RequestAdmin) {
        startActivity(Intent(this, PrintCertificateActivity::class.java).putExtra("request", request))
    }

    private fun updateRequestStatus(request: RequestAdmin, newStatus: String) {
        lifecycleScope.launch {
            try {
                SupabaseClient.client.postgrest["requests"].update({
                    set("status", newStatus)
                    if (newStatus == "Rejected") {
                        set("pdf_url", null as String?)
                    }
                }) {
                    filter {
                        eq("id", request.id)
                    }
                }
                Toast.makeText(this@AdminRequestsActivity, "Request $newStatus", Toast.LENGTH_SHORT).show()
                fetchRequests()
            } catch (e: Exception) {
                Toast.makeText(this@AdminRequestsActivity, "Update failed: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openCertificatePreview(request: RequestAdmin) {
        startActivity(Intent(this, PrintCertificateActivity::class.java).putExtra("request", request))
    }
}
