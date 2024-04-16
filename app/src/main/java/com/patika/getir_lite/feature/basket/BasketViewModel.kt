package com.patika.getir_lite.feature.basket

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.patika.getir_lite.data.ProductRepository
import com.patika.getir_lite.model.BaseResponse
import com.patika.getir_lite.model.BasketWithProducts
import com.patika.getir_lite.util.TopLevelException.GenericException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transform
import javax.inject.Inject

@HiltViewModel
class BasketViewModel @Inject constructor(
    productRepository: ProductRepository
) : ViewModel() {
    val basketWithProducts: Flow<BaseResponse<BasketWithProducts>> = productRepository
        .getBasketWithProductsAsFlow()
        .transform {
            when (it) {
                null -> emit(BaseResponse.Loading)
                else -> emit(BaseResponse.Success(it))
            }
        }
        .catch { error ->
            emit(BaseResponse.Error(GenericException(error.message)))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = BaseResponse.Loading
        )
}
