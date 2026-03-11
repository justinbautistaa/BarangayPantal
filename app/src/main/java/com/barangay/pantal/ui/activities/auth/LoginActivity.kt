package com.barangay.pantal.ui.activities.auth

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.barangay.pantal.databinding.ActivityLoginBinding
import com.barangay.pantal.model.User
import com.barangay.pantal.network.SupabaseClient
import com.barangay.pantal.ui.activities.BaseActivity
import com.barangay.pantal.ui.activities.admin.AdminDashboardActivity
import com.barangay.pantal.ui.activities.staff.StaffDashboardActivity
import com.barangay.pantal.ui.activities.user.UserDashboardActivity
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch
import java.util.Locale

class LoginActivity : BaseActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.loginButton.setOnClickListener {
            loginUser()
        }

        binding.signupTextView.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }
    }

    private fun loginUser() {
        val email = binding.emailEditText.text.toString().trim()
        val password = binding.passwordEditText.text.toString().trim()

        if (email.isEmpty()) {
            binding.emailEditText.error = "Email is required"
            binding.emailEditText.requestFocus()
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailEditText.error = "Please enter a valid email"
            binding.emailEditText.requestFocus()
            return
        }

        if (password.isEmpty()) {
            binding.passwordEditText.error = "Password is required"
            binding.passwordEditText.requestFocus()
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        binding.loginButton.isEnabled = false

        lifecycleScope.launch {
            try {
                // 1. Sign in with Supabase Auth
                SupabaseClient.client.auth.signInWith(Email) {
                    this.email = email
                    this.password = password
                }

                // 2. Get user session/id
                val supabaseUser = SupabaseClient.client.auth.retrieveUserForCurrentSession()
                val userId = supabaseUser.id

                Log.d("LoginActivity", "Login successful for UID: $userId")
                fetchUserRoleAndNavigate(userId)

            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                binding.loginButton.isEnabled = true
                val errorMsg = e.localizedMessage ?: "Unknown error"
                Log.e("LoginActivity", "Auth failed: $errorMsg")
                Toast.makeText(this@LoginActivity, "Login Failed: $errorMsg", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun fetchUserRoleAndNavigate(uid: String) {
        lifecycleScope.launch {
            try {
                // Fetch user from 'users' table
                val user = SupabaseClient.client.postgrest["users"]
                    .select {
                        filter {
                            eq("id", uid)
                        }
                    }
                    .decodeSingleOrNull<User>()

                binding.progressBar.visibility = View.GONE
                binding.loginButton.isEnabled = true

                if (user != null) {
                    Log.d("LoginActivity", "Role found: ${user.role}")
                    navigateBasedOnRole(user.role)
                } else {
                    Log.e("LoginActivity", "No user data found at users table for id: $uid")
                    Toast.makeText(this@LoginActivity, "User profile not found in database.", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                binding.loginButton.isEnabled = true
                Log.e("LoginActivity", "Database error: ${e.message}")
                Toast.makeText(this@LoginActivity, "Database Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun navigateBasedOnRole(role: String) {
        val cleanRole = role.trim().lowercase(Locale.getDefault())
        saveUserRole(cleanRole)

        val intent = when (cleanRole) {
            "admin" -> Intent(this, AdminDashboardActivity::class.java)
            "staff" -> Intent(this, StaffDashboardActivity::class.java)
            else -> Intent(this, UserDashboardActivity::class.java)
        }
        startActivity(intent)
        finishAffinity()
    }

    private fun saveUserRole(role: String) {
        val sharedPref = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("user_role", role)
            apply()
        }
    }
}
