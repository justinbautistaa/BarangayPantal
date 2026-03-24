package com.barangay.pantal.ui.activities.auth

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import com.barangay.pantal.R
import com.barangay.pantal.databinding.ActivityLoginBinding
import com.barangay.pantal.model.User
import com.barangay.pantal.network.SupabaseClient
import com.barangay.pantal.ui.activities.BaseActivity
import com.barangay.pantal.ui.activities.admin.AdminDashboardActivity
import com.barangay.pantal.ui.activities.staff.StaffDashboardActivity
import com.barangay.pantal.ui.activities.user.UserDashboardActivity
import io.github.jan.supabase.gotrue.OtpType
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.gotrue.providers.builtin.OTP
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

        binding.forgotPasswordTextView.setOnClickListener {
            showForgotPasswordDialog()
        }
    }

    private fun loginUser() {
        val email = binding.emailEditText.text.toString().trim().lowercase(Locale.getDefault())
        val password = binding.passwordEditText.text.toString().trim()

        var isValid = true

        if (email.isEmpty()) {
            binding.emailEditText.error = getString(R.string.error_email_required)
            isValid = false
        } else if (!isValidGmail(email)) {
            binding.emailEditText.error = getString(R.string.error_gmail_only)
            isValid = false
        }

        if (password.isEmpty()) {
            binding.passwordEditText.error = getString(R.string.error_password_required)
            isValid = false
        } else if (password.length < 6) {
            binding.passwordEditText.error = getString(R.string.error_password_length)
            isValid = false
        }

        if (!isValid) return

        binding.progressBar.visibility = View.VISIBLE
        binding.loginButton.isEnabled = false

        lifecycleScope.launch {
            try {
                SupabaseClient.client.auth.signInWith(Email) {
                    this.email = email
                    this.password = password
                }

                val supabaseUser = SupabaseClient.client.auth.retrieveUserForCurrentSession()
                val userId = supabaseUser.id

                Log.d("LoginActivity", "Login successful for UID: $userId")
                fetchUserRoleAndNavigate(userId)

            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                binding.loginButton.isEnabled = true
                val errorMsg = e.localizedMessage ?: "Unknown error"
                Log.e("LoginActivity", "Auth failed: $errorMsg")
                Toast.makeText(this@LoginActivity, getString(R.string.error_login_failed, errorMsg), Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun showForgotPasswordDialog() {
        val emailInput = EditText(this).apply {
            hint = "Enter your Gmail address"
            setPadding(60, 40, 60, 40)
        }

        AlertDialog.Builder(this)
            .setTitle("Forgot Password")
            .setMessage("We will send a Gmail OTP so you can reset your password.")
            .setView(emailInput)
            .setPositiveButton("Send OTP") { _, _ ->
                val email = emailInput.text.toString().trim().lowercase(Locale.getDefault())
                if (isValidGmail(email)) {
                    sendPasswordResetOtp(email)
                } else {
                    Toast.makeText(this, getString(R.string.error_gmail_only), Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun sendPasswordResetOtp(email: String) {
        lifecycleScope.launch {
            try {
                SupabaseClient.client.auth.signInWith(OTP) {
                    this.email = email
                    createUser = false
                }
                Toast.makeText(this@LoginActivity, "OTP sent to $email", Toast.LENGTH_LONG).show()
                showResetOtpDialog(email)
            } catch (e: Exception) {
                Toast.makeText(this@LoginActivity, "Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun showResetOtpDialog(email: String) {
        val otpInput = EditText(this).apply {
            hint = "Enter Gmail OTP"
            setPadding(60, 32, 60, 16)
        }
        val newPasswordInput = EditText(this).apply {
            hint = "New Password"
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            setPadding(60, 16, 60, 16)
        }
        val confirmPasswordInput = EditText(this).apply {
            hint = "Confirm New Password"
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            setPadding(60, 16, 60, 32)
        }

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(otpInput)
            addView(newPasswordInput)
            addView(confirmPasswordInput)
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle("Reset Password via OTP")
            .setMessage("Enter the OTP from Gmail and choose a new password.")
            .setView(layout)
            .setPositiveButton("Reset", null)
            .setNegativeButton("Cancel", null)
            .setNeutralButton("Resend", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val otp = otpInput.text.toString().trim()
                val newPassword = newPasswordInput.text.toString().trim()
                val confirmPassword = confirmPasswordInput.text.toString().trim()

                if (otp.isBlank()) {
                    otpInput.error = "OTP is required"
                    return@setOnClickListener
                }
                if (newPassword.length < 6) {
                    newPasswordInput.error = getString(R.string.error_password_length)
                    return@setOnClickListener
                }
                if (newPassword != confirmPassword) {
                    confirmPasswordInput.error = getString(R.string.error_passwords_dont_match)
                    return@setOnClickListener
                }

                verifyResetOtpAndChangePassword(dialog, email, otp, newPassword)
            }

            dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener {
                resendResetOtp(email)
            }
        }

        dialog.show()
    }

    private fun verifyResetOtpAndChangePassword(
        dialog: AlertDialog,
        email: String,
        otp: String,
        newPassword: String
    ) {
        lifecycleScope.launch {
            try {
                SupabaseClient.client.auth.verifyEmailOtp(OtpType.Email.EMAIL, email, otp)
                SupabaseClient.client.auth.updateUser {
                    password = newPassword
                }
                Toast.makeText(this@LoginActivity, "Password updated successfully!", Toast.LENGTH_LONG).show()
                dialog.dismiss()
            } catch (e: Exception) {
                Toast.makeText(
                    this@LoginActivity,
                    "Password reset failed: ${e.localizedMessage}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun resendResetOtp(email: String) {
        lifecycleScope.launch {
            try {
                SupabaseClient.client.auth.signInWith(OTP) {
                    this.email = email
                    createUser = false
                }
                Toast.makeText(this@LoginActivity, "OTP resent to $email", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(
                    this@LoginActivity,
                    "Failed to resend OTP: ${e.localizedMessage}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun fetchUserRoleAndNavigate(uid: String) {
        lifecycleScope.launch {
            try {
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
                    Toast.makeText(this@LoginActivity, getString(R.string.error_user_not_found), Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                binding.loginButton.isEnabled = true
                Log.e("LoginActivity", "Database error: ${e.message}")
                Toast.makeText(this@LoginActivity, getString(R.string.error_database, e.message), Toast.LENGTH_LONG).show()
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

    private fun isValidGmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches() &&
            email.lowercase(Locale.getDefault()).endsWith("@gmail.com")
    }
}
