package com.patika.getir_lite.feature.detail

import android.view.LayoutInflater
import android.view.ViewGroup
import com.patika.getir_lite.R
import com.patika.getir_lite.databinding.FragmentDetailBinding
import com.patika.getir_lite.databinding.TotalPriceCardBinding
import com.patika.getir_lite.feature.BaseFragment

class DetailFragment : BaseFragment<FragmentDetailBinding>() {
    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentDetailBinding = FragmentDetailBinding.inflate(inflater, container, false)

    override fun FragmentDetailBinding.initializeViews() {
        layoutTotalPriceCard.tvTotalPrice.text = "₺ 20.00"
    }
}
