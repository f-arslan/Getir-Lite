package com.patika.getir_lite.model

import androidx.room.Embedded
import androidx.room.Relation
import com.patika.getir_lite.data.local.model.OrderEntity
import com.patika.getir_lite.data.local.model.ProductEntity

data class BasketWithProducts(
    @Embedded val order: OrderEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "orderId",
        entity = ProductEntity::class
    )
    val products: List<ProductEntity>
)
