package com.patika.getir_lite.feature.listing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.patika.getir_lite.data.remote.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ListingViewModel @Inject constructor(
    private val productRepository: ProductRepository
) : ViewModel() {
    fun initialize() {
        viewModelScope.launch {
            val x = productRepository.getProducts()
            println(x)
            val y = productRepository.getSuggestedProducts()
            println(y)
        }
    }
}

