package com.example.remed

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.remed.adapter.ObatAdapter
import com.example.remed.databinding.KeranjangActivityBinding // Pastikan ini benar
import com.example.remed.model.Obat
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseException
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import androidx.appcompat.widget.SearchView.OnQueryTextListener

class KeranjangActivity : AppCompatActivity() {
    private lateinit var binding: KeranjangActivityBinding
    private val database = FirebaseDatabase.getInstance()
    private val obatRef = database.getReference("obat") // Reference ke node "obat" di database
    private val obatList = mutableListOf<Obat>()
    private lateinit var obatAdapter: ObatAdapter
    private var originalObatList = mutableListOf<Obat>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = KeranjangActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)


        obatAdapter = ObatAdapter(obatList)
        binding.rvObat.adapter = obatAdapter

        obatRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                obatList.clear()
                for (obatSnapshot in snapshot.children) {
                    val obat = obatSnapshot.getValue(Obat::class.java)
                    obat?.let {
                        obatList.add(it)
                        originalObatList.add(it) // Tambahkan juga ke originalObatList
                        Log.d("KeranjangActivity", "Obat Data: $it")
                    }
                }
                obatAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("KeranjangActivity", "Error fetching obat data: ${error.toException()}")
            }
        })

        // Listener untuk SearchView (perhatikan spasi sebelum OnQueryTextListener)
        binding.searchView.setOnQueryTextListener(object : OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterObatList(newText)
                return true
            }
        })
        // 1. Dapatkan userId (sesuaikan dengan cara Anda menyimpan/mendapatkan data pengguna)
        // Contoh: jika Anda mendapatkannya dari SharedPreferences
        val userId = intent.getStringExtra("userId")
        // 2. Panggil fungsi setProfileImageFromFirebase
        if (userId != null) {
            setProfileImageFromFirebase(binding.ivProfileImage, userId) // Ganti dengan ID ImageView yang benar
        } else {
            // Tangani jika userId tidak ditemukan (misalnya, tampilkan gambar default)
            binding.ivProfileImage.setImageResource(R.drawable.ic_user)
        }

        // Profile Image Logic (Assuming you have a ProfileActivity)
        binding.ivProfileImage.setOnClickListener {
            val intent = Intent(this, ProfilActivity::class.java)
            intent.putExtra("userId", userId)
            startActivity(intent)
        }

        binding.backButton.setOnClickListener {
            // Finish the current activity to go back to the previous one
            finish()
            intent.putExtra("userId", userId)
        }
        binding.btnNext.setOnClickListener {
            val intent = Intent(this,KeranjangDetailActivity::class.java)
            intent.putExtra("userId",userId)
            startActivity(intent)
        }
    }
    private fun filterObatList(query: String?) {
        val filteredList = if (query.isNullOrEmpty()) {
            originalObatList // Tampilkan semua obat jika query kosong
        } else {
            originalObatList.filter { obat ->
                obat.namaObat.contains(query, ignoreCase = true) // Filter berdasarkan nama obat
            }
        }
        obatAdapter.updateObatList(filteredList)
        obatAdapter.notifyDataSetChanged() // Perbarui adapter dengan daftar yang difilter
    }

    fun setProfileImageFromFirebase(imageView: ImageView, userId: String) {
        val database = FirebaseDatabase.getInstance()
        val userRef = database.getReference("users/$userId")

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val imageUrl = dataSnapshot.child("photoUrl").getValue(String::class.java)

                    if (!imageUrl.isNullOrEmpty()) {
                        Glide.with(imageView.context) // Load image using the context of the ImageView
                            .load(imageUrl)
                            .placeholder(R.drawable.ic_user) // Default placeholder image while loading
                            .error(R.drawable.ic_user) // Error image if loading fails
                            .into(imageView)
                    } else {
                        // No image URL, set default
                        imageView.setImageResource(R.drawable.ic_user)
                    }
                } else {
                    // User data not found, set default
                    Log.e("setProfileImageFromFirebase", "User data not found for ID: $userId")
                    imageView.setImageResource(R.drawable.ic_user)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle database error, set default
                Log.e("setProfileImageFromFirebase", "Error fetching user data: ${databaseError.message}")
                imageView.setImageResource(R.drawable.ic_user)
            }
        })
    }

}
