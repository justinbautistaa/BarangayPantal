package com.barangay.pantal.ui.activities.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import com.barangay.pantal.R
import com.barangay.pantal.databinding.ActivitySignupBinding
import com.barangay.pantal.model.User
import com.barangay.pantal.network.SupabaseClient
import com.barangay.pantal.ui.activities.BaseActivity
import io.github.jan.supabase.gotrue.OtpType
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.OTP
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.util.Locale

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
        val address = binding.addressEditText.text.toString().trim()
        val phoneNumber = binding.phoneEditText.text.toString().trim()
        val email = binding.emailEditText.text.toString().trim().lowercase(Locale.getDefault())
        val password = binding.passwordEditText.text.toString().trim()
        val confirmPassword = binding.confirmPasswordEditText.text.toString().trim()

        var isValid = true

        if (fullName.isEmpty()) {
            binding.nameEditText.error = "Full name is required"
            isValid = false
        }

        if (address.isEmpty()) {
            binding.addressEditText.error = "Address is required"
            isValid = false
        }

        if (phoneNumber.isEmpty()) {
            binding.phoneEditText.error = "Phone number is required"
            isValid = false
        } else if (phoneNumber.length < 11) {
            binding.phoneEditText.error = "Enter a valid phone number"
            isValid = false
        }

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

        if (confirmPassword.isEmpty()) {
            binding.confirmPasswordEditText.error = "Please confirm your password"
            isValid = false
        } else if (password != confirmPassword) {
            binding.confirmPasswordEditText.error = getString(R.string.error_passwords_dont_match)
            isValid = false
        }

        if (!isValid) return

        binding.progressBar.visibility = View.VISIBLE
        binding.signupButton.isEnabled = false

        lifecycleScope.launch {
            try {
                SupabaseClient.client.auth.signUpWith(OTP) {
                    this.email = email
                    createUser = true
                    data = buildJsonObject {
                        put("full_name", fullName)
                        put("address", address)
                        put("phone_number", phoneNumber)
                    }
                }

                binding.progressBar.visibility = View.GONE
                binding.signupButton.isEnabled = true
                Toast.makeText(this@SignupActivity, "OTP sent to your Gmail.", Toast.LENGTH_LONG).show()

                showSignupOtpDialog(
                    email = email,
                    password = password,
                    fullName = fullName,
                    address = address,
                    phoneNumber = phoneNumber
                )
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                binding.signupButton.isEnabled = true
                val errorMsg = e.localizedMessage ?: getString(R.string.error_connection_failed)
                Log.e("SignupActivity", "Error during OTP signup: $errorMsg")
                Toast.makeText(
                    this@SignupActivity,
                    getString(R.string.error_signup_failed, errorMsg),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun showSignupOtpDialog(
        email: String,
        password: String,
        fullName: String,
        address: String,
        phoneNumber: String
    ) {
        val otpInput = EditText(this).apply {
            hint = "Enter Gmail OTP"
            setPadding(60, 40, 60, 40)
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle("Verify Gmail OTP")
            .setMessage("Enter the OTP sent to $email to complete your signup.")
            .setView(otpInput)
            .setPositiveButton("Verify", null)
            .setNegativeButton("Cancel", null)
            .setNeutralButton("Resend", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val otp = otpInput.text.toString().trim()
                if (otp.isBlank()) {
                    otpInput.error = "OTP is required"
                    return@setOnClickListener
                }

                verifySignupOtp(
                    dialog = dialog,
                    email = email,
                    otp = otp,
                    password = password,
                    fullName = fullName,
                    address = address,
                    phoneNumber = phoneNumber
                )
            }

            dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener {
                resendSignupOtp(email)
            }
        }

        dialog.show()
    }

    private fun verifySignupOtp(
        dialog: AlertDialog,
        email: String,
        otp: String,
        password: String,
        fullName: String,
        address: String,
        phoneNumber: String
    ) {
        lifecycleScope.launch {
            try {
                binding.progressBar.visibility = View.VISIBLE

                SupabaseClient.client.auth.verifyEmailOtp(OtpType.Email.EMAIL, email, otp)
                SupabaseClient.client.auth.updateUser {
                    this.password = password
                    data {
                        put("full_name", fullName)
                        put("address", address)
                        put("phone_number", phoneNumber)
                    }
                }

                val authUser = SupabaseClient.client.auth.currentUserOrNull()
                    ?: SupabaseClient.client.auth.retrieveUserForCurrentSession()

                val role = if (email == "admin@gmail.com") "admin" else "user"
                val userProfile = User(
                    id = authUser.id,
                    fullName = fullName,
                    email = email,
                    role = role,
                    address = address,
                    phoneNumber = phoneNumber
                )

                SupabaseClient.client.postgrest["users"].upsert(userProfile)

                binding.progressBar.visibility = View.GONE
                dialog.dismiss()
                Toast.makeText(this@SignupActivity, "Signup completed successfully!", Toast.LENGTH_LONG).show()
                startActivity(Intent(this@SignupActivity, LoginActivity::class.java))
                finish()
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                Log.e("SignupActivity", "OTP verification failed: ${e.localizedMessage}")
                Toast.makeText(
                    this@SignupActivity,
                    "OTP verification failed: ${e.localizedMessage}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun resendSignupOtp(email: String) {
        lifecycleScope.launch {
            try {
                SupabaseClient.client.auth.resendEmail(OtpType.Email.SIGNUP, email)
                Toast.makeText(this@SignupActivity, "OTP resent to $email", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(
                    this@SignupActivity,
                    "Failed to resend OTP: ${e.localizedMessage}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun isValidGmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches() &&
            email.lowercase(Locale.getDefault()).endsWith("@gmail.com")
    }
}
