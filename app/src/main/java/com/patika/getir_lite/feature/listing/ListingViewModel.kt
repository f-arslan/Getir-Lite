package com.patika.getir_lite.feature.listing

import androidx.lifecycle.ViewModel
import com.patika.getir_lite.data.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ListingViewModel @Inject constructor(
    private val productRepository: ProductRepository
) : ViewModel() {


}
