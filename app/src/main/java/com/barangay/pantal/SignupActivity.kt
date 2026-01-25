package com.barangay.pantal

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SignupActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        // Find views
        val editTextFullName = findViewById<EditText>(R.id.editTextFullName)
        val editTextUsername = findViewById<EditText>(R.id.editTextUsername)
        val editTextEmail = findViewById<EditText>(R.id.editTextEmail)
        val editTextPassword = findViewById<EditText>(R.id.editTextPassword)
        val editTextConfirmPassword = findViewById<EditText>(R.id.editTextConfirmPassword)
        val buttonCreateAccount = findViewById<Button>(R.id.buttonCreateAccount)
        val textViewBack = findViewById<TextView>(R.id.textViewBack)

        // Back button
        textViewBack.setOnClickListener {
            finish() // Go back to login
        }

        // Create account button
        buttonCreateAccount.setOnClickListener {
            val fullName = editTextFullName.text.toString().trim()
            val username = editTextUsername.text.toString().trim()
            val email = editTextEmail.text.toString().trim()
            val password = editTextPassword.text.toString().trim()
            val confirmPassword = editTextConfirmPassword.text.toString().trim()

            // Validation
            if (fullName.isEmpty() || username.isEmpty() || email.isEmpty() ||
                password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Success
            Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show()

            // Go to Dashboard
            val intent = Intent(this, DashboardActivity::class.java)
            startActivity(intent)
            finish() // Close signup screen
        }
    }
}