package com.patika.getir_lite.model

import androidx.room.Embedded
import androidx.room.Relation
import com.patika.getir_lite.data.local.model.ItemEntity
import com.patika.getir_lite.data.local.model.ItemWithProduct
import com.patika.getir_lite.data.local.model.OrderEntity

data class BasketWithProducts(
    @Embedded val order: OrderEntity,
    @Relation(
        entity = ItemEntity::class,
        parentColumn = "id",
        entityColumn = "orderId"
    )
    val itemsWithProducts: List<ItemWithProduct>
)
