package com.example.remed

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.remed.databinding.RegisterActivityBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: RegisterActivityBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = RegisterActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().reference.child("users")

        binding.buttonRegister.setOnClickListener {
            val name = binding.inputName.text.toString()
            val gender = binding.inputGender.text.toString()
            val age = binding.inputAge.text.toString()
            val medicalHistory = binding.inputMedicalHistory.text.toString()
            val emailPhone = binding.inputEmailPhone.text.toString()
            val address = binding.inputAddress.text.toString()
            val password = binding.inputPassword.text.toString()
            val controlSchedule = binding.inputControlSchedule.text.toString()

            if (emailPhone.isNotEmpty() && password.isNotEmpty()) {
                firebaseAuth.createUserWithEmailAndPassword(emailPhone, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val currentUser = firebaseAuth.currentUser
                            val userId = currentUser?.uid ?: ""

                            val user = User(name, gender, age, medicalHistory, emailPhone, address, controlSchedule, userId)
                            databaseReference.child(userId).setValue(user)

                            Toast.makeText(this, "Registrasi berhasil!", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, MainActivity::class.java)) // Assuming MainActivity is your main screen
                            finish()
                        } else {
                            Toast.makeText(this, "Registrasi gagal. Silakan coba lagi.", Toast.LENGTH_SHORT).show()
                            Log.e("RegisterActivity", "Error: ${task.exception?.message}")
                        }
                    }
            } else {
                Toast.makeText(this, "Email dan password harus diisi.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
