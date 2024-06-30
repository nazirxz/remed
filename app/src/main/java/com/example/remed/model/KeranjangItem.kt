package com.example.remed.model

import android.os.Parcel
import android.os.Parcelable
import java.io.Serializable

data class KeranjangItem(
    val namaObat: String? ="",
    val photoUrl: String? = null,
    val totalPesanan: Int = 0,
    val metodePembayaran: String? = "",
    val layananPengiriman: String? =""
): Serializable, Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readInt(),
        parcel.readString(),
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(namaObat)
        parcel.writeString(photoUrl)
        parcel.writeInt(totalPesanan)
        parcel.writeString(metodePembayaran)
        parcel.writeString(layananPengiriman)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<KeranjangItem> {
        override fun createFromParcel(parcel: Parcel): KeranjangItem {
            return KeranjangItem(parcel)
        }

        override fun newArray(size: Int): Array<KeranjangItem?> {
            return arrayOfNulls(size)
        }
    }
}
