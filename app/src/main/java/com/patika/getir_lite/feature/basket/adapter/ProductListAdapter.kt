package com.patika.getir_lite.feature.basket.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.patika.getir_lite.databinding.ItemListingBinding
import com.patika.getir_lite.databinding.SuggestedProductBinding
import com.patika.getir_lite.feature.ProductAdapter
import com.patika.getir_lite.model.Product
import com.patika.getir_lite.model.ProductEvent
import com.patika.getir_lite.util.decor.MarginItemDecoration

class ProductListAdapter(
    events: (ProductEvent) -> Unit,
    onProductClick: (Long) -> Unit
) :
    RecyclerView.Adapter<ProductListAdapter.HorizontalAdapterViewHolder>() {

    val productAdapter = ProductAdapter(
        bindingInflater = ItemListingBinding::inflate,
        events = events,
        onProductClick = onProductClick
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HorizontalAdapterViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = SuggestedProductBinding.inflate(layoutInflater, parent, false)
        return HorizontalAdapterViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HorizontalAdapterViewHolder, position: Int) {
        holder.bind()
    }

    override fun getItemCount(): Int = 1

    inner class HorizontalAdapterViewHolder(private val binding: SuggestedProductBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            binding.rvSuggestedProduct.apply {
                adapter = productAdapter
                addItemDecoration(MarginItemDecoration())
            }
        }
    }

    fun saveData(data: List<Product>) {
        productAdapter.saveData(data)
    }
}
