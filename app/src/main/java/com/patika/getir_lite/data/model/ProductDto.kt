package com.patika.getir_lite.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ProductDto(
    val id: String,
    val name: String,
    val attribute: String? = null,
    val thumbnailURL: String,
    val imageURL: String,
    val price: Double,
    val priceText: String,
    val shortDescription: String? = null
)
