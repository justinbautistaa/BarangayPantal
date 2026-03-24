package com.barangay.pantal.ui.activities.admin

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
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
        binding.toolbar.setNavigationOnClickListener { finish() }

        adapter = StaffAdapter(emptyList()) { user ->
            removeStaffRole(user)
        }
        binding.rvStaff.layoutManager = LinearLayoutManager(this)
        binding.rvStaff.adapter = adapter

        binding.fabAddStaff.setOnClickListener {
            showAddStaffDialog()
        }

        fetchStaffMembers()
    }

    override fun onResume() {
        super.onResume()
        fetchStaffMembers()
    }

    private fun fetchStaffMembers() {
        lifecycleScope.launch {
            try {
                val result = SupabaseClient.client.postgrest["users"]
                    .select { filter { eq("role", "staff") } }
                    .decodeList<User>()
                (binding.rvStaff.adapter as StaffAdapter).updateData(result)
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

    private fun showAddStaffDialog() {
        lifecycleScope.launch {
            try {
                val candidates = SupabaseClient.client.postgrest["users"]
                    .select()
                    .decodeList<User>()
                    .filter { it.id != null && it.role.lowercase() == "user" }
                    .sortedBy { it.fullName }

                if (candidates.isEmpty()) {
                    Toast.makeText(
                        this@StaffManagementActivity,
                        "No regular users available to promote.",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@launch
                }

                val labels = candidates.map { "${it.fullName} (${it.email})" }
                val input = AutoCompleteTextView(this@StaffManagementActivity).apply {
                    setAdapter(
                        ArrayAdapter(
                            this@StaffManagementActivity,
                            android.R.layout.simple_dropdown_item_1line,
                            labels
                        )
                    )
                    hint = "Select user to promote"
                    setText(labels.first(), false)
                }

                MaterialAlertDialogBuilder(this@StaffManagementActivity)
                    .setTitle("Add Staff")
                    .setView(input)
                    .setNegativeButton("Cancel", null)
                    .setPositiveButton("Promote") { _, _ ->
                        val selectedIndex = labels.indexOf(input.text.toString())
                        val selectedUser = candidates.getOrNull(selectedIndex)
                        if (selectedUser == null) {
                            Toast.makeText(
                                this@StaffManagementActivity,
                                "Please select a valid user.",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            promoteToStaff(selectedUser)
                        }
                    }
                    .show()
            } catch (e: Exception) {
                Toast.makeText(
                    this@StaffManagementActivity,
                    "Failed to load users: ${e.localizedMessage}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun promoteToStaff(user: User) {
        lifecycleScope.launch {
            try {
                SupabaseClient.client.postgrest["users"].update({
                    set("role", "staff")
                }) {
                    filter { eq("id", user.id!!) }
                }
                Toast.makeText(
                    this@StaffManagementActivity,
                    "${user.fullName} is now a staff member.",
                    Toast.LENGTH_SHORT
                ).show()
                fetchStaffMembers()
            } catch (e: Exception) {
                Toast.makeText(
                    this@StaffManagementActivity,
                    "Failed to promote user: ${e.localizedMessage}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
