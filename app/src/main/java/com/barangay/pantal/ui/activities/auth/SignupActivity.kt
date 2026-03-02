package com.barangay.pantal.ui.activities.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.barangay.pantal.databinding.ActivitySignupBinding
import com.barangay.pantal.model.User
import com.barangay.pantal.network.Content
import com.barangay.pantal.network.EmailService
import com.barangay.pantal.network.EmailUser
import com.barangay.pantal.network.Personalization
import com.barangay.pantal.network.SendGridRequest
import com.barangay.pantal.ui.activities.BaseActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.random.Random

class SignupActivity : BaseActivity() {

    private lateinit var binding: ActivitySignupBinding
    private lateinit var auth: FirebaseAuth
    private var generatedOtp: String? = null

    // REPLACE WITH YOUR ACTUAL SENDGRID API KEY
    private val SENDGRID_API_KEY = "Bearer YOUR_API_KEY_HERE"
    // REPLACE WITH YOUR VERIFIED SENDER EMAIL
    private val SENDER_EMAIL = "your-verified-email@example.com"

    private val emailService: EmailService by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.sendgrid.com/v3/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(EmailService::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.btnSendOtp.setOnClickListener {
            sendOtp()
        }

        binding.signupButton.setOnClickListener {
            signupUser()
        }

        binding.loginTextView.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    private fun sendOtp() {
        val email = binding.emailEditText.text.toString().trim()

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches() || !email.endsWith("@gmail.com")) {
            binding.emailEditText.error = "Please enter a valid Gmail address"
            binding.emailEditText.requestFocus()
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        binding.btnSendOtp.isEnabled = false

        // Generate 6-digit OTP
        generatedOtp = (100000 + Random.nextInt(900000)).toString()
        
        lifecycleScope.launch {
            try {
                val request = SendGridRequest(
                    personalizations = listOf(Personalization(to = listOf(EmailUser(email)))),
                    from = EmailUser(SENDER_EMAIL, "Barangay Pantal"),
                    subject = "Your Signup OTP",
                    content = listOf(Content("text/plain", "Your verification code is: $generatedOtp"))
                )

                val response = emailService.sendEmail(SENDGRID_API_KEY, request)
                
                binding.progressBar.visibility = View.GONE
                if (response.isSuccessful) {
                    binding.otpLayout.visibility = View.VISIBLE
                    binding.btnSendOtp.text = "Resend"
                    binding.btnSendOtp.isEnabled = true
                    Toast.makeText(this@SignupActivity, "OTP sent to $email", Toast.LENGTH_LONG).show()
                } else {
                    binding.btnSendOtp.isEnabled = true
                    val errorBody = response.errorBody()?.string()
                    Log.e("SignupActivity", "SendGrid Error: $errorBody")
                    Toast.makeText(this@SignupActivity, "Failed to send OTP. Check logs.", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                binding.btnSendOtp.isEnabled = true
                Log.e("SignupActivity", "Error", e)
                Toast.makeText(this@SignupActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun signupUser() {
        val fullName = binding.nameEditText.text.toString().trim()
        val email = binding.emailEditText.text.toString().trim()
        val enteredOtp = binding.otpEditText.text.toString().trim()
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

        if (generatedOtp == null) {
            Toast.makeText(this, "Please request an OTP first", Toast.LENGTH_SHORT).show()
            return
        }

        if (enteredOtp != generatedOtp) {
            binding.otpEditText.error = "Invalid OTP"
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
                        saveUserToDatabase(firebaseUser.uid, fullName, email)
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
                Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            } else {
                binding.signupButton.isEnabled = true
                Toast.makeText(this, "Failed to save user data", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
