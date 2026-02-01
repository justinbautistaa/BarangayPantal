package com.barangay.pantal.ui.activities

import android.os.Bundle
import android.widget.Toast
import com.barangay.pantal.databinding.ActivityAddHouseholdBinding
import com.barangay.pantal.model.Household
import com.google.firebase.database.FirebaseDatabase

class AddHouseholdActivity : BaseActivity() {

    private lateinit var binding: ActivityAddHouseholdBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddHouseholdBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.saveHouseholdButton.setOnClickListener {
            saveHousehold()
        }
    }

    private fun saveHousehold() {
        val householdName = binding.householdNameEditText.text.toString().trim()

        if (householdName.isEmpty()) {
            Toast.makeText(this, "Please enter a household name", Toast.LENGTH_SHORT).show()
            return
        }

        val database = FirebaseDatabase.getInstance().reference.child("households")
        val householdId = database.push().key

        if (householdId != null) {
            val household = Household(householdId, householdName)
            database.child(householdId).setValue(household).addOnCompleteListener {
                if (it.isSuccessful) {
                    Toast.makeText(this, "Household added successfully", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "Failed to add household", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(this, "Failed to generate a unique ID for the household", Toast.LENGTH_SHORT).show()
        }
    }
}
