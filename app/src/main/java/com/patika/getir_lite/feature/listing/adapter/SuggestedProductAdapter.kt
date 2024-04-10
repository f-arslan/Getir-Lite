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
import com.patika.getir_lite.databinding.ItemListingBinding
import com.patika.getir_lite.model.SuggestedProduct
import java.math.BigDecimal
import java.util.Locale

class SuggestedProductAdapter : ListAdapter<SuggestedProduct, RecyclerView.ViewHolder>(ItemDiff) {

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
        fun bind(suggestedProduct: SuggestedProduct) {
            with(binding) {
                tvItemName.text = suggestedProduct.name
                tvItemCount.text = suggestedProduct.count.toString()
                tvItemAttribute.text = suggestedProduct.attribute
                tvItemPrice.text = formatPrice(suggestedProduct.price)
                val imageLoader = ivItem.context.imageLoader
                val request = ImageRequest.Builder(ivItem.context)
                    .data(suggestedProduct.imageURL)
                    .target(ivItem)
                    .build()
                imageLoader.enqueue(request)
                println("${suggestedProduct.name} ${suggestedProduct.imageURL}")
            }
        }

        private fun formatPrice(price: BigDecimal): String = String.format(
            Locale.FRANCE,
            binding.root.context.getString(R.string.price_format),
            price
        )
    }

    fun saveData(suggestedProducts: List<SuggestedProduct>) {
        asyncListDiffer.submitList(suggestedProducts)
    }

    companion object {
        val ItemDiff = object : DiffUtil.ItemCallback<SuggestedProduct>() {
            override fun areItemsTheSame(
                oldItem: SuggestedProduct,
                newItem: SuggestedProduct
            ): Boolean = oldItem.id == newItem.id

            override fun areContentsTheSame(
                oldItem: SuggestedProduct,
                newItem: SuggestedProduct
            ): Boolean = oldItem == newItem
        }
    }
}
