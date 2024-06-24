package com.example.remed

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    val name: String,
    val gender: String,
    val age: String,
    val medicalHistory: String,
    val emailPhone: String,
    val address: String,
    val controlSchedule: String,
    val uid: String
) : Parcelable
