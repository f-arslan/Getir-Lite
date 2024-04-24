package com.patika.getir_lite.feature.basket

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.patika.getir_lite.data.ProductRepository
import com.patika.getir_lite.util.TopLevelException.GenericOperationFail
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject

@HiltViewModel
class BasketViewModel @Inject constructor(
    private val productRepository: ProductRepository
) : ViewModel() {
    private val _basketUiState = MutableStateFlow<BasketUiState>(BasketUiState.Idle)
    val basketUiState = _basketUiState.asStateFlow()

    private val _sizeUiState = MutableStateFlow(SizeUiState())
    val sizeUiState = _sizeUiState.asStateFlow()

    fun onClearAndFinishBasketClick() {
        viewModelScope.launch {
            val isCleaned = productRepository.clearBasket()
            when {
                isCleaned -> _basketUiState.update { BasketUiState.Completed }
                else -> _basketUiState.update { BasketUiState.Error(GenericOperationFail()) }
            }
        }
    }

    fun updateIsZeroState(price: BigDecimal) {
        _sizeUiState.update { it.copy(isZero = price.toDouble() == 0.0) }
    }

    fun updateSizeState(newSize: Int) {
        val currentTime = System.currentTimeMillis()
        val sizeState = when {
            newSize > _sizeUiState.value.lastSize -> SizeState.Bigger
            newSize < _sizeUiState.value.lastSize -> SizeState.Smaller
            else -> SizeState.Idle
        }
        _sizeUiState.update {
            it.copy(
                sizeState = sizeState,
                lastSize = newSize,
                lastUpdateTime = currentTime
            )
        }
        resetStateIfNeeded(currentTime)
    }

    private var timerJob: Job? = null
    private fun resetStateIfNeeded(timeOfLastUpdate: Long) {
        timerJob?.cancel()

        timerJob = viewModelScope.launch {
            delay(350)
            val currentState = _sizeUiState.value
            if (currentState.lastUpdateTime == timeOfLastUpdate) {
                _sizeUiState.update {
                    it.copy(sizeState = SizeState.Idle, lastUpdateTime = System.currentTimeMillis())
                }
            }
        }
    }

    fun resetUiState() {
        _basketUiState.update { BasketUiState.Idle }
    }
}
