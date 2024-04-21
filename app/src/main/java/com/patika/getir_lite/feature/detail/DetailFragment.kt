package com.patika.getir_lite.feature.detail

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import coil.load
import com.patika.getir_lite.ProductViewModel
import com.patika.getir_lite.databinding.FragmentDetailBinding
import com.patika.getir_lite.feature.BaseFragment
import com.patika.getir_lite.model.BaseResponse
import com.patika.getir_lite.model.ProductEvent
import com.patika.getir_lite.util.ext.animateBasketVisibility
import com.patika.getir_lite.util.ext.formatPrice
import com.patika.getir_lite.util.ext.safeNavigate
import com.patika.getir_lite.util.ext.scopeWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class DetailFragment : BaseFragment<FragmentDetailBinding>() {

    private val args: DetailFragmentArgs by navArgs()
    private val productViewModel: ProductViewModel by activityViewModels()
    private val viewModel: DetailViewModel by viewModels()

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentDetailBinding = FragmentDetailBinding.inflate(inflater, container, false)

    override fun FragmentDetailBinding.onMain() {
        val productId = args.productId
        viewModel.initializeProduct(productId)

        observeProductState()
        updateProductAction()
        listenActionClickListeners(productId)
        listenBasket()
        basketClickOperation()
        listenCancelClick()
    }

    private fun FragmentDetailBinding.listenActionClickListeners(productId: Long) =
        with(productViewModel) {
            with(itemActionView) {
                setOnDeleteClickListener {
                    onEvent(ProductEvent.OnDeleteClick(productId))
                }
                setOnMinusClickListener {
                    onEvent(ProductEvent.OnMinusClick(productId, -1))
                }
                setOnPlusClickListener {
                    onEvent(ProductEvent.OnPlusClick(productId, -1))
                }
            }
            btnAddToBasket.setOnClickListener {
                onEvent(ProductEvent.OnPlusClick(productId, -1))
            }
        }

    private fun FragmentDetailBinding.updateProductAction() = scopeWithLifecycle {
        viewModel.productState.collectLatest {
            if (it is BaseResponse.Success) {
                val count = it.data.count
                btnAddToBasket.visibility = if (count <= 0) View.VISIBLE else View.GONE
                itemActionView.visibility = if (count > 0) View.VISIBLE else View.GONE
                if (count > 0) itemActionView.setCount(count)
            }
        }
    }

    private fun FragmentDetailBinding.observeProductState() = scopeWithLifecycle {
        viewModel.productState.collectLatest { response ->
            when (response) {
                is BaseResponse.Error -> {}
                BaseResponse.Loading -> {}
                is BaseResponse.Success -> {
                    val product = response.data
                    ivFood.load(product.imageURL) {
                        crossfade(true)
                    }
                    tvFoodPrice.text = product.price.formatPrice()
                    tvProductName.text = product.name
                    tvProductAttribute.text = product.attribute
                }
            }
        }
    }

    private fun FragmentDetailBinding.listenBasket() = scopeWithLifecycle {
        productViewModel.basket.collectLatest { response ->
            with(layoutTotalPriceCard) {
                when (response) {
                    is BaseResponse.Error -> cvTotalPrice.visibility = View.GONE

                    BaseResponse.Loading -> cvTotalPrice.visibility = View.GONE

                    is BaseResponse.Success -> {
                        response.data?.totalPrice?.let {
                            cvTotalPrice.animateBasketVisibility(it, requireContext(), tvTotalPrice)
                        } ?: run {
                            cvTotalPrice.visibility = View.GONE
                        }
                    }
                }
            }
        }
    }

    private fun FragmentDetailBinding.listenCancelClick() {
        btnCancel.setOnClickListener {
            if (isAdded) {
                findNavController().popBackStack()
            }
        }
    }

    private fun FragmentDetailBinding.basketClickOperation() = with(layoutTotalPriceCard) {
        cvTotalPrice.setOnClickListener {
            navigateToBasket()
        }
    }

    private fun navigateToBasket() {
        safeNavigate(DetailFragmentDirections.actionDetailFragmentToBasketFragment())
    }
}
