package com.patika.getir_lite.feature.listing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.patika.getir_lite.data.ProductRepository
import com.patika.getir_lite.model.Response
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ListingViewModel @Inject constructor(
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _remoteUiState = MutableStateFlow(ListingRemoteUiState())
    val remoteUiState = _remoteUiState.asStateFlow()

    fun fetchProductData() = viewModelScope.launch {
        _remoteUiState.update {
            it.copy(product = Response.Loading, suggestedProduct = Response.Loading)
        }

        val productDeferred = async {
            productRepository.getProducts()
        }

        val suggestedProductDeferred = async {
            productRepository.getSuggestedProducts()
        }

        val productResponse = productDeferred.await().toResponse()
        val suggestedProductResponse = suggestedProductDeferred.await().toResponse()

        _remoteUiState.update {
            it.copy(product = productResponse, suggestedProduct = suggestedProductResponse)
        }
    }
}
