package com.example.remed

import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.bumptech.glide.Glide
import com.example.remed.databinding.JadwalActivityBinding
import com.example.remed.model.Notifikasi
import com.example.remed.services.ReminderReceiver
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.*

class JadwalActivity : AppCompatActivity() {

    private lateinit var binding: JadwalActivityBinding
    private lateinit var auth: FirebaseAuth
    private val alarmManager by lazy { getSystemService(Context.ALARM_SERVICE) as AlarmManager }
    private val notificationManager by lazy { getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = JadwalActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.etTanggal.setOnClickListener {
            showDatePickerDialog()
        }

        binding.etJam.setOnClickListener {
            showTimePickerDialog()
        }
        binding.btnSimpan.setOnClickListener { simpanPengingat() }

        val userId = intent.getStringExtra("userId")
        if (userId != null) {
            setProfileImageFromFirebase(binding.ivProfileImage, userId)
        } else {
            binding.ivProfileImage.setImageResource(R.drawable.ic_user)
        }

        binding.ivProfileImage.setOnClickListener {
            val intent = Intent(this, ProfilActivity::class.java)
            intent.putExtra("userId", userId)
            startActivity(intent)
        }

        binding.backButton.setOnClickListener {
            finish()
            intent.putExtra("userId", userId)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1)
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "reminder_channel",
                "Reminder Channel",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun simpanPengingat() {
        val userId = auth.currentUser?.uid ?: run {
            Toast.makeText(this, "Anda harus login terlebih dahulu", Toast.LENGTH_SHORT).show()
            return
        }
        val namaObat = binding.etNamaObat.text.toString().trim()
        val tanggal = binding.etTanggal.text.toString().trim()
        val jam = binding.etJam.text.toString().trim()
        val catatan = binding.etCatatan.text.toString().trim()

        if (namaObat.isEmpty() || tanggal.isEmpty() || jam.isEmpty()) {
            Toast.makeText(this, "Harap isi semua field", Toast.LENGTH_SHORT).show()
            return
        }

        val tanggalJamString = "$tanggal $jam"

        try {
            val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val tanggalJam = dateFormat.parse(tanggalJamString) ?: run {
                Toast.makeText(this, "Format tanggal/jam tidak valid", Toast.LENGTH_SHORT).show()
                return
            }

            if (tanggalJam.before(Date())) {
                Toast.makeText(this, "Waktu pengingat harus di masa depan", Toast.LENGTH_SHORT).show()
                return
            }

            val notifikasi = Notifikasi(namaObat, tanggal, jam, catatan)

            val database = FirebaseDatabase.getInstance()
            val pengingatRef = database.getReference("pengingat_obat/$userId").push()
            pengingatRef.setValue(notifikasi)
                .addOnSuccessListener {
                    Toast.makeText(this, "Pengingat berhasil disimpan", Toast.LENGTH_SHORT).show()
                    scheduleAlarm(tanggalJam, notifikasi)
                }
                .addOnFailureListener { e ->
                    Log.e("SimpanPengingat", "Error saving reminder: ${e.message}")
                    Toast.makeText(this, "Gagal menyimpan pengingat", Toast.LENGTH_SHORT).show()
                }
        } catch (e: Exception) {
            Log.e("SimpanPengingat", "Error parsing date/time: ${e.message}")
            Toast.makeText(this, "Format tanggal/jam tidak valid", Toast.LENGTH_SHORT).show()
        }
    }

    private fun scheduleAlarm(waktu: Date, notifikasi: Notifikasi) {
        val alarmIntent = Intent(this, ReminderReceiver::class.java).apply {
            putExtra("namaObat", notifikasi.namaObat)
            putExtra("tanggal", notifikasi.tanggal)
            putExtra("jam", notifikasi.jam)
            putExtra("catatan", notifikasi.catatan)
        }
        val pendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, PendingIntent.FLAG_IMMUTABLE)
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, waktu.time, pendingIntent)
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            val selectedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
            binding.etTanggal.setText(selectedDate)
        }, year, month, day)

        datePickerDialog.show()
    }

    private fun showTimePickerDialog() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(this, { _, selectedHour, selectedMinute ->
            val selectedTime = String.format("%02d:%02d", selectedHour, selectedMinute)
            binding.etJam.setText(selectedTime)
        }, hour, minute, true)

        timePickerDialog.show()
    }

    fun setProfileImageFromFirebase(imageView: ImageView, userId: String) {
        val database = FirebaseDatabase.getInstance()
        val userRef = database.getReference("users/$userId")

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val imageUrl = dataSnapshot.child("photoUrl").getValue(String::class.java)

                    if (!imageUrl.isNullOrEmpty()) {
                        Glide.with(imageView.context)
                            .load(imageUrl)
                            .placeholder(R.drawable.ic_user)
                            .error(R.drawable.ic_user)
                            .into(imageView)
                    } else {
                        imageView.setImageResource(R.drawable.ic_user)
                    }
                } else {
                    Log.e("setProfileImageFromFirebase", "User data not found for ID: $userId")
                    imageView.setImageResource(R.drawable.ic_user)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("setProfileImageFromFirebase", "Error fetching user data: ${databaseError.message}")
                imageView.setImageResource(R.drawable.ic_user)
            }
        })
    }
}