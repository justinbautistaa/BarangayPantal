package com.barangay.pantal.ui.activities.staff

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.barangay.pantal.R
import com.barangay.pantal.databinding.ActivityStaffResidentsBinding
import com.barangay.pantal.model.Resident
import com.barangay.pantal.network.SupabaseClient
import com.barangay.pantal.ui.activities.BaseActivity
import com.barangay.pantal.ui.adapters.admin.ResidentAdapter
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch

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
            override fun onQueryTextSubmit(query: String?): Boolean = false

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
        lifecycleScope.launch {
            try {
                val result = SupabaseClient.client.postgrest["residents"]
                    .select()
                    .decodeList<Resident>()

                residents.clear()
                residents.addAll(result.sortedBy { it.name.lowercase() })
                filterResidents(binding.searchView.query.toString())
            } catch (e: Exception) {
                Toast.makeText(this@StaffResidentsActivity, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun showDeleteConfirmationDialog(resident: Resident) {
        AlertDialog.Builder(this)
            .setTitle("Delete Resident")
            .setMessage("Are you sure you want to delete this resident?")
            .setPositiveButton("Delete") { _, _ ->
                deleteResident(resident.id)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteResident(id: String) {
        lifecycleScope.launch {
            try {
                SupabaseClient.client.postgrest["residents"].delete {
                    filter {
                        eq("id", id)
                    }
                }
                Toast.makeText(this@StaffResidentsActivity, "Resident deleted", Toast.LENGTH_SHORT).show()
                fetchResidents()
            } catch (e: Exception) {
                Toast.makeText(this@StaffResidentsActivity, "Delete failed: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
