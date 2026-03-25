package com.barangay.pantal.ui.activities.auth

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import com.barangay.pantal.R
import com.barangay.pantal.databinding.ActivitySignupBinding
import com.barangay.pantal.model.Resident
import com.barangay.pantal.model.User
import com.barangay.pantal.network.SupabaseClient
import com.barangay.pantal.ui.activities.BaseActivity
import com.barangay.pantal.util.ValidationUtils
import io.github.jan.supabase.gotrue.OtpType
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.OTP
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class SignupActivity : BaseActivity() {

    private lateinit var binding: ActivitySignupBinding
    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val genderOptions = arrayOf("Male", "Female")
    private val civilStatusOptions = arrayOf("Single", "Married", "Widowed", "Separated")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupForm()

        binding.signupButton.setOnClickListener {
            signupUser()
        }

        binding.loginTextView.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    private fun setupForm() {
        binding.genderAutoComplete.setAdapter(
            ArrayAdapter(this, android.R.layout.simple_list_item_1, genderOptions)
        )
        binding.civilStatusAutoComplete.setAdapter(
            ArrayAdapter(this, android.R.layout.simple_list_item_1, civilStatusOptions)
        )
        binding.birthdateEditText.setOnClickListener { showBirthdatePicker() }
        binding.birthdateEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) showBirthdatePicker()
        }
    }

    private fun showBirthdatePicker() {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val selectedCalendar = Calendar.getInstance().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                }
                val birthdate = dateFormatter.format(selectedCalendar.time)
                binding.birthdateEditText.setText(birthdate)
                binding.ageEditText.setText(calculateAgeFromBirthdate(birthdate).toString())
            },
            calendar.get(Calendar.YEAR) - 18,
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun calculateAgeFromBirthdate(birthdate: String): Int {
        return try {
            val birthDate = dateFormatter.parse(birthdate) ?: return 0
            val today = Calendar.getInstance()
            val dob = Calendar.getInstance().apply { time = birthDate }
            var age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR)
            if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) {
                age -= 1
            }
            age.coerceAtLeast(0)
        } catch (_: Exception) {
            0
        }
    }

    private fun signupUser() {
        val fullName = ValidationUtils.cleanText(binding.nameEditText.text.toString(), 120)
        val birthdate = ValidationUtils.cleanText(binding.birthdateEditText.text.toString(), 20)
        val age = binding.ageEditText.text?.toString()?.trim()?.toIntOrNull() ?: calculateAgeFromBirthdate(birthdate)
        val gender = ValidationUtils.cleanText(binding.genderAutoComplete.text.toString(), 20)
        val civilStatus = ValidationUtils.cleanText(binding.civilStatusAutoComplete.text.toString(), 40)
        val address = ValidationUtils.cleanText(binding.addressEditText.text.toString(), 255)
        val phoneNumber = ValidationUtils.cleanText(binding.phoneEditText.text.toString(), 20)
        val occupation = ValidationUtils.cleanText(binding.occupationEditText.text.toString(), 120)
        val email = ValidationUtils.cleanText(
            binding.emailEditText.text.toString(),
            160
        ).lowercase(Locale.getDefault())
        val password = binding.passwordEditText.text.toString().trim()
        val confirmPassword = binding.confirmPasswordEditText.text.toString().trim()
        val isSenior = binding.isSeniorCheckbox.isChecked
        val isPwd = binding.isPwdCheckbox.isChecked

        var isValid = true

        if (fullName.length < 3) {
            binding.nameEditText.error = "Full name is required"
            isValid = false
        }

        if (birthdate.isEmpty()) {
            binding.birthdateEditText.error = "Birthdate is required"
            isValid = false
        }

        if (gender.isEmpty()) {
            binding.genderAutoComplete.error = "Please select gender"
            isValid = false
        }

        if (address.isEmpty()) {
            binding.addressEditText.error = "Address is required"
            isValid = false
        }

        if (phoneNumber.isNotEmpty() && !ValidationUtils.isValidPhone(phoneNumber)) {
            binding.phoneEditText.error = "Enter a valid phone number"
            isValid = false
        }

        if (email.isEmpty()) {
            binding.emailEditText.error = getString(R.string.error_email_required)
            isValid = false
        } else if (!ValidationUtils.isValidGmail(email)) {
            binding.emailEditText.error = getString(R.string.error_gmail_only)
            isValid = false
        }

        if (password.isEmpty()) {
            binding.passwordEditText.error = getString(R.string.error_password_required)
            isValid = false
        } else if (!ValidationUtils.isStrongPassword(password)) {
            binding.passwordEditText.error = "Password must be at least 8 characters"
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
                        put("name", fullName)
                        put("birthdate", birthdate)
                        put("age", age)
                        put("gender", gender)
                        put("civil_status", civilStatus)
                        put("address", address)
                        put("phone_number", phoneNumber)
                        put("phone", phoneNumber)
                        put("occupation", occupation)
                        put("is_voter", false)
                        put("is_senior", isSenior)
                        put("is_pwd", isPwd)
                        put("role", "resident")
                    }
                }

                binding.progressBar.visibility = View.GONE
                binding.signupButton.isEnabled = true
                Toast.makeText(this@SignupActivity, "OTP sent to your Gmail.", Toast.LENGTH_LONG).show()

                showSignupOtpDialog(
                    email = email,
                    password = password,
                    fullName = fullName,
                    birthdate = birthdate,
                    age = age,
                    gender = gender,
                    civilStatus = civilStatus,
                    address = address,
                    phoneNumber = phoneNumber,
                    occupation = occupation,
                    isSenior = isSenior,
                    isPwd = isPwd
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
        birthdate: String,
        age: Int,
        gender: String,
        civilStatus: String,
        address: String,
        phoneNumber: String,
        occupation: String,
        isSenior: Boolean,
        isPwd: Boolean
    ) {
        val otpInput = EditText(this).apply {
            hint = "Enter 6-digit Gmail OTP"
            inputType = InputType.TYPE_CLASS_NUMBER
            filters = arrayOf(InputFilter.LengthFilter(6))
            setPadding(60, 40, 60, 40)
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle("Verify Gmail OTP")
            .setMessage("Enter the 6-digit OTP sent to $email to complete your signup.")
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
                if (!otp.matches(Regex("\\d{6}"))) {
                    otpInput.error = "OTP must be 6 digits"
                    return@setOnClickListener
                }

                verifySignupOtp(
                    dialog = dialog,
                    email = email,
                    otp = otp,
                    password = password,
                    fullName = fullName,
                    birthdate = birthdate,
                    age = age,
                    gender = gender,
                    civilStatus = civilStatus,
                    address = address,
                    phoneNumber = phoneNumber,
                    occupation = occupation,
                    isSenior = isSenior,
                    isPwd = isPwd
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
        birthdate: String,
        age: Int,
        gender: String,
        civilStatus: String,
        address: String,
        phoneNumber: String,
        occupation: String,
        isSenior: Boolean,
        isPwd: Boolean
    ) {
        lifecycleScope.launch {
            try {
                binding.progressBar.visibility = View.VISIBLE

                SupabaseClient.client.auth.verifyEmailOtp(OtpType.Email.EMAIL, email, otp)
                SupabaseClient.client.auth.updateUser {
                    this.password = password
                    data {
                        put("full_name", fullName)
                        put("name", fullName)
                        put("birthdate", birthdate)
                        put("age", age)
                        put("gender", gender)
                        put("civil_status", civilStatus)
                        put("address", address)
                        put("phone_number", phoneNumber)
                        put("phone", phoneNumber)
                        put("occupation", occupation)
                        put("is_voter", false)
                        put("is_senior", isSenior)
                        put("is_pwd", isPwd)
                        put("role", "resident")
                    }
                }

                val authUser = SupabaseClient.client.auth.currentUserOrNull()
                    ?: SupabaseClient.client.auth.retrieveUserForCurrentSession()

                val role = "user"
                val userProfile = User(
                    id = authUser.id,
                    fullName = fullName,
                    email = email,
                    role = role,
                    age = age,
                    gender = gender.ifBlank { null },
                    address = address,
                    occupation = occupation.ifBlank { null },
                    phoneNumber = phoneNumber.ifBlank { null }
                )

                SupabaseClient.client.postgrest["users"].upsert(userProfile)
                try {
                    val residentProfile = Resident(
                        id = authUser.id,
                        name = fullName,
                        age = age,
                        gender = gender.ifBlank { null },
                        address = address,
                        occupation = occupation.ifBlank { null },
                        birthdate = birthdate.ifBlank { null },
                        civilStatus = civilStatus.ifBlank { null },
                        phone = phoneNumber.ifBlank { null },
                        email = email,
                        isVoter = false,
                        isSenior = isSenior,
                        isPwd = isPwd
                    )
                    SupabaseClient.client.postgrest["residents"].upsert(residentProfile)
                } catch (residentSyncError: Exception) {
                    Log.w("SignupActivity", "Resident sync warning: ${residentSyncError.localizedMessage}")
                }

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

}
