package com.patika.getir_lite.data.remote.model

import com.patika.getir_lite.model.Product
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

fun ProductDto.toDomainModel(): Product {
    val attribute = attribute ?: shortDescription
    return Product(id, name.trim(), price.toBigDecimal(), attribute?.trim(), imageURL)
}
