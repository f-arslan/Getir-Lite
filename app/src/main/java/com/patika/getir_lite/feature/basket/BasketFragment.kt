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
import com.patika.getir_lite.data.local.model.ItemWithProduct
import com.patika.getir_lite.data.local.model.toProductWithCount
import com.patika.getir_lite.databinding.FragmentBasketBinding
import com.patika.getir_lite.feature.BaseFragment
import com.patika.getir_lite.feature.adapter.HeaderAdapter
import com.patika.getir_lite.feature.adapter.ProductAdapter
import com.patika.getir_lite.feature.adapter.ProductListAdapter
import com.patika.getir_lite.model.BaseResponse
import com.patika.getir_lite.util.decor.DividerDecoration
import com.patika.getir_lite.util.ext.formatPrice
import com.patika.getir_lite.util.ext.makeSnackbar
import com.patika.getir_lite.util.ext.safeNavigate
import com.patika.getir_lite.util.ext.scopeWithLifecycle
import com.patika.getir_lite.util.ext.showCancelDialog
import com.patika.getir_lite.util.ext.showCompleteDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

/**
 * A fragment for displaying and managing a basket of products This clas handles the UI related to the
 * shopping basket, including interactions such as completing or clearing the basket,
 * and navigating to product details or the listing page.
 */
@AndroidEntryPoint
class BasketFragment : BaseFragment<FragmentBasketBinding>() {

    private val viewModel: BasketViewModel by viewModels()
    private val productViewModel: ProductViewModel by activityViewModels()
    private lateinit var basketProductAdapter: ProductAdapter
    private lateinit var productListAdapter: ProductListAdapter
    private lateinit var concatAdapter: ConcatAdapter

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentBasketBinding = FragmentBasketBinding.inflate(inflater, container, false)

    override fun FragmentBasketBinding.onMain() {
        setupTextView()
        setupRecycleViewAndAdapters()
        listenBasketProductsState()
        observeSuggestedProduct()
        listenDeleteBasketClick()
        listenUiStateChanges()
        listenOnCancelClick()
        finishOrderClick()
        notifyChanges()
    }

    private fun FragmentBasketBinding.finishOrderClick() {
        btnCompleteBasket.setOnClickListener {
            showCompleteDialog(
                tvFinalPrice.text.toString(),
                viewModel::onClearAndFinishBasketClick
            )
        }
    }

    private fun FragmentBasketBinding.listenUiStateChanges() = scopeWithLifecycle {
        fun performNavigation(action: () -> Unit) {
            action()
            viewModel.resetUiState()
        }

        viewModel.basketUiState.collectLatest { state ->
            when (state) {
                BasketUiState.Completed -> performNavigation(::navigateToListing)

                is BasketUiState.Error -> makeSnackbar(state.exception.message)
                else -> Unit
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
        btnDeleteBasket.setOnClickListener {
            when {
                isZero -> makeSnackbar(getString(R.string.basket_already_empty))
                else -> showCancelDialog(viewModel::onClearAndFinishBasketClick)
            }
        }
    }

    private fun FragmentBasketBinding.setupRecycleViewAndAdapters() {
        basketProductAdapter = ProductAdapter(
            viewType = ProductAdapter.BASKET_VIEW_TYPE,
            events = productViewModel::onEvent,
            onProductClick = ::navigateToDetailFragment
        )

        val headerAdapter = HeaderAdapter(getString(R.string.suggested_product))

        productListAdapter = ProductListAdapter(
            events = productViewModel::onEvent,
            onProductClick = ::navigateToDetailFragment
        )

        concatAdapter = ConcatAdapter(basketProductAdapter, headerAdapter)

        rvBasket.apply {
            adapter = concatAdapter
            itemAnimator?.apply {
                changeDuration = 150
                addDuration = 150
                removeDuration = 150
                moveDuration = 150
            }

            val divider = ContextCompat.getDrawable(requireContext(), R.drawable.divider)
            val margin = resources.getDimensionPixelSize(R.dimen.margin_8)
            divider?.let {
                addItemDecoration(DividerDecoration(divider, margin))
            }
        }
    }

    private fun navigateToDetailFragment(productId: Long) {
        safeNavigate(BasketFragmentDirections.actionBasketFragmentToDetailFragment(productId))
    }

    private fun navigateToListing() {
        safeNavigate(BasketFragmentDirections.actionBasketFragmentToListingFragment())
    }

    private fun FragmentBasketBinding.listenBasketProductsState() = scopeWithLifecycle {
        productViewModel.basketWithProducts.collectLatest { basketWithProducts ->
            when (basketWithProducts) {
                is BaseResponse.Success -> {
                    val data = basketWithProducts.data
                    val price =
                        data.order.totalPrice.also { viewModel.updateIsZeroState(it) }
                            .formatPrice()

                    basketProductAdapter.saveData(data.itemsWithProducts.map(ItemWithProduct::toProductWithCount))
                    viewModel.updateSizeState(data.itemsWithProducts.size)
                    tvFinalPrice.text = price
                    SpannableString(price).apply {
                        setSpan(StrikethroughSpan(), 0, price.length, 0)
                        tvOldPrice.text = this
                    }
                }

                else -> Unit
            }
        }
    }

    private fun observeSuggestedProduct() = scopeWithLifecycle {
        productViewModel.suggestedProducts.collectLatest { response ->
            if (response is BaseResponse.Success) {
                productListAdapter.saveData(response.data)
            }
        }
    }

    private fun FragmentBasketBinding.setupTextView() {
        layoutToolbar.textView.text = getString(R.string.basket_title)
    }

    private fun notifyChanges() = scopeWithLifecycle {
        viewModel.sizeUiState.collectLatest { sizeUiState ->
            isZero = sizeUiState.isZero
            if (this@BasketFragment::concatAdapter.isInitialized) {
                when (sizeUiState.sizeState) {
                    SizeState.Bigger, SizeState.Smaller -> {
                        val isAdapterPresent =
                            concatAdapter.adapters.any { it is ProductListAdapter }
                        if (isAdapterPresent) {
                            concatAdapter.removeAdapter(productListAdapter)
                        } else {
                            concatAdapter.addAdapter(productListAdapter)
                        }
                    }

                    else -> {
                        if (!concatAdapter.adapters.any { it is ProductListAdapter }) {
                            concatAdapter.addAdapter(productListAdapter)
                        }
                    }
                }
            }
        }
    }

    companion object {
        private var isZero = true
    }
}
