package com.patika.getir_lite

import androidx.annotation.MainThread
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.patika.getir_lite.data.ProductRepository
import com.patika.getir_lite.model.BaseResponse
import com.patika.getir_lite.model.Order
import com.patika.getir_lite.model.Product
import com.patika.getir_lite.util.TopLevelException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductViewModel @Inject constructor(private val productRepository: ProductRepository) :
    ViewModel() {

    val products: Flow<BaseResponse<List<Product>>> = productRepository
        .getProductsAsFlow()
        .map<List<Product>, BaseResponse<List<Product>>> { BaseResponse.Success(it) }
        .catch { error ->
            emit(BaseResponse.Error(TopLevelException.GenericException(error.message)))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = BaseResponse.Loading
        )

    val suggestedProducts: Flow<BaseResponse<List<Product>>> = productRepository
        .getSuggestedProductsAsFlow()
        .map<List<Product>, BaseResponse<List<Product>>> { BaseResponse.Success(it) }
        .catch { error ->
            emit(BaseResponse.Error(TopLevelException.GenericException(error.message)))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = BaseResponse.Loading
        )

    val basket: Flow<BaseResponse<Order?>> = productRepository
        .getBasketAsFlow()
        .map<Order?, BaseResponse<Order?>> { BaseResponse.Success(it) }
        .catch { cause ->
            emit(BaseResponse.Error(TopLevelException.GenericException(cause.message)))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = BaseResponse.Loading
        )

    @MainThread
    fun initializeProductData() = viewModelScope.launch {
        productRepository.syncWithRemote()
    }
}
