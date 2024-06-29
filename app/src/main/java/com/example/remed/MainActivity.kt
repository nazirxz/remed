package com.example.remed

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.remed.databinding.MainActivityBinding
import com.example.remed.databinding.SplashScreenBinding

class MainActivity:AppCompatActivity() {
    private lateinit var binding: MainActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MainActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Ambil UID pengguna dari Intent
        val userId = intent.getStringExtra("userId")

        binding.menuProfil.setOnClickListener {
            val intent = Intent(this, ProfilActivity::class.java)
            intent.putExtra("userId", userId) // Teruskan UID ke ProfileActivity
            startActivity(intent)
        }
        binding.menuKonsultasi.setOnClickListener {
            val intent = Intent(this, KonsulActivity::class.java)
            intent.putExtra("userId", userId) // Teruskan UID ke ProfileActivity
            startActivity(intent)
        }
        binding.menuKeranjang.setOnClickListener {
            val intent = Intent(this, KeranjangActivity::class.java)
            intent.putExtra("userId", userId) // Teruskan UID ke ProfileActivity
            startActivity(intent)
        }
    }
}