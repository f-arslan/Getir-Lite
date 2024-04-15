package com.patika.getir_lite.model

sealed class ProductEvent {
    data class OnPlusClick(val entityId: Long) : ProductEvent()
    data class OnMinusClick(val entityId: Long) : ProductEvent()
    data class OnDeleteClick(val entityId: Long) : ProductEvent()
}
