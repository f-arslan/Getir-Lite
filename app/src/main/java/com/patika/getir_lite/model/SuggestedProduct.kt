package com.patika.getir_lite.model

import java.math.BigDecimal

data class SuggestedProduct(
    val id: String,
    val imageURL: String? = null,
    val price: BigDecimal,
    val name: String,
    val attribute: String,
    val count: Int = 0
)
