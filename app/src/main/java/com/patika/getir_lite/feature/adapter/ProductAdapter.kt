package com.patika.getir_lite.feature.adapter

import android.annotation.SuppressLint
import android.content.res.TypedArray
import android.view.LayoutInflater
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
import com.patika.getir_lite.model.ProductEvent
import com.patika.getir_lite.model.ProductWithCount
import com.patika.getir_lite.ui.ActionCardView
import com.patika.getir_lite.ui.BorderView
import com.patika.getir_lite.util.ext.formatPrice
import com.patika.getir_lite.util.ext.setVisibility

class ProductAdapter(
    private val viewType: Int,
    private val events: (ProductEvent) -> Unit,
    private val onProductClick: (productId: Long) -> Unit
) : ListAdapter<ProductWithCount, ProductAdapter.ProductViewHolder>(ItemDiff) {

    data class ItemCountStatus(
        val prev: Int = 0,
        val current: Int = 0,
    )

    override fun getItemViewType(position: Int): Int {
        return viewType
    }

    private val asyncListDiffer = AsyncListDiffer(this, ItemDiff)
    private val itemCountStatusMap = HashMap<Long, ItemCountStatus>()

    override fun getItemCount(): Int = asyncListDiffer.currentList.size
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            LISTING_VIEW_TYPE -> {
                val binding = ItemListingBinding.inflate(inflater, parent, false)
                ProductViewHolder(binding, events, onProductClick)
            }

            BASKET_VIEW_TYPE -> {
                val binding = ItemBasketBinding.inflate(inflater, parent, false)
                ProductViewHolder(binding, events, onProductClick)
            }

            else -> throw IllegalArgumentException("Unknown view type")
        }
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val suggestedProduct = asyncListDiffer.currentList[position]
        holder.bind(suggestedProduct)
    }

    override fun onBindViewHolder(
        holder: ProductViewHolder,
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

    inner class ProductViewHolder(
        private val binding: ViewBinding,
        private val events: (ProductEvent) -> Unit,
        private val onProductClick: (productId: Long) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(product: ProductWithCount) {
            when (binding) {
                is ItemListingBinding -> binding.bindListingBinding(product)
                is ItemBasketBinding -> binding.bindBasketBinding(product)
                else -> throw IllegalArgumentException("Unknown binding type")
            }
        }

        private fun ItemListingBinding.bindListingBinding(product: ProductWithCount) {
            tvItemName.text = product.name
            tvItemAttribute.text = product.attribute
            tvItemPrice.text = product.price.formatPrice()
            ivItem.load(product.imageURL) {
                crossfade(true)
            }
            itemActionView.handleActionOperations(product.productId, product.count)
            binding.root.setOnClickListener {
                onProductClick(product.productId)
            }
            bindCount(product.count, product.productId)
        }

        private fun ItemBasketBinding.bindBasketBinding(product: ProductWithCount) {
            tvItemName.text = product.name
            tvItemAttribute.text = product.attribute
            tvItemPrice.text = product.price.formatPrice()
            ivItem.load(product.imageURL) {
                crossfade(true)
            }
            itemActionView.handleActionOperations(product.productId, product.count)
            binding.root.setOnClickListener {
                onProductClick(product.productId)
            }
            bindCount(product.count)
        }

        fun bindCount(count: Int, entityId: Long = 0) {
            when (binding) {
                is ItemListingBinding -> {
                    binding.itemActionView.handleActionOperations(entityId, count)
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

        private fun ActionCardView.handleActionOperations(entityId: Long, count: Int) {
            setOnDeleteClickListener {
                events(ProductEvent.OnDeleteClick(entityId))
            }
            setOnPlusClickListener {
                events(ProductEvent.OnPlusClick(entityId, count))
            }
            setOnMinusClickListener {
                events(ProductEvent.OnMinusClick(entityId, count))
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

    fun saveData(
        products: List<ProductWithCount>,
        recyclerView: RecyclerView? = null,
        onListSubmit: () -> Unit = {},
    ) {
        for (i in products.indices) {
            val product = products[i]
            val itemStatus =
                itemCountStatusMap.putIfAbsent(
                    product.productId,
                    ItemCountStatus(product.count, product.count)
                )
            itemStatus?.let {
                val newEntry = ItemCountStatus(itemStatus.current, product.count)
                itemCountStatusMap[product.productId] = newEntry
            }
        }
        asyncListDiffer.submitList(products) {
            onListSubmit()
            recyclerView?.scrollToPosition(products.size - 1)
        }
    }

    companion object {
        const val LISTING_VIEW_TYPE = 1
        const val BASKET_VIEW_TYPE = 2

        val ItemDiff = object : DiffUtil.ItemCallback<ProductWithCount>() {
            override fun areItemsTheSame(
                oldItem: ProductWithCount,
                newItem: ProductWithCount
            ): Boolean = oldItem.productId == newItem.productId

            override fun areContentsTheSame(
                oldItem: ProductWithCount,
                newItem: ProductWithCount
            ): Boolean = oldItem == newItem

            override fun getChangePayload(
                oldItem: ProductWithCount,
                newItem: ProductWithCount
            ): Any? {
                return when {
                    oldItem.count != newItem.count -> {
                        ProductChangePayload.Count(newItem.count, newItem.productId)
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
