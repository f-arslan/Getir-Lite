package com.patika.getir_lite.data.remote.model

import kotlinx.serialization.Serializable

@Serializable
data class SuggestedProductDto(
    val id: String,
    val imageURL: String? = null,
    val price: Double,
    val name: String,
    val priceText: String,
    val shortDescription: String? = null,
    val category: String? = null,
    val unitPrice: Double? = null,
    val squareThumbnailURL: String? = null,
    val status: Int? = null
)
