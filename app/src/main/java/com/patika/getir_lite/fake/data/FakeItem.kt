package com.patika.getir_lite.fake.data

import com.patika.getir_lite.data.local.model.ItemEntity

val fakeItemEntities = (1..50).map {
    ItemEntity(
        id = it.toLong(),
        productId = it.toLong(),
        orderId = it.toLong(),
        count = it
    )
}
