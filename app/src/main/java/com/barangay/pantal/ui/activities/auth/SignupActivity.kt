package com.barangay.pantal.ui.activities.auth

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import com.barangay.pantal.databinding.ActivitySignupBinding
import com.barangay.pantal.model.User
import com.barangay.pantal.ui.activities.BaseActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class SignupActivity : BaseActivity() {

    private lateinit var binding: ActivitySignupBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.signupButton.setOnClickListener {
            signupUser()
        }

        binding.loginTextView.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    private fun signupUser() {
        val fullName = binding.nameEditText.text.toString().trim()
        val email = binding.emailEditText.text.toString().trim()
        val password = binding.passwordEditText.text.toString().trim()
        val confirmPassword = binding.confirmPasswordEditText.text.toString().trim()

        if (fullName.isEmpty()) {
            binding.nameEditText.error = "Full name is required"
            return
        }

        if (email.isEmpty()) {
            binding.emailEditText.error = "Email is required"
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches() || !email.endsWith("@gmail.com")) {
            binding.emailEditText.error = "Please enter a valid Gmail address"
            return
        }

        if (password.length < 6) {
            binding.passwordEditText.error = "Password must be at least 6 characters"
            return
        }

        if (password != confirmPassword) {
            binding.confirmPasswordEditText.error = "Passwords do not match"
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        binding.signupButton.isEnabled = false

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val firebaseUser = auth.currentUser
                    if (firebaseUser != null) {
                        firebaseUser.sendEmailVerification()
                            .addOnCompleteListener { verificationTask ->
                                if (verificationTask.isSuccessful) {
                                    saveUserToDatabase(firebaseUser.uid, fullName, email)
                                } else {
                                    Toast.makeText(this, "Failed to send verification email: ${verificationTask.exception?.message}", Toast.LENGTH_LONG).show()
                                    binding.progressBar.visibility = View.GONE
                                    binding.signupButton.isEnabled = true
                                }
                            }
                    }
                } else {
                    binding.progressBar.visibility = View.GONE
                    binding.signupButton.isEnabled = true
                    Toast.makeText(this, "Signup failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun saveUserToDatabase(uid: String, fullName: String, email: String) {
        val database = FirebaseDatabase.getInstance().reference.child("users").child(uid)
        val user = User(fullName, email, "user") // Default role is user

        database.setValue(user).addOnCompleteListener {
            binding.progressBar.visibility = View.GONE
            if (it.isSuccessful) {
                Toast.makeText(this, "Signup successful! Please check your Gmail for verification.", Toast.LENGTH_LONG).show()
                auth.signOut() // Sign out until email is verified
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            } else {
                binding.signupButton.isEnabled = true
                Toast.makeText(this, "Failed to save user data", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
