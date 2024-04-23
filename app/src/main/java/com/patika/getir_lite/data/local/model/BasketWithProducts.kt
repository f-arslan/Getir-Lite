package com.patika.getir_lite.data.local.model

import androidx.room.Embedded
import androidx.room.Relation

data class BasketWithProducts(
    @Embedded val order: OrderEntity,
    @Relation(
        entity = ItemEntity::class,
        parentColumn = "id",
        entityColumn = "orderId"
    )
    val itemsWithProducts: List<ItemWithProduct>
)
