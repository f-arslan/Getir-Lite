package com.patika.getir_lite.feature.listing

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.patika.getir_lite.ProductViewModel
import com.patika.getir_lite.databinding.FragmentListingBinding
import com.patika.getir_lite.databinding.ItemListingBinding
import com.patika.getir_lite.feature.BaseFragment
import com.patika.getir_lite.feature.ProductAdapter
import com.patika.getir_lite.model.BaseResponse
import com.patika.getir_lite.util.decor.GridSpacingItemDecoration
import com.patika.getir_lite.util.decor.MarginItemDecoration
import com.patika.getir_lite.util.ext.formatPrice
import com.patika.getir_lite.util.ext.scopeWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import java.math.BigDecimal

@AndroidEntryPoint
class ListingFragment : BaseFragment<FragmentListingBinding>() {

    private val productViewModel: ProductViewModel by activityViewModels()
    private lateinit var suggestedProductAdapter: ProductAdapter<ItemListingBinding>
    private lateinit var productAdapter: ProductAdapter<ItemListingBinding>

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentListingBinding =
        FragmentListingBinding.inflate(inflater, container, false)

    override fun FragmentListingBinding.onMain() {
        observeRemoteChanges()
        setupRecycleViewsAndAdapter()
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
        suggestedProductAdapter = ProductAdapter(
            bindingInflater = ItemListingBinding::inflate,
            events = productViewModel::onEvent,
            onProductClick = ::navigateToDetailFragment
        )
        layoutSuggestedProduct.rvSuggestedProduct.apply {
            adapter = suggestedProductAdapter
            addItemDecoration(MarginItemDecoration())
        }
        productAdapter = ProductAdapter(
            bindingInflater = ItemListingBinding::inflate,
            events = productViewModel::onEvent,
            onProductClick = ::navigateToDetailFragment
        )
        rvProduct.layoutManager = GridLayoutManager(requireContext(), SPAN_COUNT)
        rvProduct.addItemDecoration(GridSpacingItemDecoration())
        rvProduct.adapter = productAdapter
    }

    private fun navigateToDetailFragment(productId: Long) {
        if (isAdded) {
            findNavController().navigate(
                ListingFragmentDirections.actionListingFragmentToDetailFragment(productId)
            )
        }
    }

    private fun FragmentListingBinding.observeRemoteChanges() = with(productViewModel) {
        scopeWithLifecycle {
            products.collectLatest { response ->
                when (response) {
                    is BaseResponse.Error -> Unit
                    BaseResponse.Loading -> shimmerLayoutProduct.startShimmer()

                    is BaseResponse.Success -> {
                        productAdapter.saveData(response.data)
                        shimmerLayoutProduct.run {
                            stopShimmer()
                            visibility = View.GONE
                        }
                        rvProduct.visibility = View.VISIBLE
                    }
                }
            }
        }

        scopeWithLifecycle {
            layoutSuggestedProduct.apply {
                suggestedProducts.collectLatest { response ->
                    when (response) {
                        is BaseResponse.Error -> Unit
                        BaseResponse.Loading -> shimmerLayout.startShimmer()

                        is BaseResponse.Success -> {
                            suggestedProductAdapter.saveData(response.data)
                            shimmerLayout.run {
                                stopShimmer()
                                visibility = View.GONE
                            }
                            rvSuggestedProduct.visibility = View.VISIBLE
                        }
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

    companion object {
        private const val SPAN_COUNT = 3
    }
}
