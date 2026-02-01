package com.barangay.pantal.ui.activities

import android.os.Bundle
import com.barangay.pantal.databinding.ActivityViewResidentBinding
import com.barangay.pantal.model.Resident
import com.google.android.material.chip.Chip
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ViewResidentActivity : BaseActivity() {

    private lateinit var binding: ActivityViewResidentBinding
    private var residentId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewResidentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        residentId = intent.getStringExtra("residentId")

        if (residentId != null) {
            loadResidentData()
        }
    }

    private fun loadResidentData() {
        val query = FirebaseDatabase.getInstance().reference.child("residents").child(residentId!!)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val resident = snapshot.getValue(Resident::class.java)
                if (resident != null) {
                    binding.nameTextView.text = resident.name
                    binding.detailsTextView.text = "${resident.age} years • ${resident.gender}"
                    binding.addressTextView.text = resident.address
                    binding.occupationTextView.text = resident.occupation

                    binding.tagsChipGroup.removeAllViews()
                    if (resident.isVoter) {
                        binding.tagsChipGroup.addView(createTagChip("Voter"))
                    }
                    if (resident.isSenior) {
                        binding.tagsChipGroup.addView(createTagChip("Senior"))
                    }
                    if (resident.isPwd) {
                        binding.tagsChipGroup.addView(createTagChip("PWD"))
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    private fun createTagChip(tag: String): Chip {
        return Chip(this).apply {
            text = tag
        }
    }
}
