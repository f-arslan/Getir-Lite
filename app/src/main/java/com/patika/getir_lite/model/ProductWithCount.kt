package com.patika.getir_lite.model

import java.math.BigDecimal

data class ProductWithCount(
    val productId: Long,
    val name: String,
    val price: BigDecimal,
    val attribute: String? = null,
    val imageURL: String? = null,
    val count: Int = 0,
    val type: ProductType
)
