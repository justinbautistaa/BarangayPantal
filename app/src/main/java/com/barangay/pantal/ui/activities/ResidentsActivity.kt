package com.barangay.pantal.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.barangay.pantal.R
import com.barangay.pantal.databinding.ActivityResidentsBinding
import com.barangay.pantal.model.Resident
import com.barangay.pantal.ui.adapters.ResidentAdapter
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

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
        supportActionBar?.setDisplayHomeAsUpEnabled(false)

        setupRecyclerView()
        setupSearchView()
        fetchResidents()

        binding.addButton.setOnClickListener {
            startActivity(Intent(this, AddResidentActivity::class.java))
        }

        setupBottomNavigation(binding.bottomNavigation, R.id.navigation_residents)
    }

    private fun setupRecyclerView() {
        adapter = ResidentAdapter(filteredResidents, {
            val intent = Intent(this, ViewResidentActivity::class.java)
            intent.putExtra("residentId", it.id)
            startActivity(intent)
        }, {
            val intent = Intent(this, AddResidentActivity::class.java)
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
        filteredResidents.clear()
        if (query.isNullOrEmpty()) {
            filteredResidents.addAll(residents)
        } else {
            val lowerCaseQuery = query.lowercase()
            for (resident in residents) {
                if (resident.name.lowercase().contains(lowerCaseQuery)) {
                    filteredResidents.add(resident)
                }
            }
        }
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