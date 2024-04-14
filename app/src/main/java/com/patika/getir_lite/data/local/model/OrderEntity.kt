package com.patika.getir_lite.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.patika.getir_lite.model.Order
import java.math.BigDecimal

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val orderStatus: OrderStatus = OrderStatus.IDLE,
    val totalPrice: BigDecimal = BigDecimal.ZERO
)

fun OrderEntity.toDomainModel() = Order(
    id = id,
    orderStatus = orderStatus,
    totalPrice = totalPrice
)
