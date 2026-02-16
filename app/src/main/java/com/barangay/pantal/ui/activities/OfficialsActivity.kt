package com.barangay.pantal.ui.activities

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.barangay.pantal.R
import com.barangay.pantal.databinding.ActivityOfficialsBinding
import com.barangay.pantal.models.Official
import com.barangay.pantal.ui.adapters.OfficialAdapter
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class OfficialsActivity : BaseActivity() {

    private lateinit var binding: ActivityOfficialsBinding
    private lateinit var adapter: OfficialAdapter
    private val officialsList = mutableListOf<Official>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOfficialsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.officialsRecyclerView.layoutManager = LinearLayoutManager(this)
        adapter = OfficialAdapter(officialsList)
        binding.officialsRecyclerView.adapter = adapter

        fetchOfficials()
    }

    private fun fetchOfficials() {
        val database = FirebaseDatabase.getInstance().reference.child("officials")
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                officialsList.clear()
                for (officialSnapshot in snapshot.children) {
                    val official = officialSnapshot.getValue(Official::class.java)
                    if (official != null) {
                        officialsList.add(official)
                    }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }
}