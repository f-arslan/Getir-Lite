package com.patika.getir_lite.feature.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.children
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.patika.getir_lite.databinding.SuggestedProductBinding
import com.patika.getir_lite.model.ProductEvent
import com.patika.getir_lite.model.ProductWithCount
import com.patika.getir_lite.util.decor.MarginItemDecoration

/**
 * A RecyclerView adapter designed to handle a single item containing a nested list of products, specifically for suggested products.
 * It uses an inner [ProductAdapter] to manage the actual list of products.
 */
class ProductListAdapter(
    events: (ProductEvent) -> Unit,
    onProductClick: (Long) -> Unit
) : RecyclerView.Adapter<ProductListAdapter.ProductListViewHolder>() {

    val productAdapter = ProductAdapter(
        viewType = ProductAdapter.LISTING_VIEW_TYPE,
        events = events,
        onProductClick = onProductClick
    )

    override fun getItemCount(): Int = 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductListViewHolder {
        val binding =
            SuggestedProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProductListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductListViewHolder, position: Int) {
        holder.bind()
    }

    inner class ProductListViewHolder(private val binding: SuggestedProductBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            binding.rvSuggestedProduct.apply {
                itemAnimator = null
                if (adapter == null) {
                    layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
                    adapter = productAdapter
                    isNestedScrollingEnabled = false
                    loadViews(this)
                    addItemDecoration(MarginItemDecoration())
                }
            }
        }
    }

    /**
     * Reveals all views within the RecyclerView's parent, typically used to show elements that were conditionally hidden.
     *
     * @param recyclerView The RecyclerView whose parent's child views will be made visible.
     */
    private fun loadViews(recyclerView: RecyclerView) {
        recyclerView.let {
            val parent = it.parent as ViewGroup
            val children = parent.children
            children.forEach { child ->
                child.visibility = View.VISIBLE
            }
        }
    }

    /**
     * Passes a new list of products to the inner [ProductAdapter] to display.
     *
     * @param data The list of [ProductWithCount] items to be displayed.
     */
    fun saveData(data: List<ProductWithCount>) {
        productAdapter.saveData(data)
    }

    fun isProductAdapterEmpty(): Boolean = productAdapter.itemCount == 0
}
