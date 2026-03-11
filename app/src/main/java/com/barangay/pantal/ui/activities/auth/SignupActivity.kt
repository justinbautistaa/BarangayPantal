package com.barangay.pantal.ui.activities.auth

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.barangay.pantal.databinding.ActivitySignupBinding
import com.barangay.pantal.model.User
import com.barangay.pantal.network.SupabaseClient
import com.barangay.pantal.ui.activities.BaseActivity
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch

class SignupActivity : BaseActivity() {

    private lateinit var binding: ActivitySignupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

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

        lifecycleScope.launch {
            try {
                // 1. Create account in Supabase Auth
                val signupResult = SupabaseClient.client.auth.signUpWith(Email) {
                    this.email = email
                    this.password = password
                }

                // 2. Save profile to 'users' table
                signupResult?.let { user ->
                    val userProfile = User(id = user.id, fullName = fullName, email = email, role = "user")
                    SupabaseClient.client.postgrest["users"].insert(userProfile)
                }

                binding.progressBar.visibility = View.GONE
                Toast.makeText(this@SignupActivity, "Signup successful! Check your Gmail for verification.", Toast.LENGTH_LONG).show()
                startActivity(Intent(this@SignupActivity, LoginActivity::class.java))
                finish()

            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                binding.signupButton.isEnabled = true
                Toast.makeText(this@SignupActivity, "Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
