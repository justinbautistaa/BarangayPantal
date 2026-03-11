package com.barangay.pantal.ui.activities.admin

import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.barangay.pantal.R
import com.barangay.pantal.databinding.ActivityStaffManagementBinding
import com.barangay.pantal.model.User
import com.barangay.pantal.network.SupabaseClient
import com.barangay.pantal.ui.activities.BaseActivity
import com.barangay.pantal.ui.adapters.admin.StaffAdapter
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch

class StaffManagementActivity : BaseActivity() {

    private lateinit var binding: ActivityStaffManagementBinding
    private lateinit var adapter: StaffAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStaffManagementBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        adapter = StaffAdapter(emptyList()) { user ->
            removeStaffRole(user)
        }
        binding.staffRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.staffRecyclerView.adapter = adapter

        fetchStaffMembers()
    }

    private fun fetchStaffMembers() {
        lifecycleScope.launch {
            try {
                val result = SupabaseClient.client.postgrest["users"]
                    .select { filter { eq("role", "staff") } }
                    .decodeList<User>()
                (binding.staffRecyclerView.adapter as StaffAdapter).updateData(result)
            } catch (e: Exception) {
                Toast.makeText(this@StaffManagementActivity, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun removeStaffRole(user: User) {
        lifecycleScope.launch {
            try {
                SupabaseClient.client.postgrest["users"].update({
                    set("role", "user")
                }) {
                    filter { eq("id", user.id!!) }
                }
                Toast.makeText(this@StaffManagementActivity, "Staff role removed", Toast.LENGTH_SHORT).show()
                fetchStaffMembers()
            } catch (e: Exception) {
                Toast.makeText(this@StaffManagementActivity, "Failed: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
