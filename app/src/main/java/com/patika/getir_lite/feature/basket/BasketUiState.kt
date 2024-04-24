package com.patika.getir_lite.feature.basket

import com.patika.getir_lite.util.TopLevelException.GenericOperationFail

sealed interface BasketUiState {
    data object Idle : BasketUiState
    data object Completed : BasketUiState
    data class Error(val exception: GenericOperationFail) : BasketUiState
}

enum class SizeState {
    Idle, Bigger, Smaller
}

data class SizeUiState(
    val lastSize: Int = -1,
    val sizeState: SizeState = SizeState.Idle,
    val isZero: Boolean = true,
    val lastUpdateTime: Long = System.currentTimeMillis()
)
