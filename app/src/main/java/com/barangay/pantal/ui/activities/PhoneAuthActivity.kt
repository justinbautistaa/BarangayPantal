package com.barangay.pantal.ui.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.barangay.pantal.databinding.ActivityPhoneAuthBinding
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

class PhoneAuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPhoneAuthBinding
    private lateinit var auth: FirebaseAuth
    private var storedVerificationId: String? = null
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPhoneAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.sendCodeButton.setOnClickListener {
            sendVerificationCode()
        }

        binding.verifyButton.setOnClickListener {
            verifyCode()
        }
    }

    private fun sendVerificationCode() {
        val phoneNumber = binding.phoneNumberEditText.text.toString().trim()
        if (phoneNumber.isNotEmpty()) {
            binding.progressBar.visibility = View.VISIBLE
            val options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(phoneNumber) // Phone number to verify
                .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                .setActivity(this) // Activity (for callback binding)
                .setCallbacks(callbacks) // OnVerificationStateChangedCallbacks
                .build()
            PhoneAuthProvider.verifyPhoneNumber(options)
        } else {
            Toast.makeText(this, "Enter a phone number", Toast.LENGTH_SHORT).show()
        }
    }

    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            Log.d("PhoneAuthActivity", "onVerificationCompleted:$credential")
            signInWithPhoneAuthCredential(credential)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            Log.w("PhoneAuthActivity", "onVerificationFailed", e)
            binding.progressBar.visibility = View.GONE
            Toast.makeText(this@PhoneAuthActivity, "Verification failed: ${e.message}", Toast.LENGTH_LONG).show()
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken
        ) {
            Log.d("PhoneAuthActivity", "onCodeSent:$verificationId")
            storedVerificationId = verificationId
            resendToken = token
            binding.progressBar.visibility = View.GONE
            binding.verificationCodeEditText.visibility = View.VISIBLE
            binding.verifyButton.visibility = View.VISIBLE
        }
    }

    private fun verifyCode() {
        val code = binding.verificationCodeEditText.text.toString().trim()
        if (code.isNotEmpty() && storedVerificationId != null) {
            binding.progressBar.visibility = View.VISIBLE
            val credential = PhoneAuthProvider.getCredential(storedVerificationId!!, code)
            signInWithPhoneAuthCredential(credential)
        } else {
            Toast.makeText(this, "Enter the verification code", Toast.LENGTH_SHORT).show()
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = task.result?.user
                    Toast.makeText(this@PhoneAuthActivity, "Authentication successful. UID: ${user?.uid}", Toast.LENGTH_SHORT).show()
                    // You can navigate to your main activity here
                    val intent = Intent(this@PhoneAuthActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Log.w("PhoneAuthActivity", "signInWithCredential failed", task.exception)
                    Toast.makeText(this@PhoneAuthActivity, "Authentication failed.", Toast.LENGTH_SHORT).show()
                }
                binding.progressBar.visibility = View.GONE
            }
    }
}