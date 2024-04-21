package com.patika.getir_lite.data.remote.model

import com.patika.getir_lite.data.local.model.ProductEntity
import com.patika.getir_lite.model.ProductType
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

fun SuggestedProductDto.toProductEntity(): ProductEntity {
    val imageURL = imageURL ?: squareThumbnailURL
    val attribute = shortDescription ?: ""
    return ProductEntity(
        productId = id,
        price = price.toBigDecimal(),
        name = name.trim(),
        attribute = attribute.trim(),
        imageURL = imageURL,
        productType = ProductType.SUGGESTED_PRODUCT
    )
}
