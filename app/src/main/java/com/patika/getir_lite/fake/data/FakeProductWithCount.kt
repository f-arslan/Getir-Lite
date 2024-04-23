package com.patika.getir_lite.fake.data

import com.patika.getir_lite.model.ProductType
import com.patika.getir_lite.model.ProductWithCount
import java.math.BigDecimal

val fakeProductWithCounts: List<ProductWithCount> = (0..100).map {
    ProductWithCount(
        productId = it.toLong(),
        name = "Product $it",
        price = BigDecimal(10 * it),
        type = if (it % 2 == 0) {
            ProductType.PRODUCT
        } else {
            ProductType.SUGGESTED_PRODUCT
        }
    )
}
