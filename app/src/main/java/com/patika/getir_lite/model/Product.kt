package com.patika.getir_lite.model

import java.math.BigDecimal

data class Product(
    val id: String,
    val name: String,
    val price: BigDecimal,
    val attribute: String? = null,
    val imageUrl: String,
    val count: Int = 0
)
