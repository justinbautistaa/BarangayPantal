package com.barangay.pantal.ui.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.barangay.pantal.R
import com.barangay.pantal.databinding.ActivityManageRequestsBinding
import com.barangay.pantal.model.RequestAdmin
import com.barangay.pantal.ui.adapters.RequestsAdminAdapter
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ManageRequestsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityManageRequestsBinding
    private lateinit var adapter: RequestsAdminAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManageRequestsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = RequestsAdminAdapter(
            onApproveClick = { request -> updateRequestStatus(request, "Approved") },
            onRejectClick = { request -> updateRequestStatus(request, "Rejected") }
        )
        binding.requestsRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.requestsRecyclerView.adapter = adapter

        fetchRequests()
    }

    private fun fetchRequests() {
        val database = FirebaseDatabase.getInstance().getReference("requests")
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val requests = mutableListOf<RequestAdmin>()
                for (requestSnapshot in snapshot.children) {
                    val request = requestSnapshot.getValue(RequestAdmin::class.java)
                    if (request != null) {
                        requests.add(request.copy(key = requestSnapshot.key!!))
                    }
                }
                adapter.submitList(requests.sortedByDescending { it.timestamp })
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    private fun updateRequestStatus(request: RequestAdmin, status: String) {
        val database = FirebaseDatabase.getInstance().getReference("requests")
        database.child(request.key).child("status").setValue(status)
    }
}