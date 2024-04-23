package com.patika.getir_lite.feature.detail

import androidx.annotation.MainThread
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.patika.getir_lite.data.ProductRepository
import com.patika.getir_lite.model.BaseResponse
import com.patika.getir_lite.model.ProductWithCount
import com.patika.getir_lite.util.TopLevelException.ProductNotLoadedException
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
    private val _productState =
        MutableStateFlow<BaseResponse<ProductWithCount>>(BaseResponse.Loading)
    val productState = _productState.asStateFlow()

    @MainThread
    fun initializeProduct(productId: Long) = viewModelScope.launch {
        productRepository.getProductAsFlow(productId).collect { product ->
            product?.let {
                _productState.update { BaseResponse.Success(product) }
            } ?: run {
                _productState.update { BaseResponse.Error(ProductNotLoadedException()) }
            }
        }
    }
}
