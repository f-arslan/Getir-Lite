package com.patika.getir_lite.feature.listing

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.patika.getir_lite.ProductViewModel
import com.patika.getir_lite.databinding.FragmentListingBinding
import com.patika.getir_lite.feature.BaseFragment
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
    private val viewModel: ListingViewModel by viewModels()
    private lateinit var suggestedProductAdapter: ProductAdapter
    private lateinit var productAdapter: ProductAdapter

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentListingBinding =
        FragmentListingBinding.inflate(inflater, container, false)

    override fun FragmentListingBinding.onMain() {
        observeRemoteChanges()
        setupRecycleViews()
    }

    private fun FragmentListingBinding.setupRecycleViews() {
        suggestedProductAdapter = ProductAdapter(viewModel::onEvent, ::navigateToDetailFragment)
        rvSuggestedProduct.adapter = suggestedProductAdapter
        rvSuggestedProduct.addItemDecoration(MarginItemDecoration())

        productAdapter = ProductAdapter(viewModel::onEvent, ::navigateToDetailFragment)
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
