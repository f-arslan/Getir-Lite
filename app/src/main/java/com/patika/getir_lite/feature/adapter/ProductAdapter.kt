package com.patika.getir_lite.feature.adapter

import android.annotation.SuppressLint
import android.content.res.TypedArray
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
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

/**
 * A RecyclerView adapter for handling the display of products with varying view types based on the context (e.g., listing, basket).
 * The adapter provides functionality to handle user interactions and dynamically update the UI based on product count changes.
 *
 * @property viewType An integer representing the type of view that will be used for items in this adapter.
 * @property events A function to handle product-related events (e.g., add, remove, delete).
 * @property onProductClick A function to handle clicking on a product, typically used to navigate to a detail view.
 */
class ProductAdapter(
    private val viewType: Int,
    private val events: (ProductEvent) -> Unit,
    private val onProductClick: (productId: Long) -> Unit
) : ListAdapter<ProductWithCount, ProductAdapter.ProductViewHolder>(ItemDiff) {

    /**
     * Holds the item count status for animation when product counts change.
     */
    data class ItemCountStatus(
        val prev: Int = 0,
        val current: Int = 0
    )

    /**
     * Provides the appropriate view type for items in the adapter based on the predefined view type.
     */
    override fun getItemViewType(position: Int): Int = viewType

    private val asyncListDiffer = AsyncListDiffer(this, ItemDiff)  // Manages list diffing to optimize updates.
    private val itemCountStatusMap = HashMap<Long, ItemCountStatus>()  // Tracks count changes for products to animate or adjust UI.

    override fun getItemCount(): Int = asyncListDiffer.currentList.size

    /**
     * Creates the appropriate ViewHolder based on the viewType.
     * Supports different layouts for products depending on their context (listing or basket).
     */
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

    /**
     * Binds the ViewHolder with data and payloads for partial updates, optimizing specific property changes.
     */
    override fun onBindViewHolder(
        holder: ProductViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        when (val latestPayloads = payloads.lastOrNull()) {
            is ProductChangePayload.Count -> holder.bindCount(
                count = latestPayloads.newCount,
                productId = latestPayloads.productId
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

            binding.root.isClickable = true
            itemActionView.enableButtons()
            itemActionView.handleActionOperations(product.productId, product.count, binding)

            binding.root.setOnClickListener {
                onProductClick(product.productId)
            }
            bindCount(product.count, product.productId)
        }

        private fun ItemBasketBinding.bindBasketBinding(product: ProductWithCount) {
            tvItemName.text = product.name
            tvItemAttribute.text = product.attribute
            tvItemAttribute.isVisible = product.attribute?.isNotBlank() ?: false
            tvItemPrice.text = product.price.formatPrice()
            ivItem.load(product.imageURL) {
                crossfade(true)
            }

            binding.root.isClickable = true
            itemActionView.enableButtons()
            itemActionView.handleActionOperations(product.productId, product.count, binding)
            binding.root.setOnClickListener {
                onProductClick(product.productId)
            }
            bindCount(count = product.count)
        }

        fun bindCount(count: Int, productId: Long = -1) {
            when (binding) {
                is ItemListingBinding -> {
                    binding.apply { root.isClickable = true }.itemActionView.apply {
                        enableButtons()
                        setActionVisibilityAndCount(count)
                        handleActionOperations(productId, count, binding)
                    }
                    updateCardView(binding.itemCard, productId)
                }

                is ItemBasketBinding -> {
                    binding.apply { root.isClickable = true }.itemActionView.enableButtons()
                    binding.itemActionView.setActionVisibilityAndCount(count)
                }

                else -> throw IllegalArgumentException("Unknown binding type")
            }
        }

        @SuppressLint("ResourceType")
        fun updateCardView(
            itemCard: MaterialCardView,
            productId: Long
        ) {
            val borderView = itemCard.findViewById<BorderView>(R.id.view_card_border)
            val status = itemCountStatusMap.getOrDefault(productId, ItemCountStatus())
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
                        itemCountStatusMap[productId] = ItemCountStatus(1, 1)
                    }

                    else -> configureCard(0, false)
                }

                else -> configureCard(0, false)
            }
        }

        private fun ActionCardView.handleActionOperations(
            productId: Long,
            count: Int,
            binding: ViewBinding
        ) {
            setOnDeleteClickListener {
                binding.root.isClickable = false
                disableButtons()
                events(ProductEvent.OnDeleteClick(productId))
            }
            setOnPlusClickListener {
                binding.root.isClickable = false
                disableButtons()
                events(ProductEvent.OnPlusClick(productId, count))
            }
            setOnMinusClickListener {
                binding.root.isClickable = false
                disableButtons()
                events(ProductEvent.OnMinusClick(productId, count))
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

    fun saveData(products: List<ProductWithCount>) {
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
        asyncListDiffer.submitList(products)
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
        data class Count(val newCount: Int, val productId: Long) : ProductChangePayload
    }
}
