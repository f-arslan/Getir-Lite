package com.patika.getir_lite.feature.adapter

import android.annotation.SuppressLint
import android.content.res.TypedArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import coil.load
import com.google.android.material.card.MaterialCardView
import com.patika.getir_lite.R
import com.patika.getir_lite.databinding.ItemBasketBinding
import com.patika.getir_lite.databinding.ItemListingBinding
import com.patika.getir_lite.model.Product
import com.patika.getir_lite.model.ProductEvent
import com.patika.getir_lite.ui.ActionCardView
import com.patika.getir_lite.ui.BorderView
import com.patika.getir_lite.util.ext.formatPrice
import com.patika.getir_lite.util.ext.setVisibility

class ProductAdapter<B : ViewBinding>(
    private val bindingInflater: (inflater: LayoutInflater, parent: ViewGroup, attachToParent: Boolean) -> B,
    private val events: (ProductEvent) -> Unit,
    private val onProductClick: (productId: Long) -> Unit
) : ListAdapter<Product, ProductAdapter<B>.ProductViewHolder<B>>(ItemDiff) {

    data class ItemCountStatus(
        val prev: Int = 0,
        val current: Int = 0,
    )

    private val asyncListDiffer = AsyncListDiffer(this, ItemDiff)
    val itemCountStatusMap = HashMap<Long, ItemCountStatus>()

    override fun getItemCount(): Int = asyncListDiffer.currentList.size
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder<B> {
        val inflater = LayoutInflater.from(parent.context)
        val binding = bindingInflater(inflater, parent, false)
        return ProductViewHolder(binding, events, onProductClick)
    }

    override fun onBindViewHolder(holder: ProductViewHolder<B>, position: Int) {
        val suggestedProduct = asyncListDiffer.currentList[position]
        holder.bind(suggestedProduct)
    }

    override fun onBindViewHolder(
        holder: ProductViewHolder<B>,
        position: Int,
        payloads: MutableList<Any>
    ) {
        when (val latestPayloads = payloads.lastOrNull()) {
            is ProductChangePayload.Count -> holder.bindCount(
                latestPayloads.newCount,
                latestPayloads.entityId
            )

            else -> onBindViewHolder(holder, position)
        }
    }

    inner class ProductViewHolder<B : ViewBinding>(
        private val binding: B,
        private val events: (ProductEvent) -> Unit,
        private val onProductClick: (productId: Long) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(product: Product) {
            when (binding) {
                is ItemListingBinding -> binding.bindListingBinding(product)
                is ItemBasketBinding -> binding.bindBasketBinding(product)
                else -> throw IllegalArgumentException("Unknown binding type")
            }
        }

        private fun ItemListingBinding.bindListingBinding(product: Product) {
            tvItemName.text = product.name
            tvItemAttribute.text = product.attribute
            tvItemPrice.text = product.price.formatPrice()
            ivItem.load(product.imageURL) {
                crossfade(true)
            }
            itemActionView.handleActionOperations(product.entityId)
            binding.root.setOnClickListener {
                onProductClick(product.entityId)
            }
            bindCount(product.count, product.entityId)
        }

        private fun ItemBasketBinding.bindBasketBinding(product: Product) {
            tvItemName.text = product.name
            tvItemAttribute.text = product.attribute
            tvItemPrice.text = product.price.formatPrice()
            ivItem.load(product.imageURL) {
                crossfade(true)
            }
            itemActionView.handleActionOperations(product.entityId)
            binding.root.setOnClickListener {
                onProductClick(product.entityId)
            }
            bindCount(product.count)
        }

        fun bindCount(count: Int, entityId: Long = 0) {
            when (binding) {
                is ItemListingBinding -> {
                    binding.itemActionView.setCount(count)
                    updateCardView(binding.itemCard, entityId)
                }

                is ItemBasketBinding -> {
                    binding.itemActionView.setCount(count)
                }

                else -> throw IllegalArgumentException("Unknown binding type")
            }
        }

        @SuppressLint("ResourceType")
        fun updateCardView(
            itemCard: MaterialCardView,
            entityId: Long
        ) {
            val borderView = itemCard.findViewById<BorderView>(R.id.view_card_border)
            val status = itemCountStatusMap.getOrDefault(entityId, ItemCountStatus())
            val prev = status.prev
            val current = status.current
            val density = itemCard.resources.displayMetrics.density

            fun configureCard(strokeColorIndex: Int, isVisible: Boolean) {
                itemCard.apply {
                    strokeColor = itemColors.getColor(strokeColorIndex, 0)
                    strokeWidth = density.toInt()
                    radius = 16 * density
                }
                borderView.setVisibility(isVisible)
            }

            when (current) {
                0 -> configureCard(1, false)
                1 -> when (prev) {
                    0 -> {
                        borderView.setVisibility(true)
                        itemCard.strokeWidth = 0
                        borderView.startAnimation()
                        itemCountStatusMap[entityId] = ItemCountStatus(1, 1)
                    }

                    else -> configureCard(0, false)
                }

                else -> configureCard(0, false)
            }
        }

        private fun ActionCardView.handleActionOperations(entityId: Long) {
            setOnDeleteClickListener {
                events(ProductEvent.OnDeleteClick(entityId))
            }
            setOnPlusClickListener {
                events(ProductEvent.OnPlusClick(entityId))
            }
            setOnMinusClickListener {
                events(ProductEvent.OnMinusClick(entityId))
            }
        }

        private val itemColors: TypedArray =
            binding.root.context.obtainStyledAttributes(
                intArrayOf(
                    com.google.android.material.R.attr.colorPrimary,
                    com.google.android.material.R.attr.colorTertiary
                )
            )
    }

    fun saveData(products: List<Product>) {
        for (i in products.indices) {
            val product = products[i]
            val itemStatus =
                itemCountStatusMap.putIfAbsent(
                    product.entityId,
                    ItemCountStatus(product.count, product.count)
                )
            itemStatus?.let {
                val newEntry = ItemCountStatus(itemStatus.current, product.count)
                itemCountStatusMap[product.entityId] = newEntry
            }
        }
        asyncListDiffer.submitList(products)
    }

    companion object {
        val ItemDiff = object : DiffUtil.ItemCallback<Product>() {
            override fun areItemsTheSame(
                oldItem: Product,
                newItem: Product
            ): Boolean = oldItem.entityId == newItem.entityId

            override fun areContentsTheSame(
                oldItem: Product,
                newItem: Product
            ): Boolean = oldItem == newItem

            override fun getChangePayload(oldItem: Product, newItem: Product): Any? {
                return when {
                    oldItem.count != newItem.count -> {
                        ProductChangePayload.Count(newItem.count, newItem.entityId)
                    }

                    else -> super.getChangePayload(oldItem, newItem)
                }
            }
        }
    }

    private sealed interface ProductChangePayload {
        data class Count(val newCount: Int, val entityId: Long) : ProductChangePayload
    }
}
