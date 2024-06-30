package com.example.remed.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.remed.R
import com.example.remed.databinding.ItemObatBinding
import com.example.remed.model.Obat
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso

class ObatAdapter(private val obatList: List<Obat>) : RecyclerView.Adapter<ObatAdapter.ObatViewHolder>() {
    private var obatListFiltered: List<Obat> = obatList.toList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ObatViewHolder {
        val binding = ItemObatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ObatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ObatViewHolder, position: Int) {
        val obat = obatList[position]
        holder.bind(obat)
    }

    override fun getItemCount(): Int {
        return obatList.size
    }
    fun updateObatList(newObatList: List<Obat>) {
        obatListFiltered = newObatList
        notifyDataSetChanged()
    }

    inner class ObatViewHolder(private val binding: ItemObatBinding) : RecyclerView.ViewHolder(binding.root) {

        private var isQuantityShown = false
        private var quantity = 0

        init {
            binding.ivAdd.setOnClickListener {
                isQuantityShown = true
                quantity++
                updateQuantityVisibilityAndText()
            }

            binding.ivRemove.setOnClickListener {
                if (quantity > 0) {
                    quantity--
                    if (quantity == 0) {
                        isQuantityShown = false
                    }
                    updateQuantityVisibilityAndText()
                }
            }
        }

        fun bind(obat: Obat) {
            binding.apply {
                tvNamaObat.text = obat.namaObat
                tvNamaKlinik.text = obat.namaKlinik

                // Check stock quantity and update status text
                val stock = obat.jumlahStok.toIntOrNull() ?: 0
                if (stock < 10) {
                    tvStatus.text = "Tersedia (${stock} tersisa)"
                    // Optionally, you can change the background or style based on stock level
//                    itemView.setBackgroundResource(R.drawable.background_low_stock)
                } else {
                    tvStatus.text = "Tersedia"
//                    itemView.setBackgroundResource(R.drawable.background_normal_stock)
                }
                // Load image from Firebase Storage using Picasso
                if (!obat.photoUrl.isNullOrEmpty()) {
                    Picasso.get()
                        .load(obat.photoUrl)
                        .placeholder(R.drawable.ic_persediaanobat) // Placeholder image
                        .into(ivObat)
                } else {
                    // Handle case where photoUrl is empty or null
                    ivObat.setImageResource(R.drawable.ic_persediaanobat)
                }
            }
            // Initial visibility setup:
            isQuantityShown = quantity > 0  // Show if quantity is already greater than 0
            updateQuantityVisibilityAndText()
        }
        private fun updateQuantityVisibilityAndText() {
            binding.tvQuantity.visibility = if (isQuantityShown) View.VISIBLE else View.GONE
            binding.ivRemove.visibility = if (isQuantityShown) View.VISIBLE else View.GONE
            binding.tvQuantity.text = quantity.toString()
        }
    }

}