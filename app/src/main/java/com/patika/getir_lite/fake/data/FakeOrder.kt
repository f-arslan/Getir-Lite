package com.patika.getir_lite.fake.data

import com.patika.getir_lite.data.local.model.OrderEntity
import com.patika.getir_lite.data.local.model.OrderStatus
import com.patika.getir_lite.model.Order
import java.math.BigDecimal

val fakeOrders: List<Order> = listOf(
    Order(1, OrderStatus.IDLE, BigDecimal(1)),
    Order(2, OrderStatus.IDLE, BigDecimal(2)),
    Order(3, OrderStatus.IDLE, BigDecimal(3)),
    Order(4, OrderStatus.IDLE, BigDecimal(4)),
    Order(5, OrderStatus.IDLE, BigDecimal(5))
)

val fakeOrderEntities: List<OrderEntity> = listOf(
    OrderEntity(1, OrderStatus.IDLE, BigDecimal(1)),
    OrderEntity(2, OrderStatus.IDLE, BigDecimal(2)),
    OrderEntity(3, OrderStatus.IDLE, BigDecimal(3)),
    OrderEntity(4, OrderStatus.IDLE, BigDecimal(4)),
    OrderEntity(5, OrderStatus.IDLE, BigDecimal(5))
)
