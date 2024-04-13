package com.patika.getir_lite.model

import java.math.BigDecimal

data class SuggestedProduct(
    val id: String,
    val price: BigDecimal,
    val name: String,
    val attribute: String,
    val imageURL: String? = null,
    val count: Int = 0  // TODO: Update UI view with count
)
