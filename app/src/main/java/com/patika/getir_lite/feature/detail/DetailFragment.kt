package com.patika.getir_lite.feature.detail

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import coil.load
import com.patika.getir_lite.ProductViewModel
import com.patika.getir_lite.databinding.FragmentDetailBinding
import com.patika.getir_lite.feature.BaseFragment
import com.patika.getir_lite.model.BaseResponse
import com.patika.getir_lite.model.ProductEvent
import com.patika.getir_lite.util.ext.formatPrice
import com.patika.getir_lite.util.ext.scopeWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import java.math.BigDecimal

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
        val productId = args.productId.also(::println)
        viewModel.initializeProduct(productId)

        observeProductState()
        updateProductAction()
        listenActionClickListeners(productId)
        listenBasket()
    }

    private fun FragmentDetailBinding.listenActionClickListeners(productId: Long) =
        with(viewModel) {
            with(itemActionView) {
                setOnDeleteClickListener {
                    onEvent(ProductEvent.OnDeleteClick(productId))
                }
                setOnMinusClickListener {
                    onEvent(ProductEvent.OnMinusClick(productId))
                }
                setOnPlusClickListener {
                    onEvent(ProductEvent.OnPlusClick(productId))
                }
            }
            btnAddToBasket.setOnClickListener {
                onEvent(ProductEvent.OnPlusClick(productId))
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

    private fun FragmentDetailBinding.listenBasket() = with(productViewModel) {
        scopeWithLifecycle {
            basket.collectLatest { response ->
                with(layoutTotalPriceCard) {
                    when (response) {
                        is BaseResponse.Error -> cvTotalPrice.visibility = View.GONE

                        BaseResponse.Loading -> cvTotalPrice.visibility = View.GONE

                        is BaseResponse.Success -> {
                            response.data?.totalPrice?.let {
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
        }
    }
}
