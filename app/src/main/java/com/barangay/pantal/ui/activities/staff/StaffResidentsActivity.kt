package com.barangay.pantal.ui.activities.staff

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.barangay.pantal.R
import com.barangay.pantal.databinding.ActivityStaffResidentsBinding
import com.barangay.pantal.model.Resident
import com.barangay.pantal.ui.activities.BaseActivity
import com.barangay.pantal.ui.adapters.admin.ResidentAdapter
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class StaffResidentsActivity : BaseActivity() {

    private lateinit var binding: ActivityStaffResidentsBinding
    private lateinit var adapter: ResidentAdapter
    private val residents = mutableListOf<Resident>()
    private val filteredResidents = mutableListOf<Resident>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStaffResidentsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)

        setupRecyclerView()
        setupSearchView()
        fetchResidents()

        binding.addButton.setOnClickListener {
            startActivity(Intent(this, StaffAddResidentActivity::class.java))
        }

        setupBottomNavigation(binding.bottomNavigation, R.id.navigation_residents)
    }

    private fun setupRecyclerView() {
        adapter = ResidentAdapter(filteredResidents, {
            val intent = Intent(this, StaffViewResidentActivity::class.java)
            intent.putExtra("residentId", it.id)
            startActivity(intent)
        }, {
            val intent = Intent(this, StaffAddResidentActivity::class.java)
            intent.putExtra("residentId", it.id)
            startActivity(intent)
        }, { residentId ->
            residents.firstOrNull { it.id == residentId }?.let {
                showDeleteConfirmationDialog(it)
            }
        })

        binding.residentsRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.residentsRecyclerView.adapter = adapter
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterResidents(newText)
                return true
            }
        })
    }

    private fun filterResidents(query: String?) {
        val newFilteredResidents = if (query.isNullOrEmpty()) {
            residents
        } else {
            val lowerCaseQuery = query.lowercase()
            residents.filter { it.name.lowercase().contains(lowerCaseQuery) }
        }

        filteredResidents.clear()
        filteredResidents.addAll(newFilteredResidents)
        adapter.notifyDataSetChanged()
    }

    private fun fetchResidents() {
        binding.progressBar.visibility = View.VISIBLE
        val query = FirebaseDatabase.getInstance().reference.child("residents")
        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                residents.clear()
                for (data in snapshot.children) {
                    val resident = data.getValue(Resident::class.java)
                    if (resident != null) {
                        residents.add(resident)
                    }
                }
                filterResidents(binding.searchView.query.toString())
                binding.progressBar.visibility = View.GONE
            }



            override fun onCancelled(error: DatabaseError) {
                binding.progressBar.visibility = View.GONE
                // Handle error
            }
        })
    }

    private fun showDeleteConfirmationDialog(resident: Resident) {
        AlertDialog.Builder(this)
            .setTitle("Delete Resident")
            .setMessage("Are you sure you want to delete this resident?")
            .setPositiveButton("Delete") { _, _ ->
                FirebaseDatabase.getInstance().reference.child("residents").child(resident.id).removeValue()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}