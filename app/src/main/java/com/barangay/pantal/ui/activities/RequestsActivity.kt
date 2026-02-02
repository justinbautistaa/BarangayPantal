package com.barangay.pantal.ui.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.barangay.pantal.R
import com.barangay.pantal.databinding.ActivityRequestsBinding
import com.barangay.pantal.model.Request
import com.barangay.pantal.ui.adapters.RequestAdapter
import com.google.android.material.chip.Chip
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class RequestsActivity : BaseActivity(), RequestAdapter.OnRequestInteractionListener {

    private lateinit var binding: ActivityRequestsBinding
    private lateinit var adapter: RequestAdapter
    private lateinit var auth: FirebaseAuth
    private val requests = mutableListOf<Request>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPref = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val userRole = sharedPref.getString("user_role", "user")

        if (userRole == "admin") {
            startActivity(Intent(this, AdminRequestsActivity::class.java))
            finish()
            return
        }

        binding = ActivityRequestsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        adapter = RequestAdapter(this, mutableListOf(), this, false)
        binding.rvRequests.layoutManager = LinearLayoutManager(this)
        binding.rvRequests.adapter = adapter

        binding.fabCreateRequest.setOnClickListener {
            startActivity(Intent(this, CreateRequestActivity::class.java))
        }

        binding.requestFilterChipGroup.setOnCheckedChangeListener { group, checkedId ->
            val chip = group.findViewById<Chip>(checkedId)
            if(chip != null) {
                filterRequests(chip.text.toString())
            } else {
                filterRequests("All")
            }
        }

        binding.bottomNavigation.selectedItemId = R.id.navigation_requests

        binding.bottomNavigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_dashboard -> {
                    val intent = Intent(this, UserDashboardActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    startActivity(intent)
                    true
                }
                R.id.navigation_announcements -> {
                    startActivity(Intent(this, AnnouncementsActivity::class.java))
                    true
                }
                R.id.navigation_requests -> {
                    true
                }
                R.id.navigation_services -> {
                    startActivity(Intent(this, ServicesActivity::class.java))
                    true
                }
                else -> false
            }
        }

        fetchRequests()
    }

    private fun fetchRequests() {
        val user = auth.currentUser
        if (user != null) {
            val database = FirebaseDatabase.getInstance().getReference("requests")
            database.orderByChild("userId").equalTo(user.uid)
                .addValueEventListener(object : ValueEventListener {
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
        // Not used in this activity
    }

    override fun onRejectRequest(request: Request) {
        // Not used in this activity
    }
}