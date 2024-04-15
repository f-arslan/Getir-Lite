package com.patika.getir_lite.feature.basket

import android.view.LayoutInflater
import android.view.ViewGroup
import com.patika.getir_lite.databinding.FragmentBasketBinding
import com.patika.getir_lite.feature.BaseFragment

class BasketFragment : BaseFragment<FragmentBasketBinding>() {
    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentBasketBinding = FragmentBasketBinding.inflate(inflater, container, false)


    override fun FragmentBasketBinding.onMain() {

    }
}
