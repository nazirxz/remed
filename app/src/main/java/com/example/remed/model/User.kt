package com.example.remed

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    var name: String? = null, // Tambahkan default value null
    var gender: String? = null,
    var age: String? = null,
    var medicalHistory: String? = null,
    var emailPhone: String? = null,
    var address: String? = null,
    var controlSchedule: String? = null,
    var uid: String? = null,
    var photoUrl: String? = null
) : Parcelable
