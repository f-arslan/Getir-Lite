package com.patika.getir_lite.feature.listing.adapter

import android.annotation.SuppressLint
import android.content.res.TypedArray
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.patika.getir_lite.databinding.ItemActionCardBinding
import com.patika.getir_lite.databinding.ItemListingBinding
import com.patika.getir_lite.model.ItemActionType
import com.patika.getir_lite.model.ItemActionType.ONLY_PLUS
import com.patika.getir_lite.model.ItemActionType.PLUS_DELETE
import com.patika.getir_lite.model.ItemActionType.PLUS_MINUS
import com.patika.getir_lite.model.Product
import com.patika.getir_lite.model.ProductEvent
import com.patika.getir_lite.util.ext.formatPrice
import com.patika.getir_lite.util.ext.setVisibility
import com.patika.getir_lite.util.ext.toItemActionType

class ProductAdapter(private val events: (ProductEvent) -> Unit) :
    ListAdapter<Product, ProductAdapter.SuggestedProductViewHolder>(ItemDiff) {

    private val asyncListDiffer = AsyncListDiffer(this, ItemDiff)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SuggestedProductViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemListingBinding.inflate(inflater, parent, false)
        return SuggestedProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SuggestedProductViewHolder, position: Int) {
        val suggestedProduct = asyncListDiffer.currentList[position]
        holder.bind(suggestedProduct)
    }

    override fun onBindViewHolder(
        holder: SuggestedProductViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        when (val latestPayloads = payloads.lastOrNull()) {
            is ProductChangePayload.Count -> holder.bindCount(latestPayloads.newCount)
            else -> onBindViewHolder(holder, position)
        }
    }

    override fun getItemCount(): Int = asyncListDiffer.currentList.size

    inner class SuggestedProductViewHolder(private val binding: ItemListingBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(product: Product) {
            with(binding) {
                tvItemName.text = product.name

                bindCount(product.count)
                tvItemAttribute.text = product.attribute
                tvItemPrice.text = product.price.formatPrice()
                ivItem.load(product.imageURL) {
                    crossfade(true)
                }

                layoutActionButtons.handleActionOperations(product.entityId)
            }
        }

        private fun ItemActionCardBinding.handleActionOperations(entityId: Long) {
            btnAdd.setOnClickListener {
                events(ProductEvent.OnPlusClick(entityId))
            }
            btnMinus.setOnClickListener {
                events(ProductEvent.OnMinusClick(entityId))
            }
            btnDelete.setOnClickListener {
                events(ProductEvent.OnDeleteClick(entityId))
            }
        }

        @SuppressLint("ResourceType")
        internal fun bindCount(count: Int) {
            binding.layoutActionButtons.tvItemCount.text = count.toString()
            val itemActionType = count.toItemActionType()
            binding.layoutActionButtons.setButtonVisibility(itemActionType)
            if (itemActionType != ONLY_PLUS) {
                binding.itemCard.strokeColor = itemColors.getColor(0, 0)
            } else {
                binding.itemCard.strokeColor = itemColors.getColor(1, 0)
            }
        }

        private fun ItemActionCardBinding.setButtonVisibility(itemActionType: ItemActionType) {
            btnDelete.setVisibility(itemActionType == PLUS_DELETE)
            tvItemCount.setVisibility(itemActionType != ONLY_PLUS)
            btnMinus.setVisibility(itemActionType == PLUS_MINUS)
            btnAdd.setVisibility(true)
        }


        private val itemColors: TypedArray =
            binding.root.context.obtainStyledAttributes(
                intArrayOf(
                    com.google.android.material.R.attr.colorPrimary,
                    com.google.android.material.R.attr.colorTertiary
                )
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

            override fun getChangePayload(oldItem: Product, newItem: Product): Any? {
                return when {
                    oldItem.count != newItem.count -> {
                        ProductChangePayload.Count(newItem.count)
                    }

                    else -> super.getChangePayload(oldItem, newItem)
                }
            }
        }
    }

    private sealed interface ProductChangePayload {
        data class Count(val newCount: Int) : ProductChangePayload
    }
}
