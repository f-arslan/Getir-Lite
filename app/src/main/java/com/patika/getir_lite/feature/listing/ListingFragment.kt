package com.patika.getir_lite.feature.listing

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.GridLayoutManager
import com.facebook.shimmer.ShimmerFrameLayout
import com.patika.getir_lite.ProductViewModel
import com.patika.getir_lite.databinding.FragmentListingBinding
import com.patika.getir_lite.databinding.ItemListingBinding
import com.patika.getir_lite.feature.BaseFragment
import com.patika.getir_lite.feature.adapter.ProductAdapter
import com.patika.getir_lite.feature.adapter.ProductListAdapter
import com.patika.getir_lite.model.BaseResponse
import com.patika.getir_lite.util.decor.GridSpacingItemDecoration
import com.patika.getir_lite.util.ext.formatPrice
import com.patika.getir_lite.util.ext.scopeWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import java.math.BigDecimal

@AndroidEntryPoint
class ListingFragment : BaseFragment<FragmentListingBinding>() {

    private val productViewModel: ProductViewModel by activityViewModels()
    private lateinit var suggestedProductListAdapter: ProductListAdapter
    private lateinit var productAdapter: ProductAdapter<ItemListingBinding>

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentListingBinding =
        FragmentListingBinding.inflate(inflater, container, false)

    override fun FragmentListingBinding.onMain() {
        setupRecycleViewsAndAdapter()
        observeRemoteChanges()
        onBasketClick()
    }

    private fun FragmentListingBinding.onBasketClick() {
        cvTotalPrice.setOnClickListener {
            if (isAdded) {
                findNavController().navigate(
                    ListingFragmentDirections.actionListingFragmentToBasketFragment()
                )
            }
        }
    }

    private fun FragmentListingBinding.setupRecycleViewsAndAdapter() {
        suggestedProductListAdapter = ProductListAdapter(
            events = productViewModel::onEvent,
            onProductClick = ::navigateToDetailFragment
        )

        productAdapter = ProductAdapter(
            bindingInflater = ItemListingBinding::inflate,
            events = productViewModel::onEvent,
            onProductClick = ::navigateToDetailFragment
        )

        val layoutManager = GridLayoutManager(requireContext(), SPAN_COUNT)
        val concatAdapter = ConcatAdapter(
            suggestedProductListAdapter,
            productAdapter
        )

        rvProduct.layoutManager = layoutManager
        rvProduct.adapter = concatAdapter
        rvProduct.addItemDecoration(GridSpacingItemDecoration())
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int = if (position == 0) SPAN_COUNT else 1
        }
    }

    private fun navigateToDetailFragment(productId: Long) {
        if (isAdded) {
            findNavController().navigate(
                ListingFragmentDirections.actionListingFragmentToDetailFragment(productId)
            )
        }
    }

    private fun FragmentListingBinding.observeRemoteChanges() = with(productViewModel) {
        val shimmerLayout = shimmerLayoutProduct.shimmerLayout
        val suggestedShimmerLayout = shimmerLayoutSuggested.shimmerLayout

        scopeWithLifecycle {
            products.collectLatest { response ->
                when (response) {
                    is BaseResponse.Error -> Unit
                    BaseResponse.Loading -> shimmerLayout.startShimmer()

                    is BaseResponse.Success -> {
                        productAdapter.saveData(response.data)
                        closeShimmer(shimmerLayout, suggestedShimmerLayout)
                    }
                }
            }
        }

        scopeWithLifecycle {
            suggestedProducts.collectLatest { response ->
                when (response) {
                    is BaseResponse.Error -> Unit
                    BaseResponse.Loading -> Unit

                    is BaseResponse.Success -> {
                        suggestedProductListAdapter.saveData(response.data)
                    }
                }
            }
        }

        scopeWithLifecycle {
            basket.collectLatest { response ->
                when (response) {
                    is BaseResponse.Error -> cvTotalPrice.visibility = View.GONE

                    BaseResponse.Loading -> cvTotalPrice.visibility = View.GONE

                    is BaseResponse.Success -> response.data?.totalPrice?.let {
                        tvTotalPrice.text = it.formatPrice()
                        cvTotalPrice.visibility =
                            if (it > BigDecimal.ZERO) View.VISIBLE else View.GONE
                    } ?: run {
                        cvTotalPrice.visibility = View.GONE
                    }
                }
            }
        }
    }

    private fun FragmentListingBinding.closeShimmer(
        shimmerLayout: ShimmerFrameLayout,
        suggestedShimmerLayout: ShimmerFrameLayout
    ) {
        shimmerLayout.apply {
            stopShimmer()
            visibility = View.GONE
        }
        suggestedShimmerLayout.apply {
            stopShimmer()
            visibility = View.GONE
        }
        rvProduct.visibility = View.VISIBLE
    }

    override fun onResume() {
        super.onResume()
        scopeWithLifecycle {
            val isEmpty = suggestedProductListAdapter.isProductAdapterEmpty()
            if (isEmpty) {
                val response = productViewModel.suggestedProducts.first()
                if (response is BaseResponse.Success) {
                    suggestedProductListAdapter.saveData(response.data)
                }
            }
        }
    }

    companion object {
        private const val SPAN_COUNT = 3
    }
}
