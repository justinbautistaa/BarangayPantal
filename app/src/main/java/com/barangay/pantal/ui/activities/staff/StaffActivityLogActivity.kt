package com.barangay.pantal.ui.activities.staff

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.barangay.pantal.databinding.ActivityStaffActivityLogBinding
import com.barangay.pantal.model.ActivityLog
import com.barangay.pantal.network.SupabaseClient
import com.barangay.pantal.ui.activities.BaseActivity
import com.barangay.pantal.ui.adapters.staff.ActivityLogAdapter
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch

class StaffActivityLogActivity : BaseActivity() {

    private lateinit var binding: ActivityStaffActivityLogBinding
    private lateinit var adapter: ActivityLogAdapter
    private val activityLogList = mutableListOf<ActivityLog>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStaffActivityLogBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        adapter = ActivityLogAdapter(activityLogList)
        binding.activityLogRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.activityLogRecyclerView.adapter = adapter

        setupBottomNavigation(binding.bottomNavigation, View.NO_ID)

        fetchActivityLog()
    }

    private fun fetchActivityLog() {
        lifecycleScope.launch {
            try {
                val result = SupabaseClient.client.postgrest["activity_log"]
                    .select()
                    .decodeList<ActivityLog>()
                
                activityLogList.clear()
                activityLogList.addAll(result.sortedByDescending { it.timestamp })
                adapter.notifyDataSetChanged()
            } catch (e: Exception) {
                Toast.makeText(this@StaffActivityLogActivity, "Error fetching logs: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
