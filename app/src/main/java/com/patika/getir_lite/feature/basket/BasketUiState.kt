package com.patika.getir_lite.feature.basket

import com.patika.getir_lite.util.TopLevelException.GenericOperationFail

sealed interface BasketUiState {
    data object Idle : BasketUiState
    data object Completed : BasketUiState
    data object Cleaned : BasketUiState
    data class Error(val exception: GenericOperationFail) : BasketUiState
}
