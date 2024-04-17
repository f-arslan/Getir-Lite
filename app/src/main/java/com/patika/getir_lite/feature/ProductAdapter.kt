package com.patika.getir_lite.feature

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
import com.patika.getir_lite.databinding.ItemBasketBinding
import com.patika.getir_lite.databinding.ItemListingBinding
import com.patika.getir_lite.model.ItemActionType.ONLY_PLUS_IDLE
import com.patika.getir_lite.model.Product
import com.patika.getir_lite.model.ProductEvent
import com.patika.getir_lite.ui.ActionCardView
import com.patika.getir_lite.util.ext.formatPrice
import com.patika.getir_lite.util.ext.toItemActionType

class ProductAdapter<B : ViewBinding>(
    private val bindingInflater: (inflater: LayoutInflater, parent: ViewGroup, attachToParent: Boolean) -> B,
    private val events: (ProductEvent) -> Unit,
    private val onProductClick: (productId: Long) -> Unit = {}
) : ListAdapter<Product, ProductAdapter.ProductViewHolder<B>>(ItemDiff) {

    private val asyncListDiffer = AsyncListDiffer(this, ItemDiff)

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
            is ProductChangePayload.Count -> holder.bindCount(latestPayloads.newCount)
            else -> onBindViewHolder(holder, position)
        }
    }

    override fun getItemCount(): Int = asyncListDiffer.currentList.size

    class ProductViewHolder<B : ViewBinding>(
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
            bindCount(product.count)
        }

        private fun ItemBasketBinding.bindBasketBinding(product: Product) {
            tvItemName.text = product.name
            tvItemAttribute.text = product.attribute
            tvItemPrice.text = product.price.formatPrice()
            ivItem.load(product.imageURL) {
                crossfade(true)
            }
            itemActionView.handleActionOperations(product.entityId)
            bindCount(product.count)
        }

        fun bindCount(count: Int) {
            when (binding) {
                is ItemListingBinding -> {
                    binding.itemActionView.setCount(count)
                    updateCardView(count, binding.itemCard)
                }

                is ItemBasketBinding -> {
                    binding.itemActionView.setCount(count)
                }

                else -> throw IllegalArgumentException("Unknown binding type")
            }
        }

        @SuppressLint("ResourceType")
        fun updateCardView(
            count: Int,
            itemCard: MaterialCardView,
        ) {
            val itemActionType = count.toItemActionType()
            itemCard.strokeColor = if (itemActionType != ONLY_PLUS_IDLE) {
                itemColors.getColor(0, 0)
            } else {
                itemColors.getColor(1, 0)
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
