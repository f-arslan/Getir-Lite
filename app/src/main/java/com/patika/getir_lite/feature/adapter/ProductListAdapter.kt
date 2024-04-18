package com.patika.getir_lite.feature.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.children
import androidx.recyclerview.widget.RecyclerView
import com.patika.getir_lite.databinding.ItemListingBinding
import com.patika.getir_lite.databinding.SuggestedProductBinding
import com.patika.getir_lite.model.Product
import com.patika.getir_lite.model.ProductEvent
import com.patika.getir_lite.util.decor.MarginItemDecoration

class ProductListAdapter(
    events: (ProductEvent) -> Unit,
    onProductClick: (Long) -> Unit
) : RecyclerView.Adapter<ProductListAdapter.ProductListViewHolder>() {

    val productAdapter = ProductAdapter(
        bindingInflater = ItemListingBinding::inflate,
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
                if (adapter == null) {
                    loadViews(this)
                    adapter = productAdapter
                    addItemDecoration(MarginItemDecoration())
                }
            }
        }
    }

    private fun loadViews(recyclerView: RecyclerView) {
        recyclerView.let {
            val parent = it.parent as ViewGroup
            val children = parent.children
            children.forEach { child ->
                child.visibility = View.VISIBLE
            }
        }
    }

    fun saveData(data: List<Product>) {
        productAdapter.saveData(data)
    }

    fun isProductAdapterEmpty(): Boolean = productAdapter.itemCount == 0
}
