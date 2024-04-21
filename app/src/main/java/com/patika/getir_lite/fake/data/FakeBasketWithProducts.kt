package com.patika.getir_lite.fake.data

import com.patika.getir_lite.data.local.model.ItemWithProduct
import com.patika.getir_lite.data.local.model.OrderEntity
import com.patika.getir_lite.data.local.model.OrderStatus
import com.patika.getir_lite.model.BasketWithProducts


val fakeItemWithProducts = fakeItemEntities.map {
    ItemWithProduct(
        it,
        fakeProductEntities.random()
    )
}

val fakeBasketWithProducts = (1..10).map {
    BasketWithProducts(
        OrderEntity(
            id = it.toLong(),
            orderStatus = OrderStatus.entries.toTypedArray().random(),
            totalPrice = it.toBigDecimal()
        ),
        fakeItemWithProducts
    )
}
