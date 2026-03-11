package com.barangay.pantal.ui.activities.admin

import android.os.Bundle
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
import kotlinx.coroutines.launch

class AdminRequestsActivity : BaseActivity() {

    private lateinit var binding: ActivityAdminRequestsBinding
    private lateinit var adapter: RequestsAdminAdapter
    private val allRequests = mutableListOf<RequestAdmin>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminRequestsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)

        setupRecyclerView()
        setupFilters()
        fetchRequests()

        setupBottomNavigation(binding.bottomNavigation, R.id.navigation_requests)
    }

    private fun setupRecyclerView() {
        adapter = RequestsAdminAdapter(
            onApproveClick = { request -> updateRequestStatus(request, "Approved") },
            onRejectClick = { request -> updateRequestStatus(request, "Rejected") }
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

    private fun updateRequestStatus(request: RequestAdmin, newStatus: String) {
        lifecycleScope.launch {
            try {
                SupabaseClient.client.postgrest["requests"].update({
                    set("status", newStatus)
                }) {
                    filter {
                        eq("key", request.key)
                    }
                }
                Toast.makeText(this@AdminRequestsActivity, "Status updated to $newStatus", Toast.LENGTH_SHORT).show()
                fetchRequests()
            } catch (e: Exception) {
                Toast.makeText(this@AdminRequestsActivity, "Update failed: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
