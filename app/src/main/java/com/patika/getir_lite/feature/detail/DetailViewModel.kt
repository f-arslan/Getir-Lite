package com.patika.getir_lite.feature.detail

import androidx.annotation.MainThread
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.patika.getir_lite.data.ProductRepository
import com.patika.getir_lite.model.BaseResponse
import com.patika.getir_lite.model.Product
import com.patika.getir_lite.model.ProductEvent
import com.patika.getir_lite.util.TopLevelException.ProductNotFoundException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val productRepository: ProductRepository
) : ViewModel() {
    private val _productState = MutableStateFlow<BaseResponse<Product>>(BaseResponse.Loading)
    val productState = _productState.asStateFlow()

    @MainThread
    fun initializeProduct(productId: Long) = viewModelScope.launch {
        productRepository.getProductAsFlow(productId).collect { product ->
            product?.let {
                _productState.update { BaseResponse.Success(product) }
            } ?: run {
                _productState.update { BaseResponse.Error(ProductNotFoundException()) }
            }
        }
    }

    fun onEvent(event: ProductEvent) {
        viewModelScope.launch {
            when (event) {
                is ProductEvent.OnDeleteClick -> {
                    productRepository.updateItemCount(event.entityId, -1)
                }

                is ProductEvent.OnMinusClick -> {
                    productRepository.updateItemCount(event.entityId, -1)
                }

                is ProductEvent.OnPlusClick -> {
                    productRepository.updateItemCount(event.entityId, 1)
                }
            }
        }
    }
}
