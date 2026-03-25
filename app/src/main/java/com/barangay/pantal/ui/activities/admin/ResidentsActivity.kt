package com.barangay.pantal.ui.activities.admin

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.barangay.pantal.R
import com.barangay.pantal.databinding.ActivityResidentsBinding
import com.barangay.pantal.model.Resident
import com.barangay.pantal.network.SupabaseClient
import com.barangay.pantal.ui.activities.BaseActivity
import com.barangay.pantal.ui.adapters.admin.ResidentAdapter
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch

class ResidentsActivity : BaseActivity() {

    private lateinit var binding: ActivityResidentsBinding
    private lateinit var adapter: ResidentAdapter
    private val residents = mutableListOf<Resident>()
    private val filteredResidents = mutableListOf<Resident>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResidentsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        setupRecyclerView()
        setupSearchView()

        binding.addButton.setOnClickListener {
            startActivity(Intent(this@ResidentsActivity, AddResidentActivity::class.java))
        }

        setupBottomNavigation(binding.bottomNavigation, R.id.navigation_residents)
    }

    override fun onResume() {
        super.onResume()
        fetchResidents()
    }

    private fun setupRecyclerView() {
        adapter = ResidentAdapter(
            filteredResidents.toList(),
            onViewClick = { resident ->
                val intent = Intent(this@ResidentsActivity, ViewResidentActivity::class.java)
                intent.putExtra("residentId", resident.id)
                startActivity(intent)
            },
            onEditClick = { resident ->
                val intent = Intent(this@ResidentsActivity, AddResidentActivity::class.java)
                intent.putExtra("residentId", resident.id)
                startActivity(intent)
            },
            onDeleteClick = { residentId ->
                residents.firstOrNull { it.id == residentId }?.let { resident ->
                    showDeleteConfirmationDialog(resident)
                }
            }
        )

        binding.residentsRecyclerView.layoutManager = LinearLayoutManager(this@ResidentsActivity)
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
        val newFilteredResidents = if (query.isNullOrBlank()) {
            residents
        } else {
            val lowerCaseQuery = query.lowercase()
            residents.filter { it.name.lowercase().contains(lowerCaseQuery) }
        }

        filteredResidents.clear()
        filteredResidents.addAll(newFilteredResidents)
        adapter.updateList(filteredResidents.toList())
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
                filterResidents(binding.searchView.query?.toString())

            } catch (e: Exception) {
                Toast.makeText(
                    this@ResidentsActivity,
                    getString(R.string.error_general, e.localizedMessage ?: "Unknown error"),
                    Toast.LENGTH_SHORT
                ).show()
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun showDeleteConfirmationDialog(resident: Resident) {
        AlertDialog.Builder(this@ResidentsActivity)
            .setTitle(getString(R.string.dialog_delete_resident_title))
            .setMessage(getString(R.string.dialog_delete_resident_message, resident.name))
            .setPositiveButton(getString(R.string.delete)) { _, _ ->
                deleteResident(resident.id)
            }
            .setNegativeButton(getString(R.string.cancel), null)
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

                Toast.makeText(
                    this@ResidentsActivity,
                    getString(R.string.resident_deleted),
                    Toast.LENGTH_SHORT
                ).show()

                fetchResidents()
            } catch (e: Exception) {
                Toast.makeText(
                    this@ResidentsActivity,
                    getString(R.string.delete_failed, e.localizedMessage ?: "Unknown error"),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
