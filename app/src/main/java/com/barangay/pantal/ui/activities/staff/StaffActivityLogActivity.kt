package com.barangay.pantal.ui.activities.staff

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.barangay.pantal.R
import com.barangay.pantal.databinding.ActivityStaffActivityLogBinding
import com.barangay.pantal.model.FirebaseActivityLog
import com.barangay.pantal.ui.activities.BaseActivity
import com.barangay.pantal.ui.adapters.staff.ActivityLogAdapter
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class StaffActivityLogActivity : BaseActivity() {

    private lateinit var binding: ActivityStaffActivityLogBinding
    private lateinit var adapter: ActivityLogAdapter
    private val activityLogList = mutableListOf<FirebaseActivityLog>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStaffActivityLogBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        adapter = ActivityLogAdapter(activityLogList)
        binding.activityLogRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.activityLogRecyclerView.adapter = adapter

        setupBottomNavigation(binding.bottomNavigation, R.id.navigation_activity_log)

        fetchActivityLog()
    }

    private fun fetchActivityLog() {
        val database = FirebaseDatabase.getInstance().getReference("activity_log")
        database.orderByChild("timestamp").limitToLast(100).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                activityLogList.clear()
                for (activitySnapshot in snapshot.children) {
                    val activityLog = activitySnapshot.getValue(FirebaseActivityLog::class.java)
                    if (activityLog != null) {
                        activityLogList.add(activityLog)
                    }
                }
                activityLogList.reverse()
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }
}
