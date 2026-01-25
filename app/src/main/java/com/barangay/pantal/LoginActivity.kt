package com.barangay.pantal

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.barangay.pantal.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Login button
        binding.btnLogin.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Demo credentials
            if (username == "admin" && password == "admin123") {
                Toast.makeText(this, "Admin login successful!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, DashboardActivity::class.java))
                finish()
            } else {
                // TODO: Replace with actual user authentication
                Toast.makeText(this, "User login successful!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, UserDashboardActivity::class.java))
                finish()
            }
        }

        // Signup button
        binding.btnSignup.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }

        // Forgot password
        binding.tvForgotPassword.setOnClickListener {
            Toast.makeText(this, "Reset password feature coming soon", Toast.LENGTH_SHORT).show()
        }
    }
}