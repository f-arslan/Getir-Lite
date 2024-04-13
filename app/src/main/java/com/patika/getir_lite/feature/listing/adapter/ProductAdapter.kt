package com.patika.getir_lite.feature.listing.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.imageLoader
import coil.request.ImageRequest
import com.patika.getir_lite.R
import com.patika.getir_lite.databinding.ActionButtonsBinding
import com.patika.getir_lite.databinding.ItemListingBinding
import com.patika.getir_lite.model.Product
import java.math.BigDecimal
import java.util.Locale

class ProductAdapter : ListAdapter<Product, RecyclerView.ViewHolder>(ItemDiff) {

    private val asyncListDiffer = AsyncListDiffer(this, ItemDiff)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemListingBinding.inflate(inflater, parent, false)
        return SuggestedProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val suggestedProduct = asyncListDiffer.currentList[position]
        (holder as SuggestedProductViewHolder).bind(suggestedProduct)
    }

    override fun getItemCount(): Int = asyncListDiffer.currentList.size

    inner class SuggestedProductViewHolder(private val binding: ItemListingBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(product: Product) {
            with(binding) {
                tvItemName.text = product.name
                layoutLlActionButtons.tvItemCount.text = product.count.toString()
                tvItemAttribute.text = product.attribute
                tvItemPrice.text = formatPrice(product.price)
                val imageLoader = ivItem.context.imageLoader
                val request = ImageRequest.Builder(ivItem.context)
                    .data(product.imageURL)
                    .target(ivItem)
                    .build()
                imageLoader.enqueue(request)
            }
        }

        private fun formatPrice(price: BigDecimal): String = String.format(
            Locale.FRANCE,
            binding.root.context.getString(R.string.price_format),
            price
        )
    }

    fun saveData(product: List<Product>) {
        asyncListDiffer.submitList(product)
    }

    companion object {
        val ItemDiff = object : DiffUtil.ItemCallback<Product>() {
            override fun areItemsTheSame(
                oldItem: Product,
                newItem: Product
            ): Boolean = oldItem.id == newItem.id

            override fun areContentsTheSame(
                oldItem: Product,
                newItem: Product
            ): Boolean = oldItem == newItem
        }
    }
}
