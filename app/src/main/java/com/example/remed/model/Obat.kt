package com.example.remed.model

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Obat(
    val id: String = "",
    val namaObat: String = "",
    val namaKlinik: String = "",
    val jumlahStok: String = "",
    val photoUrl: String? = null
) {
    constructor() : this("", "", "", "", "")
}