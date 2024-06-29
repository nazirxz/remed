package com.example.remed

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.InputType
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.remed.databinding.ProfilActivityBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.*

class ProfilActivity : AppCompatActivity() {

    private lateinit var binding: ProfilActivityBinding
    private lateinit var databaseReference: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var storageReference: StorageReference
    private var user: User? = null
    private var isEditing = false
    private val PICK_IMAGE_REQUEST = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ProfilActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().reference.child("users")
        storageReference = FirebaseStorage.getInstance().reference

        val userId = auth.currentUser?.uid ?: ""

        // Fetch user data from database
        fetchDataFromDatabase(userId)

        // Add listener for "Save" button
        binding.btnSave.setOnClickListener {
            if (isEditing) {
                saveUserData(userId)
            } else {
                toggleEditState()
            }
        }

        // Add listener for "Add Photo" button
        binding.btnTambahFoto.setOnClickListener {
            openImageChooser()
        }

        // Add listener for each EditText
        val editTexts = listOf(binding.etNama, binding.etJenisKelamin, binding.etUsia, binding.etRiwayatPenyakit, binding.etEmail, binding.etAlamat)
        editTexts.forEach { editText ->
            editText.setOnClickListener {
                if (isEditing) {
                    makeEditTextEditable(editText)
                }
            }
        }
    }

    private fun fetchDataFromDatabase(userId: String) {
        databaseReference.child(userId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    user = snapshot.getValue(User::class.java)
                    populateUserData()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ProfilActivity, "Failed to fetch data: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun populateUserData() {
        user?.let {
            binding.etNama.setText(it.name)
            binding.etJenisKelamin.setText(it.gender)
            binding.etUsia.setText(it.age)
            binding.etRiwayatPenyakit.setText(it.medicalHistory)
            binding.etEmail.setText(it.emailPhone)
            binding.etAlamat.setText(it.address)

            // Load profile photo with Glide
            Glide.with(this@ProfilActivity)
                .load(it.photoUrl)
                .placeholder(R.drawable.ic_user) // Placeholder while loading image
                .error(R.drawable.ic_user) // Error image if failed to load
                .into(binding.ivProfileImage)
        }
    }

    private fun saveUserData(userId: String) {
        val updatedUser = User(
            binding.etNama.text.toString(),
            binding.etJenisKelamin.text.toString(),
            binding.etUsia.text.toString(),
            binding.etRiwayatPenyakit.text.toString(),
            binding.etEmail.text.toString(),
            binding.etAlamat.text.toString(),
            user?.controlSchedule ?: "",
            userId,
            user?.photoUrl // Use existing photoUrl or null if not available
        )

        databaseReference.child(userId).setValue(updatedUser)
            .addOnSuccessListener {
                Toast.makeText(this, "Data successfully saved", Toast.LENGTH_SHORT).show()
                toggleEditState()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to save data: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun openImageChooser() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            val imageUri = data.data
            uploadImageToFirebase(imageUri!!)
        }
    }

    private fun uploadImageToFirebase(imageUri: Uri) {
        val userId = auth.currentUser?.uid ?: return // Get user UID

        val fileName = UUID.randomUUID().toString()
        val imageRef = storageReference.child("images/$fileName") // Save image in 'images' folder

        imageRef.putFile(imageUri)
            .addOnSuccessListener { taskSnapshot ->
                // Image uploaded successfully, get the download URL
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    val downloadUrl = uri.toString()
                    // Save image URL to Realtime Database
                    saveImageToDatabase(userId, downloadUrl)
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Failed to upload image: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveImageToDatabase(userId: String, imageUrl: String) {
        databaseReference.child(userId).child("photoUrl").setValue(imageUrl)
            .addOnSuccessListener {
                Toast.makeText(this, "Image successfully saved", Toast.LENGTH_SHORT).show()
                user?.photoUrl = imageUrl
                populateUserData() // Refresh view with new image
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to save image: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun makeEditTextEditable(editText: EditText) {
        editText.inputType = InputType.TYPE_CLASS_TEXT
        editText.isFocusable = true
        editText.isFocusableInTouchMode = true
    }

    private fun toggleEditState() {
        isEditing = !isEditing
        binding.btnSave.text = if (isEditing) "Save" else "Edit"

        val editTexts = listOf(binding.etNama, binding.etJenisKelamin, binding.etUsia, binding.etRiwayatPenyakit, binding.etEmail, binding.etAlamat)
        editTexts.forEach { editText ->
            editText.isEnabled = isEditing
            if (!isEditing) {
                editText.inputType = InputType.TYPE_NULL // Disable text input when not editing
            }
        }
    }
}
