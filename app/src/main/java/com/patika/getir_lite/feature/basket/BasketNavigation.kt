package com.patika.getir_lite.feature.basket

import com.patika.getir_lite.util.TopLevelException.GenericOperationFail

sealed interface BasketNavigation {
    data object None : BasketNavigation
    data object NavigateToListing : BasketNavigation
    data class Error(val exception: GenericOperationFail) : BasketNavigation
}
