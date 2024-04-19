package com.patika.getir_lite.feature.basket

import android.text.SpannableString
import android.text.style.StrikethroughSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ConcatAdapter
import com.patika.getir_lite.ProductViewModel
import com.patika.getir_lite.R
import com.patika.getir_lite.data.local.model.toDomainModel
import com.patika.getir_lite.databinding.FragmentBasketBinding
import com.patika.getir_lite.databinding.ItemBasketBinding
import com.patika.getir_lite.feature.BaseFragment
import com.patika.getir_lite.feature.adapter.HeaderAdapter
import com.patika.getir_lite.feature.adapter.ProductAdapter
import com.patika.getir_lite.feature.adapter.ProductListAdapter
import com.patika.getir_lite.model.BaseResponse
import com.patika.getir_lite.util.decor.DividerDecoration
import com.patika.getir_lite.util.ext.formatPrice
import com.patika.getir_lite.util.ext.makeSnackbar
import com.patika.getir_lite.util.ext.scopeWithLifecycle
import com.patika.getir_lite.util.ext.showCancelDialog
import com.patika.getir_lite.util.ext.showCompleteDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class BasketFragment : BaseFragment<FragmentBasketBinding>() {

    private val viewModel: BasketViewModel by viewModels()
    private val productViewModel: ProductViewModel by activityViewModels()
    private lateinit var basketProductAdapter: ProductAdapter<ItemBasketBinding>
    private lateinit var productListAdapter: ProductListAdapter

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentBasketBinding = FragmentBasketBinding.inflate(inflater, container, false)

    override fun FragmentBasketBinding.onMain() {
        setupTextView()
        setupRecycleViewAndAdapters()
        listenBasketWithProducts()
        listenSuggestedProducts()
        listenDeleteBasketClick()
        listenUiStateChanges()
        listenOnCancelClick()
        finishOrderClick()
    }

    private fun FragmentBasketBinding.finishOrderClick() {
        btnCompleteBasket.setOnClickListener {
            viewModel.onFinishOrderClick()
        }
    }

    private fun FragmentBasketBinding.listenUiStateChanges() = scopeWithLifecycle {
        fun performNavigation(action: () -> Unit) {
            action()
            viewModel.resetUiState()
        }

        viewModel.basketUiState.collectLatest { state ->
            when (state) {
                BasketUiState.Cleaned -> performNavigation(::navigateToListing)
                BasketUiState.Completed -> showCompleteDialog { performNavigation(::navigateToListing) }
                is BasketUiState.Error -> makeSnackbar(state.exception.message)
                BasketUiState.Idle -> Unit
            }
        }
    }

    private fun FragmentBasketBinding.listenOnCancelClick() {
        layoutToolbar.btnCancel.setOnClickListener {
            if (isAdded) {
                findNavController().popBackStack()
            }
        }
    }

    private fun FragmentBasketBinding.listenDeleteBasketClick() {
        btnDeleteBasket.setOnClickListener { showCancelDialog(viewModel::onClearBasketClick) }
    }

    private fun FragmentBasketBinding.setupRecycleViewAndAdapters() {
        basketProductAdapter = ProductAdapter(
            bindingInflater = ItemBasketBinding::inflate,
            events = productViewModel::onEvent,
            onProductClick = ::navigateToDetailFragment
        )

        val divider = ContextCompat.getDrawable(requireContext(), R.drawable.divider)
        val margin = resources.getDimensionPixelSize(R.dimen.margin_8)
        divider?.let {
            rvBasket.addItemDecoration(DividerDecoration(divider, margin))
        }

        val headerAdapter = HeaderAdapter(getString(R.string.suggested_product))
        productListAdapter = ProductListAdapter(
            events = productViewModel::onEvent,
            onProductClick = ::navigateToDetailFragment
        )

        val concatAdapter = ConcatAdapter(basketProductAdapter, headerAdapter, productListAdapter)
        rvBasket.adapter = concatAdapter
    }

    private fun navigateToDetailFragment(productId: Long) {
        if (isAdded) {
            findNavController().navigate(
                BasketFragmentDirections.actionBasketFragmentToDetailFragment(productId)
            )
        }
    }

    private fun navigateToListing() {
        if (isAdded) {
            findNavController().navigate(
                BasketFragmentDirections.actionBasketFragmentToListingFragment()
            )
        }
    }

    private fun listenSuggestedProducts() = scopeWithLifecycle {
        productViewModel.suggestedProducts.collectLatest { response ->
            when (response) {
                is BaseResponse.Error -> Unit
                BaseResponse.Loading -> Unit

                is BaseResponse.Success -> {
                    productListAdapter.saveData(response.data)
                }
            }
        }
    }

    private fun FragmentBasketBinding.listenBasketWithProducts() = with(productViewModel) {
        scopeWithLifecycle {
            basketWithProducts.collectLatest { response ->
                when (response) {
                    is BaseResponse.Success -> {
                        val data = response.data
                        val price = data.order.totalPrice.formatPrice()
                        basketProductAdapter.saveData(data.products.map { it.toDomainModel() })
                        tvFinalPrice.text = price
                        SpannableString(price).apply {
                            setSpan(StrikethroughSpan(), 0, price.length, 0)
                            tvOldPrice.text = this
                        }
                        if (data.order.totalPrice.toDouble() == 0.0) {
                            navigateToListingWithAnimDelay()
                        }
                    }

                    else -> Unit
                }
            }
        }
    }

    private fun navigateToListingWithAnimDelay() = scopeWithLifecycle {
        val maxAnimDuration = binding.rvBasket.itemAnimator?.let {
            listOf(
                it.addDuration,
                it.removeDuration,
                it.moveDuration,
                it.changeDuration
            ).maxOrNull() ?: 0L
        } ?: 0L
        delay(maxAnimDuration + 150L)
        navigateToListing()
    }

    private fun FragmentBasketBinding.setupTextView() {
        layoutToolbar.textView.text = getString(R.string.basket_title)
    }
}
