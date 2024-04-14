package com.patika.getir_lite.model

import com.patika.getir_lite.data.local.model.OrderStatus
import java.math.BigDecimal

data class Order(
    val id: Long,
    val orderStatus: OrderStatus,
    val totalPrice: BigDecimal
)
