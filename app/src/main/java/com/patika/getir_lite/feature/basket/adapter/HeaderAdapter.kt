package com.patika.getir_lite.feature.basket.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.patika.getir_lite.databinding.ListHeaderBinding

class HeaderAdapter(private val header: String) :
    RecyclerView.Adapter<HeaderAdapter.HeaderViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HeaderViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ListHeaderBinding.inflate(layoutInflater, parent, false)
        return HeaderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HeaderViewHolder, position: Int) {
        holder.bind()
    }

    inner class HeaderViewHolder(private val binding: ListHeaderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            binding.tvHeader.text = header
        }
    }

    override fun getItemCount(): Int = 1
}
