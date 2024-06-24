package com.example.remed

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.remed.databinding.LoginActivityBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: LoginActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LoginActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set click listener for the register link
        binding.registerLink.setOnClickListener {
            // Start the register activity
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }
}