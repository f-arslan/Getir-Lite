package com.patika.getir_lite.data.local.model

import androidx.room.Embedded
import androidx.room.Relation
import com.patika.getir_lite.model.ProductWithCount

data class ItemWithProduct(
    @Embedded val item: ItemEntity,
    @Relation(
        parentColumn = "productId",
        entityColumn = "id"
    )
    val product: ProductEntity
)

fun ItemWithProduct.toProductWithCount() = ProductWithCount(
    productId = product.id,
    name = product.name,
    price = product.price,
    attribute = product.attribute,
    imageURL = product.imageURL,
    count = item.count,
    productType = product.productType
)
