package com.patika.getir_lite.feature.basket

import android.text.SpannableString
import android.text.style.StrikethroughSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.patika.getir_lite.R
import com.patika.getir_lite.databinding.FragmentBasketBinding
import com.patika.getir_lite.feature.BaseFragment
import com.patika.getir_lite.util.ext.scopeWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class BasketFragment : BaseFragment<FragmentBasketBinding>() {

    private val viewModel: BasketViewModel by viewModels()

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentBasketBinding = FragmentBasketBinding.inflate(inflater, container, false)

    override fun FragmentBasketBinding.onMain() {
        setupTextViews()
        listenBasketWithProducts()
    }

    private fun listenBasketWithProducts() = with(viewModel) {
        scopeWithLifecycle {
            basketWithProducts.collectLatest {
                println(it)
            }
        }
    }

    private fun FragmentBasketBinding.setupTextViews() = with(tvOldPrice) {
        layoutToolbar.textView.text = getString(R.string.basket_title)
        tvFinalPrice.text = getString(R.string._2000_00)

        text = getString(R.string._2000_00) // Debug purposes
        SpannableString(text).apply {
            setSpan(StrikethroughSpan(), 0, text.length, 0)
            text = this
        }
    }
}
