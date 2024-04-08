package com.patika.getir_lite.feature.listing

import android.view.LayoutInflater
import android.view.ViewGroup
import com.patika.getir_lite.databinding.FragmentListingBinding
import com.patika.getir_lite.feature.BaseFragment

class ListingFragment : BaseFragment<FragmentListingBinding>() {
    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentListingBinding =
        FragmentListingBinding.inflate(inflater, container, false)

    override fun FragmentListingBinding.initializeViews() {
        
    }
}