package com.barangay.pantal.ui.activities.admin

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.barangay.pantal.R
import com.barangay.pantal.databinding.ActivityStaffManagementBinding
import com.barangay.pantal.model.User
import com.barangay.pantal.ui.activities.BaseActivity
import com.barangay.pantal.ui.activities.auth.SignupActivity
import com.barangay.pantal.ui.adapters.admin.StaffAdapter
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class StaffManagementActivity : BaseActivity() {

    private lateinit var binding: ActivityStaffManagementBinding
    private lateinit var staffAdapter: StaffAdapter
    private val database = FirebaseDatabase.getInstance().getReference("users")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStaffManagementBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        setupBottomNavigation(binding.bottomNavigation, R.id.navigation_staff_management)

        setupRecyclerView()
        loadStaff()

        binding.fabAddStaff.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }
    }

    private fun setupRecyclerView() {
        staffAdapter = StaffAdapter(emptyList()) { staff ->
            deleteStaff(staff)
        }
        binding.rvStaff.apply {
            layoutManager = LinearLayoutManager(this@StaffManagementActivity)
            adapter = staffAdapter
        }
    }

    private fun loadStaff() {
        database.orderByChild("role").equalTo("staff")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val staffList = mutableListOf<User>()
                    for (staffSnapshot in snapshot.children) {
                        val staff = staffSnapshot.getValue(User::class.java)
                        staff?.let { staffList.add(it) }
                    }
                    staffAdapter.updateList(staffList)
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@StaffManagementActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun deleteStaff(staff: User) {
        // Find user by email and delete
        database.orderByChild("email").equalTo(staff.email)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (staffSnapshot in snapshot.children) {
                        staffSnapshot.ref.removeValue().addOnSuccessListener {
                            Toast.makeText(this@StaffManagementActivity, "Staff deleted", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@StaffManagementActivity, "Delete failed", Toast.LENGTH_SHORT).show()
                }
            })
    }
}
