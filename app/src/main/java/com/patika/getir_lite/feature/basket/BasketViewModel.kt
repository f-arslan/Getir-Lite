package com.patika.getir_lite.feature.basket

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.patika.getir_lite.data.ProductRepository
import com.patika.getir_lite.util.TopLevelException.GenericOperationFail
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BasketViewModel @Inject constructor(
    private val productRepository: ProductRepository
) : ViewModel() {
    private val _basketUiState = MutableStateFlow<BasketUiState>(BasketUiState.Idle)
    val basketUiState = _basketUiState.asStateFlow()

    fun onClearAndFinishBasketClick() {
        viewModelScope.launch {
            val isCleaned = productRepository.clearBasket()
            when {
                isCleaned -> _basketUiState.update { BasketUiState.Completed }
                else -> _basketUiState.update { BasketUiState.Error(GenericOperationFail()) }
            }
        }
    }

    fun resetUiState() {
        _basketUiState.update { BasketUiState.Idle }
    }
}
