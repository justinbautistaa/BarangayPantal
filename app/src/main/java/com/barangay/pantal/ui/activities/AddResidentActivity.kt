package com.barangay.pantal.ui.activities

import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import com.barangay.pantal.databinding.ActivityAddResidentBinding
import com.barangay.pantal.model.Resident
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AddResidentActivity : BaseActivity() {

    private lateinit var binding: ActivityAddResidentBinding
    private var residentId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddResidentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        residentId = intent.getStringExtra("residentId")

        if (residentId != null) {
            loadResidentData()
        }

        binding.saveButton.setOnClickListener {
            saveResident()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun loadResidentData() {
        val query = FirebaseDatabase.getInstance().reference.child("residents").child(residentId!!)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val resident = snapshot.getValue(Resident::class.java)
                if (resident != null) {
                    binding.nameEditText.setText(resident.name)
                    binding.ageEditText.setText(resident.age.toString())
                    binding.genderEditText.setText(resident.gender)
                    binding.addressEditText.setText(resident.address)
                    binding.occupationEditText.setText(resident.occupation)
                    binding.voterSwitch.isChecked = resident.isVoter
                    binding.seniorSwitch.isChecked = resident.isSenior
                    binding.pwdSwitch.isChecked = resident.isPwd
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    private fun saveResident() {
        val name = binding.nameEditText.text.toString().trim()
        val age = binding.ageEditText.text.toString().trim().toIntOrNull() ?: 0
        val gender = binding.genderEditText.text.toString().trim()
        val address = binding.addressEditText.text.toString().trim()
        val occupation = binding.occupationEditText.text.toString().trim()
        val isVoter = binding.voterSwitch.isChecked
        val isSenior = binding.seniorSwitch.isChecked
        val isPwd = binding.pwdSwitch.isChecked

        if (name.isEmpty() || gender.isEmpty() || address.isEmpty() || occupation.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val database = FirebaseDatabase.getInstance().reference.child("residents")
        val currentResidentId = residentId ?: database.push().key!!

        val resident = Resident(currentResidentId, name, age, gender, address, occupation, isVoter, isSenior, isPwd)
        database.child(currentResidentId).setValue(resident).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Resident saved successfully", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Failed to save resident", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
