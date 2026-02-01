package com.barangay.pantal.ui.activities

import android.os.Bundle
import android.widget.Toast
import com.barangay.pantal.databinding.ActivityRequestServiceBinding
import com.barangay.pantal.model.Request
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class RequestServiceActivity : BaseActivity() {

    private lateinit var binding: ActivityRequestServiceBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRequestServiceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        val serviceName = intent.getStringExtra("serviceName")
        binding.serviceNameTextView.text = serviceName

        binding.submitRequestButton.setOnClickListener {
            submitRequest(serviceName)
        }
    }

    private fun submitRequest(serviceName: String?) {
        val purpose = binding.purposeEditText.text.toString().trim()
        val user = auth.currentUser

        if (purpose.isEmpty()) {
            Toast.makeText(this, "Please enter the purpose of your request", Toast.LENGTH_SHORT).show()
            return
        }

        if (user != null && serviceName != null) {
            val database = FirebaseDatabase.getInstance().reference.child("requests")
            val requestId = database.push().key

            if (requestId != null) {
                val request = Request(
                    id = requestId,
                    userId = user.uid,
                    name = user.displayName ?: "",
                    type = serviceName,
                    date = System.currentTimeMillis().toString(),
                    status = "Pending",
                    purpose = purpose,
                    timestamp = System.currentTimeMillis()
                )

                database.child(requestId).setValue(request).addOnCompleteListener {
                    if (it.isSuccessful) {
                        Toast.makeText(this, "Service requested successfully", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this, "Failed to submit request", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Failed to generate a unique ID for the request", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "You must be logged in to make a request", Toast.LENGTH_SHORT).show()
        }
    }
}
