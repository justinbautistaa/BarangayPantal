package com.barangay.pantal.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.barangay.pantal.R
import com.barangay.pantal.databinding.ActivityAdminRequestsBinding
import com.barangay.pantal.model.Request
import com.barangay.pantal.ui.adapters.RequestAdapter
import com.google.android.material.chip.Chip
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AdminRequestsActivity : BaseActivity(), RequestAdapter.OnRequestInteractionListener {

    private lateinit var binding: ActivityAdminRequestsBinding
    private lateinit var adapter: RequestAdapter
    private val requests = mutableListOf<Request>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminRequestsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = RequestAdapter(this, mutableListOf(), this, true)
        binding.rvRequests.layoutManager = LinearLayoutManager(this)
        binding.rvRequests.adapter = adapter

        binding.requestFilterChipGroup.setOnCheckedChangeListener { group, checkedId ->
            val chip = group.findViewById<Chip>(checkedId)
            if(chip != null) {
                filterRequests(chip.text.toString())
            } else {
                filterRequests("All")
            }
        }

        setupBottomNavigation(binding.bottomNavigation, R.id.navigation_requests)

        fetchRequests()
    }

    private fun fetchRequests() {
        val database = FirebaseDatabase.getInstance().getReference("requests")
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                requests.clear()
                for (requestSnapshot in snapshot.children) {
                    val request = requestSnapshot.getValue(Request::class.java)
                    if (request != null) {
                        requests.add(request)
                    }
                }
                binding.chipAll.isChecked = true
                filterRequests("All")
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    private fun filterRequests(status: String) {
        val filteredList = if (status.equals("All", ignoreCase = true)) {
            requests
        } else {
            requests.filter { it.status.equals(status, ignoreCase = true) }
        }
        adapter.updateList(filteredList)
    }

    override fun onViewRequest(request: Request) {
        // Handle view request
    }

    override fun onApproveRequest(request: Request) {
        updateRequestStatus(request, "Approved")
    }

    override fun onRejectRequest(request: Request) {
        updateRequestStatus(request, "Rejected")
    }

    private fun updateRequestStatus(request: Request, status: String) {
        val database = FirebaseDatabase.getInstance().getReference("requests")
        database.child(request.id).child("status").setValue(status)
    }
}