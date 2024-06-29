package com.example.remed

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.remed.databinding.KonsultasiActivityBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class KonsulActivity : AppCompatActivity() {

    private lateinit var binding: KonsultasiActivityBinding
    private val database = FirebaseDatabase.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = KonsultasiActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Ambil UID pengguna dari Intent
        val userId = intent.getStringExtra("userId")
        // Back Button Logic
        binding.backButton.setOnClickListener {
            // Finish the current activity to go back to the previous one
            finish()
            intent.putExtra("userId", userId)

        }
        // Fetch profile image URL from Firebase Realtime Database
        val userRef = database.getReference("users/$userId")

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val imageUrl = dataSnapshot.child("photoUrl").getValue(String::class.java)

                    if (!imageUrl.isNullOrEmpty()) {
                        Glide.with(this@KonsulActivity)
                            .load(imageUrl)
                            .into(binding.ivProfileImage)
                    } else {
                        // No image URL found, set a default image or placeholder
                        binding.ivProfileImage.setImageResource(R.drawable.ic_user) // Replace with your default image resource
                    }
                } else {
                    // User data not found
                    Log.e("KonsulActivity", "User data not found for ID: $userId")
                    // Handle the error, e.g., display an error message or default image
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle database errors (e.g., display an error message)
                Log.e("KonsulActivity", "Error fetching user data: ${databaseError.message}")
                // Display a default image or error placeholder
            }
        })

        // Profile Image Logic (Assuming you have a ProfileActivity)
        binding.ivProfileImage.setOnClickListener {
            val intent = Intent(this, ProfilActivity::class.java)
            intent.putExtra("userId", userId)
            startActivity(intent)
        }

        binding.konsulWA.setOnClickListener {
            val phoneNumber = "6282283535508" // Replace with the actual phone number (with country code)
            val message = "Halo, saya ingin berkonsultasi." // Optional pre-filled message

            val url = "https://api.whatsapp.com/send?phone=$phoneNumber&text=$message"
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)

            val packageManager = packageManager
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            } else {
                // Handle the case where WhatsApp is not installed
                Toast.makeText(this, "WhatsApp tidak terpasang", Toast.LENGTH_SHORT).show()
                // You could also consider redirecting to the Play Store to download WhatsApp
            }
        }
    }
}
