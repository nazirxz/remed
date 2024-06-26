package com.example.remed

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.remed.databinding.ForgetpassActivityBinding

class ResetActivity : AppCompatActivity() {
    private lateinit var binding: ForgetpassActivityBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ForgetpassActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}