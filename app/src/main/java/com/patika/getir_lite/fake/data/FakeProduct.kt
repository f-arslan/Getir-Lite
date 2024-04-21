package com.patika.getir_lite.fake.data

import com.patika.getir_lite.data.local.model.ProductEntity
import com.patika.getir_lite.model.ProductType

val fakeProductEntities = (1..100).map {
    ProductEntity(
        id = it.toLong(),
        productId = it.toString(),
        name = "Product $it",
        price = it.toBigDecimal(),
        attribute = "Attribute $it",
        imageURL = "https://picsum.photos/200/300",
        productType = ProductType.entries.toTypedArray().random()
    )
}
