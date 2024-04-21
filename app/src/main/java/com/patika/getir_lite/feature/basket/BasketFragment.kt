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
import androidx.recyclerview.widget.RecyclerView
import com.patika.getir_lite.AnimationState
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

@AndroidEntryPoint
class BasketFragment : BaseFragment<FragmentBasketBinding>(), AnimationFinishListener {

    private val viewModel: BasketViewModel by viewModels()
    private val productViewModel: ProductViewModel by activityViewModels()
    private lateinit var basketProductAdapter: ProductAdapter
    private lateinit var productListAdapter: ProductListAdapter
    private lateinit var headerAdapter: HeaderAdapter

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentBasketBinding = FragmentBasketBinding.inflate(inflater, container, false)

    override fun FragmentBasketBinding.onMain() {
        productViewModel.notifyActionCompleted(AnimationState.FINISHED)
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
            viewType = ProductAdapter.BASKET_VIEW_TYPE,
            events = productViewModel::onEvent,
            onProductClick = ::navigateToDetailFragment
        )

        headerAdapter = HeaderAdapter(getString(R.string.suggested_product))

        productListAdapter = ProductListAdapter(
            events = productViewModel::onEvent,
            onProductClick = ::navigateToDetailFragment
        )

        val concatAdapter = ConcatAdapter(basketProductAdapter, headerAdapter, productListAdapter)
        rvBasket.adapter = concatAdapter
        rvBasket.itemAnimator = RVItemAnimator(this@BasketFragment)

        val divider = ContextCompat.getDrawable(requireContext(), R.drawable.divider)
        val margin = resources.getDimensionPixelSize(R.dimen.margin_8)
        divider?.let {
            rvBasket.addItemDecoration(DividerDecoration(divider, margin))
        }
    }

    private fun navigateToDetailFragment(productId: Long) {
        safeNavigate(BasketFragmentDirections.actionBasketFragmentToDetailFragment(productId))
    }

    private fun navigateToListing() {
        safeNavigate(BasketFragmentDirections.actionBasketFragmentToListingFragment())
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
                        basketProductAdapter.saveData(data.itemsWithProducts.map(ItemWithProduct::toProductWithCount))
                        tvFinalPrice.text = price
                        SpannableString(price).apply {
                            setSpan(StrikethroughSpan(), 0, price.length, 0)
                            tvOldPrice.text = this
                        }
                        if (data.order.totalPrice.toDouble() == 0.0) {
                            navigateToListing()
                        }
                    }

                    else -> Unit
                }
            }
        }
    }

    private fun FragmentBasketBinding.setupTextView() {
        layoutToolbar.textView.text = getString(R.string.basket_title)
    }

    override fun onAddFinished(item: RecyclerView.ViewHolder) {
        productViewModel.notifyActionCompleted(AnimationState.FINISHED)
    }

    override fun onRemoveFinished(item: RecyclerView.ViewHolder) {
        productViewModel.notifyActionCompleted(AnimationState.FINISHED)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        productViewModel.notifyActionCompleted(AnimationState.OPENED)
    }
}
