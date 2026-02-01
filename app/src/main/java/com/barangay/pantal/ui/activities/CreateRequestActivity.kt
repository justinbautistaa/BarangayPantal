package com.barangay.pantal.ui.activities

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import com.barangay.pantal.databinding.ActivityCreateRequestBinding
import com.barangay.pantal.model.Request
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CreateRequestActivity : BaseActivity() {

    private lateinit var binding: ActivityCreateRequestBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateRequestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        val requestTypes = arrayOf("Certificate of Residency", "Barangay Clearance", "Business Permit")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, requestTypes)
        binding.requestTypeAutoComplete.setAdapter(adapter)

        binding.submitRequestButton.setOnClickListener {
            submitRequest()
        }
    }

    private fun submitRequest() {
        val requestType = binding.requestTypeAutoComplete.text.toString()
        val purpose = binding.purposeEditText.text.toString().trim()

        if (requestType.isEmpty()) {
            binding.requestTypeAutoComplete.error = "Request type is required"
            binding.requestTypeAutoComplete.requestFocus()
            return
        }

        if (purpose.isEmpty()) {
            binding.purposeEditText.error = "Purpose is required"
            binding.purposeEditText.requestFocus()
            return
        }

        val user = auth.currentUser
        if (user != null) {
            val userDatabase = FirebaseDatabase.getInstance().reference.child("users").child(user.uid)
            userDatabase.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val fullName = snapshot.child("fullName").getValue(String::class.java)
                    if (fullName != null) {
                        val database = FirebaseDatabase.getInstance().getReference("requests")
                        val requestId = database.push().key
                        if (requestId != null) {
                            val sdf = SimpleDateFormat("MM/dd/yyyy", Locale.US)
                            val currentDate = sdf.format(Date())
                            val request = Request(
                                id = requestId,
                                userId = user.uid,
                                name = fullName,
                                type = requestType,
                                date = currentDate,
                                purpose = purpose,
                                status = "Pending",
                                timestamp = System.currentTimeMillis()
                            )
                            database.child(requestId).setValue(request).addOnCompleteListener {
                                if (it.isSuccessful) {
                                    Toast.makeText(this@CreateRequestActivity, "Request submitted successfully", Toast.LENGTH_SHORT).show()
                                    finish()
                                } else {
                                    Toast.makeText(this@CreateRequestActivity, "Failed to submit request: ${it.exception?.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@CreateRequestActivity, "Failed to get user name: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}