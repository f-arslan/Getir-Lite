package com.patika.getir_lite.model

import java.math.BigDecimal

data class Product(
    val id: String,
    val name: String,
    val price: BigDecimal,
    val attribute: String? = null,
    val imageURL: String? = null,
    val count: Int = 0,
    val productType: ProductType = ProductType.PRODUCT
)
