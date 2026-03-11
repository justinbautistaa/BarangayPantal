package com.barangay.pantal.ui.activities.admin

import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.barangay.pantal.databinding.ActivityManageRequestsBinding
import com.barangay.pantal.model.RequestAdmin
import com.barangay.pantal.network.SupabaseClient
import com.barangay.pantal.ui.activities.BaseActivity
import com.barangay.pantal.ui.adapters.RequestsAdminAdapter
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch

class ManageRequestsActivity : BaseActivity() {

    private lateinit var binding: ActivityManageRequestsBinding
    private lateinit var adapter: RequestsAdminAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManageRequestsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.root.findViewById(com.barangay.pantal.R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        adapter = RequestsAdminAdapter(
            onApproveClick = { request -> updateRequestStatus(request, "Approved") },
            onRejectClick = { request -> updateRequestStatus(request, "Rejected") }
        )
        binding.requestsRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.requestsRecyclerView.adapter = adapter

        fetchRequests()
    }

    private fun fetchRequests() {
        lifecycleScope.launch {
            try {
                val result = SupabaseClient.client.postgrest["requests"]
                    .select()
                    .decodeList<RequestAdmin>()
                adapter.submitList(result.sortedByDescending { it.timestamp })
            } catch (e: Exception) {
                Toast.makeText(this@ManageRequestsActivity, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateRequestStatus(request: RequestAdmin, status: String) {
        lifecycleScope.launch {
            try {
                SupabaseClient.client.postgrest["requests"].update({
                    set("status", status)
                }) {
                    filter {
                        eq("key", request.key)
                    }
                }
                Toast.makeText(this@ManageRequestsActivity, "Status updated", Toast.LENGTH_SHORT).show()
                fetchRequests()
            } catch (e: Exception) {
                Toast.makeText(this@ManageRequestsActivity, "Failed: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
